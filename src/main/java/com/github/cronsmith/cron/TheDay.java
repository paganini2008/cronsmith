
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheDay
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public interface TheDay extends Day {

    TheDay andDay(int dayOfMonth);

    default Day andLastDay() {
        return andLastDay(0);
    }

    Day andLastDay(int n);

    TheDay andLatestWeekday(int dayOfMonth);

    Day andLastWeekday();

    default Day toLastDay() {
        return toLastDay(1);
    }

    Day toLastDay(int interval);

    Day toLastWeekday(int interval);

    TheDay toLatestWeekday(int dayOfMonth, int interval);

    default TheDay toDay(int dayOfMonth) {
        return toDay(dayOfMonth, 1);
    }

    TheDay toDay(int dayOfMonth, int interval);

}
