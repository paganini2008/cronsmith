
package com.github.cronsmith.cron;

import java.time.DayOfWeek;
import java.util.Iterator;

/**
 * 
 * @Description: Week
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public interface Week extends Iterator<Week>, CronExpression {

    int getYear();

    int getMonth();

    int getWeek();

    int getWeekOfYear();

    default Day everyDay() {
        return everyDay(1);
    }

    default Day everyDay(int interval) {
        return everyDay(1, 7, interval);
    }

    default Day everyDay(int from, int to, int interval) {
        return everyDay(w -> from, w -> to, interval);
    }

    Day everyDay(IntFunction<Week> from, IntFunction<Week> to, int interval);

    TheDayOfWeek day(int dayOfWeek);

    default TheDayOfWeek everyWeekday() {
        return Mon().toFri();
    }

    default TheDayOfWeek Mon() {
        return day(DayOfWeek.MONDAY.getValue());
    }

    default TheDayOfWeek Tues() {
        return day(DayOfWeek.TUESDAY.getValue());
    }

    default TheDayOfWeek Wed() {
        return day(DayOfWeek.WEDNESDAY.getValue());
    }

    default TheDayOfWeek Thur() {
        return day(DayOfWeek.THURSDAY.getValue());
    }

    default TheDayOfWeek Fri() {
        return day(DayOfWeek.FRIDAY.getValue());
    }

    default TheDayOfWeek Sat() {
        return day(DayOfWeek.SATURDAY.getValue());
    }

    default TheDayOfWeek Sun() {
        return day(DayOfWeek.SUNDAY.getValue());
    }

}
