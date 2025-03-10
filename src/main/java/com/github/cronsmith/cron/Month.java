
package com.github.cronsmith.cron;

import java.util.Iterator;

/**
 * 
 * @Description: Month
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public interface Month extends Iterator<Month>, CronExpression {

    int getYear();

    int getMonth();

    default int getLastDay() {
        return getLastDay(0);
    }

    int getLastDay(int n);

    int getLastWeekday();

    int getLatestWeekday(int dayOfMonth);

    int getWeekCountOfMonth();

    default Day everyDay() {
        return everyDay(1);
    }

    default Day everyDay(int interval) {
        return everyDay(1, interval);
    }

    default Day everyDay(int from, int interval) {
        return everyDay(m -> from, interval);
    }

    Day everyDay(IntFunction<Month> from, int interval);

    TheDay day(int dayOfMonth);

    Day lastDay(int n);

    default Day lastDay() {
        return lastDay(0);
    }

    Day lastWeekday();

    TheDay latestWeekday(int dayOfMonth);

    TheWeek week(int weekOfMonth);

    default TheDayOfWeekInMonth dayOfWeek(int weekOfMonth, java.time.DayOfWeek dayOfWeek) {
        return dayOfWeek(weekOfMonth, dayOfWeek.getValue());
    }

    TheDayOfWeekInMonth dayOfWeek(int weekOfMonth, int dayOfWeek);

    default TheDayOfWeekInMonth lastDayOfWeek(int dayOfWeek) {
        return dayOfWeek(getWeekCountOfMonth(), dayOfWeek);
    }

    Week lastWeek();

    default Week everyWeek() {
        return everyWeek(1);
    }

    default Week everyWeek(int interval) {
        return everyWeek(1, interval);
    }

    default Week everyWeek(int from, int interval) {
        return everyWeek(m -> from, interval);
    }

    Week everyWeek(IntFunction<Month> from, int interval);

}
