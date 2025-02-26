
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
        return everyDay(m -> from, m -> m.getLastDay(), interval);
    }

    default Day everyDay(int from, int to, int interval) {
        return everyDay(m -> from, m -> to, interval);
    }

    Day everyDay(IntFunction<Month> from, IntFunction<Month> to, int interval);

    TheDay day(int dayOfMonth);

    Day lastDay(int n);

    default Day lastDay() {
        return lastDay(0);
    }

    default Day lastWeekday() {
        return latestWeekday(getLastDay());
    }

    Day latestWeekday(int dayOfMonth);

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
        return everyWeek(m -> 1, m -> {
            return m.getWeekCountOfMonth();
        }, interval);
    }

    default Week everyWeek(int from, int to, int interval) {
        return everyWeek(m -> from, m -> to, interval);
    }

    Week everyWeek(IntFunction<Month> from, IntFunction<Month> to, int interval);

}
