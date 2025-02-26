package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.function.Supplier;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryHour
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryHour implements Hour, Serializable {

    private static final long serialVersionUID = -5459905273757712271L;
    private Day day;
    private LocalDateTime hour;
    private final IntFunction<Day> from;
    private final IntFunction<Day> to;
    private final DateTimeSupplier supplier;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryHour(Day day, IntFunction<Day> from, IntFunction<Day> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.day = day;
        this.from = from;
        this.to = to;
        this.supplier = getSupplier();
        this.hour = supplier.get();
        this.interval = interval;
        this.self = true;
        this.forward = true;

    }

    private DateTimeSupplier getSupplier() {
        return () -> day.getTime().withHour(getFromHour()).withMinute(0).withSecond(0);
    }

    private int getFromHour() {
        int fromHour = from.apply(day);
        ChronoField.HOUR_OF_DAY.checkValidValue(fromHour);
        return fromHour;
    }

    private int getToHour() {
        int toHour = to.apply(day);
        ChronoField.HOUR_OF_DAY.checkValidValue(toHour);
        return toHour;
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || hour.getHour() + interval <= getToHour());
        if (!next) {
            if (day.hasNext()) {
                day = day.next();
                hour = supplier.get();
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Hour next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                hour = hour.plusHours(interval);
            } else {
                forward = true;
            }
        }
        return this;
    }

    @Override
    public int getYear() {
        return hour.getYear();
    }

    @Override
    public int getMonth() {
        return hour.getMonthValue();
    }

    @Override
    public int getDay() {
        return hour.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return hour.getHour();
    }

    @Override
    public LocalDateTime getTime() {
        return hour;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> (hour.toLocalDate().compareTo(target.toLocalDate()) < 0)
                || (hour.toLocalDate().compareTo(target.toLocalDate()) == 0
                        && hour.toLocalTime().isBefore(target.toLocalTime()));
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
    public TheMinute minute(int minute) {
        final Hour copy = (Hour) this.copy();
        return new ThisMinute(IteratorUtils.getFirst(copy), minute);
    }

    @Override
    public Minute everyMinute(IntFunction<Hour> from, IntFunction<Hour> to, int interval) {
        final Hour copy = (Hour) this.copy();
        return new EveryMinute(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return day;
    }

    @Override
    public String toCronString() {
        int fromHour = getFromHour();
        int toHour = getToHour();
        String str;
        boolean slashed = false;
        if (fromHour == 0 && toHour == 23) {
            str = "*";
        } else if (fromHour > 0 && toHour == 23) {
            str = String.valueOf(fromHour);
            slashed = true;
        } else {
            str = fromHour + "-" + toHour;
        }
        return interval > 1 ? str + "/" + interval : slashed ? str + "/1" : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
