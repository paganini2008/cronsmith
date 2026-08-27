package com.github.cronsmith.parser.ycron;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.github.cronsmith.antlr.YCronExpressionBaseVisitor;
import com.github.cronsmith.antlr.YCronExpressionParser.DayOfWeekContext;
import com.github.cronsmith.antlr.YCronExpressionParser.DayOfYearContext;
import com.github.cronsmith.antlr.YCronExpressionParser.HourContext;
import com.github.cronsmith.antlr.YCronExpressionParser.MinuteContext;
import com.github.cronsmith.antlr.YCronExpressionParser.SecondContext;
import com.github.cronsmith.antlr.YCronExpressionParser.WeekOfYearContext;
import com.github.cronsmith.antlr.YCronExpressionParser.YcronContext;
import com.github.cronsmith.antlr.YCronExpressionParser.YearContext;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.TheDay;
import com.github.cronsmith.cron.TheDayOfWeek;
import com.github.cronsmith.cron.TheWeek;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;
import com.github.cronsmith.parser.CronExpressionContext;
import com.github.cronsmith.utils.AbbreviationUtils;

/**
 * Turns a parsed YCRON tree into a {@link CronExpression}, mirroring how the traditional
 * {@link CronExpressionContext} works: {@link #visitYcron} walks the year-based line from the
 * outside in, threading the growing expression through one accumulator, and each field's visit
 * method extends it.
 *
 * <pre>
 *   &lt;sec&gt; &lt;min&gt; &lt;hour&gt; &lt;day-of-week&gt; &lt;week-of-year&gt; &lt;day-of-year&gt; [&lt;year&gt;]
 * </pre>
 *
 * <p>
 * Second, minute, hour and year are byte-for-byte the same as plain cron, so they are handed
 * straight to the traditional tag-visitor chain (through a private {@link CronExpressionContext}
 * delegate) rather than re-implemented. That delegate is only ever <em>read from and called</em> -
 * never modified - so reusing it here cannot affect traditional cron parsing. Only the three
 * year-based fields - day-of-week (paired with a week-of-year), week-of-year and day-of-year - are
 * parsed here. The mutual-exclusion rule (day-of-year alone, or day-of-week + week-of-year together,
 * the idle side written {@code ?}) is enforced in {@link #visitYcron}.
 *
 * @Author: Fred Feng
 * @Date: 27/08/2026
 * @Version 1.0.0
 */
public class YCronExpressionContext extends YCronExpressionBaseVisitor<CronExpression> {

    private static final String NA = "?";

    // Shared numeric shapes, used by the three year-based fields.
    private static final Pattern INT = Pattern.compile("(\\d+)");
    private static final Pattern STAR_STEP = Pattern.compile("\\*/(\\d+)");
    private static final Pattern RANGE = Pattern.compile("(\\d+)-(\\d+)");
    private static final Pattern RANGE_STEP = Pattern.compile("(\\d+)-(\\d+)/(\\d+)");
    private static final Pattern STEP = Pattern.compile("(\\d+)/(\\d+)");

    // Day-of-year only.
    private static final Pattern DOY_LATEST_WEEKDAY = Pattern.compile("(\\d+)W");
    private static final Pattern DOY_LAST_DAY_N = Pattern.compile("L-(\\d+)");
    private static final Pattern DOY_TO_LATEST_WEEKDAY = Pattern.compile("(\\d+)-(\\d+)W(?:/(\\d+))?");
    private static final Pattern DOY_TO_LAST_WEEKDAY = Pattern.compile("(\\d+)-LW(?:/(\\d+))?");
    private static final Pattern DOY_TO_LAST_DAY = Pattern.compile("(\\d+)-L(?:/(\\d+))?");

    // Day-of-week names.
    private static final String DOW = "SUN|MON|TUE|WED|THU|FRI|SAT";
    private static final Pattern DOW_NAME = Pattern.compile(DOW);
    private static final Pattern DOW_NAME_RANGE =
            Pattern.compile("(" + DOW + ")-(" + DOW + ")(?:/(\\d+))?");
    private static final Pattern DOW_NAME_STEP = Pattern.compile("(" + DOW + ")/(\\d+)");
    private static final Pattern DOW_NUM_RANGE = Pattern.compile("([1-7])-([1-7])(?:/(\\d+))?");

    /**
     * The traditional parsing context, reused verbatim for second / minute / hour / year and as the
     * single accumulator every field builds onto (its {@code cronExpression} plays the exact role
     * {@code CronExpressionContext} gives it).
     */
    // Zone lives on the delegate (it defaults to UTC, as in the traditional parser).
    private final CronExpressionContext base = new CronExpressionContext();

