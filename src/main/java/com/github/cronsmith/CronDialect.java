package com.github.cronsmith;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 *
 * The cron flavours cronsmith can print an expression in.
 * <p>
 * They differ in how many fields they take, whether a year is allowed, and which of the special
 * tags they understand. Everything is derived from the seven-field form cronsmith builds
 * internally; a schedule a target flavour simply cannot express is reported rather than quietly
 * rewritten into something that would fire at different times.
 * <p>
 * Day-of-week is always printed by name. The numeric conventions disagree with each other - Quartz
 * and AWS count SUN=1..SAT=7 while Spring and Unix count MON=1..SUN=7 - whereas {@code MON}
 * means Monday everywhere.
 *
 * @Description: CronDialect
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
public enum CronDialect {

    /**
     * The library's own form: {@code second minute hour day-of-month month day-of-week [year]}.
     */
    CRONSMITH {
        @Override
        String render(String[] fields) {
            return join(fields);
        }
    },

    /**
     * Quartz, which reads the same seven fields and the same tags, so the expression is handed
     * over unchanged.
     */
    QUARTZ {
        @Override
        String render(String[] fields) {
            return join(fields);
        }
    },

    /**
     * Spring Scheduling, six fields with no year. Spring understands {@code L}, {@code LW},
     * {@code W} and {@code #}, but has no way to restrict a schedule to particular years.
     */
    SPRING {
        @Override
        String render(String[] fields) {
            requireNoYear(fields, this);
            reject(fields[DAY_OF_MONTH], L_MINUS, this, "'L-<n>' in the day-of-month field");
            return join(Arrays.copyOfRange(fields, SECOND, YEAR));
        }
    },

    /**
     * AWS EventBridge, six fields starting at minutes and always ending with a year. It has no
     * seconds field, so a schedule that fires at anything other than one fixed second cannot be
     * expressed.
     */
    AWS {
        @Override
        String render(String[] fields) {
            requireFixedSecond(fields, this);
            reject(fields[DAY_OF_MONTH], L_MINUS, this, "'L-<n>' in the day-of-month field");
            String[] target = Arrays.copyOfRange(fields, MINUTE, FIELD_COUNT);
            if (target[target.length - 1] == null) {
                // EventBridge always wants the year field, even when it places no restriction.
                target[target.length - 1] = "*";
            }
            return join(target);
        }
    },

    /**
     * Unix crontab, five fields from minutes to day-of-week. It knows none of the Quartz
     * extensions, so {@code L}, {@code W}, {@code #} and a year restriction all have to be
     * refused; {@code ?} is written as {@code *}, which is what crontab expects.
     */
    UNIX {
        @Override
        String render(String[] fields) {
            requireFixedSecond(fields, this);
            requireNoYear(fields, this);
            reject(fields[DAY_OF_MONTH], EXTENSION, this, "'L' or 'W' in the day-of-month field");
            reject(fields[DAY_OF_WEEK], EXTENSION, this, "'L' or '#' in the day-of-week field");
            String[] target = Arrays.copyOfRange(fields, MINUTE, YEAR);
            for (int i = 0; i < target.length; i++) {
                if ("?".equals(target[i])) {
                    target[i] = "*";
                }
            }
            return join(target);
        }
    };

    /*
     * Positions in the canonical field array cronsmith renders an expression into. They are shared
     * with CRON, which fills the array in, so neither side has to take a finished expression apart
     * again to reshape it.
     */
    static final int SECOND = 0;
    static final int MINUTE = 1;
    static final int HOUR = 2;
    static final int DAY_OF_MONTH = 3;
    static final int MONTH = 4;
    static final int DAY_OF_WEEK = 5;
    static final int YEAR = 6;

    /** Number of canonical fields, the last of which may be {@code null}. */
    static final int FIELD_COUNT = 7;

    private static final Pattern FIXED_SECOND = Pattern.compile("\\d{1,2}");
    private static final Pattern L_MINUS = Pattern.compile(".*L-\\d+.*");
    private static final Pattern EXTENSION = Pattern.compile(".*[LW#].*");

    /**
     * Rewrites the canonical fields into this flavour's own layout.
     *
     * @param fields exactly {@link #FIELD_COUNT} entries, the year possibly {@code null}
     */
    abstract String render(String[] fields);

    /**
     * Renders a cronsmith expression string in this flavour. Prefer
     * {@link CRON#toCronString(com.github.cronsmith.cron.CronExpression, CronDialect)} when the
     * expression itself is at hand; this entry point is for strings that come from elsewhere.
     *
     * @throws UnsupportedOperationException when the schedule has no equivalent here
     */
    public String render(String cronString) {
        String[] parts = cronString.trim().split("\\s+");
        if (parts.length < YEAR || parts.length > FIELD_COUNT) {
            throw new IllegalArgumentException("Not a cronsmith expression: " + cronString);
        }
        return render(Arrays.copyOf(parts, FIELD_COUNT));
    }

    private static String join(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (String field : fields) {
            if (field == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(field);
        }
        return sb.toString();
    }

    private static void requireNoYear(String[] fields, CronDialect dialect) {
        if (fields[YEAR] != null && !"*".equals(fields[YEAR])) {
            throw new UnsupportedOperationException(
                    dialect + " has no year field, so the year '" + fields[YEAR]
                            + "' cannot be expressed");
        }
    }

    private static void requireFixedSecond(String[] fields, CronDialect dialect) {
        if (!FIXED_SECOND.matcher(fields[SECOND]).matches()) {
            throw new UnsupportedOperationException(dialect
                    + " has no seconds field, so the second '" + fields[SECOND]
                    + "' cannot be expressed");
        }
    }

    private static void reject(String field, Pattern pattern, CronDialect dialect, String what) {
        if (pattern.matcher(field).matches()) {
            throw new UnsupportedOperationException(dialect + " does not support " + what);
        }
    }

}
