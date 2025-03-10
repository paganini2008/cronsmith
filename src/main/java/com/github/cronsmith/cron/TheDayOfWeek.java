
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheDayOfWeek
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public interface TheDayOfWeek extends DayOfWeek {

    TheDayOfWeek andDay(int dayOfWeek);

    default TheDayOfWeek toDay(int dayOfWeek) {
        return toDay(dayOfWeek, 1);
    }

    default TheDayOfWeek toDay(java.time.DayOfWeek dayOfWeek) {
        return toDay(dayOfWeek, 1);
    }

    default TheDayOfWeek toDay(java.time.DayOfWeek dayOfWeek, int interval) {
        return toDay(dayOfWeek.getValue(), interval);
    }

    TheDayOfWeek toDay(int dayOfWeek, int interval);

    default TheDayOfWeek toWed() {
        return toDay(3);
    }

    default TheDayOfWeek toThur() {
        return toDay(4);
    }

    default TheDayOfWeek toFri() {
        return toDay(5);
    }

    default TheDayOfWeek toSat() {
        return toDay(6);
    }

    default TheDayOfWeek toSun() {
        return toDay(7);
    }

    default TheDayOfWeek andTues() {
        return andDay(2);
    }

    default TheDayOfWeek andWed() {
        return andDay(3);
    }

    default TheDayOfWeek andThur() {
        return andDay(4);
    }

    default TheDayOfWeek andFri() {
        return andDay(5);
    }

    default TheDayOfWeek andSat() {
        return andDay(6);
    }

    default TheDayOfWeek andSun() {
        return andDay(7);
    }
}
