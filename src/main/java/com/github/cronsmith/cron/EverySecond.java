package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.function.Supplier;
import com.github.cronsmith.CRON;

/**
 * 
 * @Description: EverySecond
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EverySecond implements Second, Serializable {

    private static final long serialVersionUID = -2606684197757806223L;
    private Minute minute;
    private LocalDateTime second;
    private final IntFunction<Minute> from;
    private final IntFunction<Minute> to;
    private final DateTimeSupplier supplier;
    private final int interval;
    private boolean self;
    private boolean forward;

    EverySecond(Minute minute, IntFunction<Minute> from, IntFunction<Minute> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.minute = minute;
        this.from = from;
        this.to = to;
        this.supplier = getSupplier();
        this.second = supplier.get();
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private DateTimeSupplier getSupplier() {
        return () -> minute.getTime().withSecond(getFromSecond());
    }

    private int getFromSecond() {
        int fromSecond = from.apply(minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue(fromSecond);
        return fromSecond;
    }

    private int getToSecond() {
        int toSecond = to.apply(minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue(toSecond);
        return toSecond;
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || second.getSecond() + interval <= getToSecond());
        if (!next) {
            if (minute.hasNext()) {
                minute = minute.next();
                second = supplier.get();
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Second next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                second = second.plusSeconds(interval);
            } else {
                forward = true;
            }
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return second;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier =
                () -> (second.toLocalDate().compareTo(target.toLocalDate()) < 0)
                        || (second.toLocalDate().compareTo(target.toLocalDate()) == 0
                                && second.toLocalTime().isBefore(target.toLocalTime()));
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
    public int getYear() {
        return second.getYear();
    }

    @Override
    public int getMonth() {
        return second.getMonthValue();
    }

    @Override
    public int getDay() {
        return second.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return second.getHour();
    }

    @Override
    public int getMinute() {
        return second.getMinute();
    }

    @Override
    public int getSecond() {
        return second.getSecond();
    }

    @Override
    public CronExpression getParent() {
        return minute;
    }

    @Override
    public String toCronString() {
        int fromSecond = getFromSecond();
        int toSecond = getToSecond();
        String str;
        boolean slashed = false;
        if (fromSecond == 0 && toSecond == 59) {
            str = "*";
        } else if (fromSecond > 0 && toSecond == 59) {
            str = String.valueOf(fromSecond);
            slashed = true;
        } else {
            str = fromSecond + "-" + toSecond;
        }
        return interval > 1 ? str + "/" + interval : slashed ? str + "/1" : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
