
package com.github.cronsmith.cron;

import java.util.Iterator;

/**
 * 
 * @Description: Day
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public interface Day extends Iterator<Day>, CronExpression {

    int getYear();

    int getMonth();

    int getDay();

    int getDayOfWeek();

    int getDayOfYear();

    default Hour everyHour() {
        return everyHour(1);
    }

    default Hour everyHour(int interval) {
        return everyHour(0, 23, interval);
    }

    default Hour everyHour(int from, int to, int interval) {
        return everyHour(d -> from, d -> to, interval);
    }

    Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval);

    TheHour hour(int hourOfDay);

    default TheMinute at(int hourOfDay, int minute) {
        return hour(hourOfDay).minute(minute);
    }

    default TheSecond at(int hourOfDay, int minute, int second) {
        return hour(hourOfDay).minute(minute).second(second);
    }

}
