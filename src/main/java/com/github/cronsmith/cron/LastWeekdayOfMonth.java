package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: LastWeekdayOfMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastWeekdayOfMonth implements Day {

    private static final long serialVersionUID = -6476774989810500269L;
    private Month month;
    private LocalDateTime day;
    private boolean self;

    LastWeekdayOfMonth(Month month) {
        this.month = month;
        this.day = month.getTime().withDayOfMonth(month.getLastWeekday());
        this.self = true;
    }

    @Override
    public boolean hasNext() {
        boolean next = self;
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                day = month.getTime().withDayOfMonth(month.getLastWeekday());
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        if (self) {
            self = false;
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public int getYear() {
        return day.getYear();
    }

    @Override
    public int getMonth() {
        return day.getMonthValue();
    }

    @Override
    public int getDay() {
        return day.getDayOfMonth();
    }

    @Override
    public int getDayOfWeek() {
        return day.getDayOfWeek().getValue();
    }

    @Override
    public int getDayOfYear() {
        return day.getDayOfYear();
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public TheHour hour(int hourOfDay) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hourOfDay);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public String toCronString() {
        return "LW";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }
}
