package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheDayOfWeekInMonth
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public interface TheDayOfWeekInMonth extends DayOfWeek {

    default TheDayOfWeekInMonth and(int weekOfMonth, java.time.DayOfWeek dayOfWeek) {
        return and(weekOfMonth, dayOfWeek.getValue());
    }

    TheDayOfWeekInMonth and(int weekOfMonth, int dayOfWeek);

    TheDayOfWeekInMonth andLast(int dayOfWeek);

    default TheDayOfWeekInMonth andLastSun() {
        return andLast(7);
    }

    default TheDayOfWeekInMonth andLastSat() {
        return andLast(6);
    }

    default TheDayOfWeekInMonth andLastFri() {
        return andLast(5);
    }

    default TheDayOfWeekInMonth andLastThur() {
        return andLast(4);
    }

    default TheDayOfWeekInMonth andLastWed() {
        return andLast(3);
    }

    default TheDayOfWeekInMonth andLastTues() {
        return andLast(2);
    }

    default TheDayOfWeekInMonth andLastMon() {
        return andLast(1);
    }
}