    @Override
    public CronExpression visitYcron(YcronContext ctx) {
        requireField(ctx.second(), "second");
        requireField(ctx.minute(), "minute");
        requireField(ctx.hour(), "hour");
        requireField(ctx.dayOfWeek(), "day-of-week");
        requireField(ctx.weekOfYear(), "week-of-year");
        requireField(ctx.dayOfYear(), "day-of-year");

        // Outer to inner, exactly like CronExpressionContext.visitCron; base holds the accumulator.
        base.setCronExpression(ctx.year() != null ? visit(ctx.year())
                : new CronBuilder().setZoneId(base.getZoneId()).everyYear());

        boolean doyPresent = !NA.equals(ctx.dayOfYear().getText());
        boolean woyPresent = !NA.equals(ctx.weekOfYear().getText());
        boolean dowPresent = !NA.equals(ctx.dayOfWeek().getText());
        if (doyPresent) {
            if (woyPresent || dowPresent) {
                throw new YCronParserException("day-of-year is exclusive with day-of-week and "
                        + "week-of-year; write '?' for the unused day-of-week and week-of-year");
            }
            base.setCronExpression(visit(ctx.dayOfYear()));
        } else {
            if (!woyPresent || !dowPresent) {
                throw new YCronParserException("week-of-year and day-of-week must appear together; "
                        + "write '?' for day-of-year");
            }
            base.setCronExpression(visit(ctx.weekOfYear()));
            base.setCronExpression(visit(ctx.dayOfWeek()));
        }

        base.setCronExpression(visit(ctx.hour()));
        base.setCronExpression(visit(ctx.minute()));
        return visit(ctx.second());
    }

    // ----------------------------------------------------------------- //
    // second / minute / hour / year - reused from the traditional chain //
    // ----------------------------------------------------------------- //

    @Override
    public CronExpression visitYear(YearContext ctx) {
        return base.getTagVisitor().visitYear(ctx.getText(), null, base);
    }

    @Override
    public CronExpression visitHour(HourContext ctx) {
        return base.getTagVisitor().visitHour(ctx.getText(), null, base);
    }

    @Override
    public CronExpression visitMinute(MinuteContext ctx) {
        return base.getTagVisitor().visitMinute(ctx.getText(), null, base);
    }

    @Override
    public CronExpression visitSecond(SecondContext ctx) {
        return base.getTagVisitor().visitSecond(ctx.getText(), null, base);
    }

    // ----------------------------------------------------------------- //
    // day-of-year (year-based, parsed here)                             //
    // ----------------------------------------------------------------- //

    @Override
    public CronExpression visitDayOfYear(DayOfYearContext ctx) {
        Year year = (Year) base.getCronExpression();
        Day acc = null;
        for (String el : ctx.getText().split(",")) {
            acc = dayOfYearElement(year, acc, el);
        }
        return acc;
    }

    private Day dayOfYearElement(Year year, Day acc, String el) {
        Matcher m;
        if ((m = RANGE_STEP.matcher(el)).matches()) {
            return theDay(year, acc, num(m, 1)).toDay(num(m, 2), num(m, 3));
        }
        if ((m = DOY_TO_LATEST_WEEKDAY.matcher(el)).matches()) {
            return theDay(year, acc, num(m, 1)).toLatestWeekday(num(m, 2), stepOr1(m, 3));
        }
        if ((m = DOY_TO_LAST_WEEKDAY.matcher(el)).matches()) {
            return theDay(year, acc, num(m, 1)).toLastWeekday(stepOr1(m, 2));
        }
        if ((m = DOY_TO_LAST_DAY.matcher(el)).matches()) {
            return theDay(year, acc, num(m, 1)).toLastDay(stepOr1(m, 2));
        }
        if ((m = RANGE.matcher(el)).matches()) {
            return theDay(year, acc, num(m, 1)).toDay(num(m, 2));
        }
        if ((m = DOY_LATEST_WEEKDAY.matcher(el)).matches()) {
            return requireDay(acc, el).andLatestWeekday(num(m, 1));
        }
        if ("LW".equals(el)) {
            return requireDay(acc, el).andLastWeekday();
        }
        if ((m = DOY_LAST_DAY_N.matcher(el)).matches()) {
            return requireDay(acc, el).andLastDay(num(m, 1));
        }
        if ("L".equals(el)) {
            return requireDay(acc, el).andLastDay();
        }
        if (INT.matcher(el).matches()) {
            return theDay(year, acc, Integer.parseInt(el));
        }
        throw new YCronParserException("Malformed day-of-year field: " + el);
    }

    private TheDay theDay(Year year, Day acc, int dayOfYear) {
        return acc == null ? year.day(dayOfYear) : ((TheDay) acc).andDay(dayOfYear);
    }

