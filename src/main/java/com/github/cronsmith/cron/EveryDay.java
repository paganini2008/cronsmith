package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.function.Supplier;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryDay
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryDay implements Day, IntervalChronoUnit {

    private static final long serialVersionUID = -2114922383566430661L;
    private Month month;
    private LocalDateTime day;
    private final IntFunction<Month> from;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryDay(Month month, IntFunction<Month> from, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.from = from;
        this.interval = interval;
        this.day = month.getTime().withDayOfMonth(getFromDay() + (interval - 1));
        this.self = true;
        this.forward = true;
    }

    @Override
    public int getFrom() {
        return getFromDay();
    }

    private int getFromDay() {
        int fromDay = from.apply(month);
        ChronoField.DAY_OF_MONTH.checkValidValue(fromDay);
        return fromDay;
    }

    @Override
    public boolean hasNext() {
        int toDay = month.getLastDay();
        boolean next = (self || day.getDayOfMonth() + interval <= toDay);
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                day = month.getTime().withDayOfMonth(getFromDay() + (interval - 1));
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                day = day.plusDays(interval);
            } else {
                forward = true;
            }
        }
        return this;
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
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> day.toLocalDate().isBefore(target.toLocalDate());
        if (supplier.get()) {
            while (supplier.get()) {
                if (hasNext()) {
                    next();
                } else {
                    break;
                }
            }
            forward = false;
        }
        return this;
    }

    @Override
    public TheHour hour(int hour) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hour);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public String toCronString() {
        boolean slashed = interval > 1;
        String str = "";
        int fromDay = getFromDay();
        if (fromDay == 1) {
            str = "*";
        } else if (fromDay > 1) {
            str = String.valueOf(fromDay);
            slashed = true;
        }
        return slashed ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }



}
