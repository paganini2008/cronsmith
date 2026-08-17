package com.github.cronsmith;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.temporal.ChronoField;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 * Conversion between abbreviation and integer value for month field and day-of-week field
 * 
 * @Author: Fred Feng
 * @Date: 23/02/2025
 * @Version 1.0.0
 */
public class AbbreviationUtils {

    private AbbreviationUtils() {}

    private static final Map<String, Integer> dayOfWeekNameValues = new LinkedHashMap<>();
    private static final Map<Integer, String> dayOfWeekNames;
    private static final Map<String, Integer> monthNameValues = new LinkedHashMap<>();
    private static final Map<Integer, String> monthNames;

    static {
        dayOfWeekNameValues.put("MON", DayOfWeek.MONDAY.getValue());
        dayOfWeekNameValues.put("TUE", DayOfWeek.TUESDAY.getValue());
        dayOfWeekNameValues.put("WED", DayOfWeek.WEDNESDAY.getValue());
        dayOfWeekNameValues.put("THU", DayOfWeek.THURSDAY.getValue());
        dayOfWeekNameValues.put("FRI", DayOfWeek.FRIDAY.getValue());
        dayOfWeekNameValues.put("SAT", DayOfWeek.SATURDAY.getValue());
        dayOfWeekNameValues.put("SUN", DayOfWeek.SUNDAY.getValue());

        dayOfWeekNames = MapUtils.exchange(dayOfWeekNameValues);

        monthNameValues.put("JAN", Month.JANUARY.getValue());
        monthNameValues.put("FEB", Month.FEBRUARY.getValue());
        monthNameValues.put("MAR", Month.MARCH.getValue());
        monthNameValues.put("APR", Month.APRIL.getValue());
        monthNameValues.put("MAY", Month.MAY.getValue());
        monthNameValues.put("JUN", Month.JUNE.getValue());
        monthNameValues.put("JUL", Month.JULY.getValue());
        monthNameValues.put("AUG", Month.AUGUST.getValue());
        monthNameValues.put("SEP", Month.SEPTEMBER.getValue());
        monthNameValues.put("OCT", Month.OCTOBER.getValue());
        monthNameValues.put("NOV", Month.NOVEMBER.getValue());
        monthNameValues.put("DEC", Month.DECEMBER.getValue());

        monthNames = MapUtils.exchange(monthNameValues);
    }

    public static int getDayOfWeekValue(String repr) {
        return dayOfWeekNameValues.getOrDefault(repr, -1);
    }

    public static int getMonthValue(String repr) {
        return monthNameValues.getOrDefault(repr, -1);
    }

    public static String getDayOfWeekName(int dayOfWeek) {
        return dayOfWeekNames.get(dayOfWeek);
    }

    public static String getMonthName(int month) {
        return monthNames.get(month);
    }

    /**
     * Translates a {@link DayOfWeek} value (MON=1 .. SUN=7) into the numbering cron expressions
     * use (SUN=1 .. SAT=7), which is what Quartz, AWS EventBridge and Unix crontab all read.
     */
    public static int toCronDayOfWeek(int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        return dayOfWeek % 7 + 1;
    }

    /**
     * The reverse of {@link #toCronDayOfWeek(int)}: SUN=1 .. SAT=7 becomes MON=1 .. SUN=7.
     * <p>
     * A leading 0 is read as Sunday for the benefit of people used to Unix crontab. Note that the
     * two conventions disagree about 7 - Quartz reads it as Saturday, Unix as Sunday - and this
     * library follows Quartz, which is the numbering its own seven-field format is modelled on.
     */
    public static int fromCronDayOfWeek(int cronDayOfWeek) {
        if (cronDayOfWeek < 0 || cronDayOfWeek > 7) {
            throw new IllegalArgumentException("Invalid day-of-week: " + cronDayOfWeek);
        }
        if (cronDayOfWeek <= 1) {
            return DayOfWeek.SUNDAY.getValue();
        }
        return cronDayOfWeek - 1;
    }

}