    private TheDay requireDay(Day acc, String el) {
        if (acc == null) {
            throw new YCronParserException("'" + el + "' cannot start a day-of-year field; the "
                    + "last day of the year is traditional cron ('L DEC'), not YCRON");
        }
        return (TheDay) acc;
    }

    // ----------------------------------------------------------------- //
    // week-of-year (year-based, parsed here)                            //
    // ----------------------------------------------------------------- //

    @Override
    public CronExpression visitWeekOfYear(WeekOfYearContext ctx) {
        Year year = (Year) base.getCronExpression();
        Week acc = null;
        for (String el : ctx.getText().split(",")) {
            acc = weekOfYearElement(year, acc, el);
        }
        return acc;
    }

    private Week weekOfYearElement(Year year, Week acc, String el) {
        Matcher m;
        if ((m = RANGE_STEP.matcher(el)).matches()) {
            return theWeek(year, acc, num(m, 1)).toWeek(num(m, 2), num(m, 3));
        }
        if ((m = RANGE.matcher(el)).matches()) {
            return theWeek(year, acc, num(m, 1)).toWeek(num(m, 2));
        }
        if ("L".equals(el)) {
            if (acc == null) {
                throw new YCronParserException("'L' cannot start a week-of-year field; the last "
                        + "week of the year is traditional cron ('DEC'), not YCRON");
            }
            return ((TheWeek) acc).andLastWeek();
        }
        if (INT.matcher(el).matches()) {
            return theWeek(year, acc, Integer.parseInt(el));
        }
        throw new YCronParserException("Malformed week-of-year field: " + el);
    }

    private TheWeek theWeek(Year year, Week acc, int week) {
        return acc == null ? year.week(week) : ((TheWeek) acc).andWeek(week);
    }

    // ----------------------------------------------------------------- //
    // day-of-week (year-based: paired with a week-of-year parent)       //
    // ----------------------------------------------------------------- //

    @Override
    public CronExpression visitDayOfWeek(DayOfWeekContext ctx) {
        Week week = (Week) base.getCronExpression();
        String text = ctx.getText();
        if ("*".equals(text)) {
            return week.everyDay();
        }
        Matcher m = STAR_STEP.matcher(text);
        if (m.matches()) {
            return week.everyDay(1, Integer.parseInt(m.group(1)));
        }
        Day acc = null;
        boolean useNumber = false;
        for (String el : text.split(",")) {
            if (DOW_NAME_RANGE.matcher(el).matches()) {
                Matcher r = DOW_NAME_RANGE.matcher(el);
                r.matches();
                acc = theDayOfWeek(week, acc, dow(r.group(1))).toDay(dow(r.group(2)), stepOr1(r, 3));
            } else if ((m = DOW_NUM_RANGE.matcher(el)).matches()) {
                useNumber = true;
                acc = theDayOfWeek(week, acc, cronDow(num(m, 1))).toDay(cronDow(num(m, 2)),
                        stepOr1(m, 3));
            } else if ((m = DOW_NAME_STEP.matcher(el)).matches()) {
                acc = theDayOfWeek(week, acc, dow(m.group(1))).toDay(7, num(m, 2));
            } else if ((m = STEP.matcher(el)).matches()) {
                useNumber = true;
                acc = theDayOfWeek(week, acc, cronDow(num(m, 1))).toDay(7, num(m, 2));
            } else if (DOW_NAME.matcher(el).matches()) {
                acc = theDayOfWeek(week, acc, dow(el));
            } else if (INT.matcher(el).matches()) {
                useNumber = true;
                acc = theDayOfWeek(week, acc, cronDow(Integer.parseInt(el)));
            } else {
                throw new YCronParserException("Malformed day-of-week field: " + el);
            }
        }
        acc.getBuilder().setUseDayOfWeekAsNumber(useNumber);
        return acc;
    }

    private TheDayOfWeek theDayOfWeek(Week week, Day acc, int dayOfWeek) {
        return acc == null ? week.day(dayOfWeek) : ((TheDayOfWeek) acc).andDay(dayOfWeek);
    }

    private static int dow(String name) {
        return AbbreviationUtils.getDayOfWeekValue(name);
    }

    /** Numbers in the day-of-week field are cron numbering, SUN=1 .. SAT=7. */
    private static int cronDow(int number) {
        return AbbreviationUtils.fromCronDayOfWeek(number);
    }

    // ----------------------------------------------------------------- //
    // helpers                                                           //
    // ----------------------------------------------------------------- //

    private static void requireField(Object field, String name) {
        if (field == null) {
            throw new YCronParserException("Missing or malformed '" + name + "' field");
        }
    }

    private static int num(Matcher m, int group) {
        return Integer.parseInt(m.group(group));
    }

    private static int stepOr1(Matcher m, int group) {
        String s = m.group(group);
        return s == null ? 1 : Integer.parseInt(s);
    }

}
