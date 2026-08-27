package com.github.cronsmith.cron;

import java.util.Iterator;

/**
 *
 * @Description: Year
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public interface Year extends Iterator<Year>, CronExpression {

    static final int MAX_YEAR = 2999;

    int getYear();

    int getWeekCountOfYear();

    default int getLastDayOfYear() {
        return getLastDayOfYear(0);
    }

    int getLastDayOfYear(int n);

    default int getLastWeekdayOfYear() {
        return getLatestWeekdayOfYear(getLastDayOfYear());
    }

    int getLatestWeekdayOfYear(int dayOfYear);

    TheDay day(int dayOfYear);

    default Day lastDay() {
        // The last day of the year is always 31 December - a fixed calendar date plain cron
        // expresses directly - so it stays traditional (L in December) rather than becoming a
        // year-based day-of-year. Only what has no fixed field, e.g. day(100), is YCRON.
        return Dec().lastDay();
    }

    TheWeek week(int week);

    Week lastWeek();

    default boolean isLeapYear() {
        return getTime().toLocalDate().isLeapYear();
    }

    default Month everyMonth() {
        return everyMonth(1);
    }

    default Month everyMonth(int interval) {
        return everyMonth(1, interval);
    }

    default Month everyMonth(int from, int interval) {
        return everyMonth(y -> from, interval);
    }

    Month everyMonth(IntFunction<Year> from, int interval);

    TheMonth month(int month);

    default TheMonth Jan() {
        return month(1);
    }

    default TheMonth Feb() {
        return month(2);
    }

    default TheMonth Mar() {
        return month(3);
    }

    default TheMonth Apr() {
        return month(4);
    }

    default TheMonth May() {
        return month(5);
    }

    default TheMonth June() {
        return month(6);
    }

    default TheMonth July() {
        return month(7);
    }

    default TheMonth Aug() {
        return month(8);
    }

    default TheMonth Sept() {
        return month(9);
    }

    default TheMonth Oct() {
        return month(10);
    }

    default TheMonth Nov() {
        return month(11);
    }

    default TheMonth Dec() {
        return month(12);
    }
}
