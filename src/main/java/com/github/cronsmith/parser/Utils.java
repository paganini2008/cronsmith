package com.github.cronsmith.parser;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;
import com.github.cronsmith.MapUtils;

/**
 * 
 * @Description: Utils
 * @Author: Fred Feng
 * @Date: 02/03/2025
 * @Version 1.0.0
 */
public abstract class Utils {

    private static final Map<String, Integer> dayOfWeekNameValues = new LinkedHashMap<>();
    private static final Map<Integer, String> dayOfWeekNames;
    private static final Map<String, Integer> monthNameValues = new LinkedHashMap<>();
    private static final Map<Integer, String> monthNames;

    static {
        dayOfWeekNameValues.put("MON", DayOfWeek.MONDAY.getValue());
        dayOfWeekNameValues.put("TUES", DayOfWeek.TUESDAY.getValue());
        dayOfWeekNameValues.put("WED", DayOfWeek.WEDNESDAY.getValue());
        dayOfWeekNameValues.put("THUR", DayOfWeek.THURSDAY.getValue());
        dayOfWeekNameValues.put("FRI", DayOfWeek.FRIDAY.getValue());
        dayOfWeekNameValues.put("SAT", DayOfWeek.SATURDAY.getValue());
        dayOfWeekNameValues.put("SUN", DayOfWeek.SUNDAY.getValue());

        dayOfWeekNames = MapUtils.exchange(dayOfWeekNameValues);

        monthNameValues.put("JAN", Month.JANUARY.getValue());
        monthNameValues.put("FEB", Month.FEBRUARY.getValue());
        monthNameValues.put("MAR", Month.MARCH.getValue());
        monthNameValues.put("APR", Month.APRIL.getValue());
        monthNameValues.put("MAY", Month.MAY.getValue());
        monthNameValues.put("JUNE", Month.JUNE.getValue());
        monthNameValues.put("JULY", Month.JULY.getValue());
        monthNameValues.put("AUG", Month.AUGUST.getValue());
        monthNameValues.put("SEPT", Month.SEPTEMBER.getValue());
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

}
