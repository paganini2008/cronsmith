package com.github.cronsmith.cron;

import java.io.Serializable;
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
public class EveryDay implements Day, Serializable {

    private static final long serialVersionUID = -2114922383566430661L;
    private Month month;
    private LocalDateTime day;
    private final IntFunction<Month> from;
    private final IntFunction<Month> to;
    private final DateTimeSupplier supplier;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryDay(Month month, IntFunction<Month> from, IntFunction<Month> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.from = from;
        this.to = to;
        this.supplier = getSupplier();
        this.day = supplier.get();
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private DateTimeSupplier getSupplier() {
        return () -> month.getTime().withDayOfMonth(getFromDay()).withHour(0).withMinute(0)
                .withSecond(0);
    }


    private int getFromDay() {
        int fromDay = from.apply(month);
        ChronoField.DAY_OF_MONTH.checkValidValue(fromDay);
        return fromDay;
    }

    private int getToDay() {
        int toDay = to.apply(month);
        ChronoField.DAY_OF_MONTH.checkValidValue(toDay);
        return toDay;
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || day.getDayOfMonth() + interval <= getToDay());
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                day = supplier.get();
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
    public Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        boolean slashed = false;
        String str;
        int fromDay = getFromDay();
        int toDay = getToDay();
        if (fromDay == 1 && toDay == month.getLastDay()) {
            str = "*";
        } else if (fromDay != 1 && toDay == month.getLastDay()) {
            str = fromDay + "";
            slashed = true;
        } else {
            str = fromDay + "-" + toDay;
        }
        return interval > 1 ? str + "/" + interval : slashed ? str + "/1" : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
