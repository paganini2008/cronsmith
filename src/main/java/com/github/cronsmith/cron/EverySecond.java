package com.github.cronsmith.cron;

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
public class EverySecond implements Second, IntervalChronoUnit {

    private static final long serialVersionUID = -2606684197757806223L;
    private Minute minute;
    private LocalDateTime second;
    private final IntFunction<Minute> from;
    private final int interval;
    private boolean self;
    private boolean forward;

    EverySecond(Minute minute, IntFunction<Minute> from, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.minute = minute;
        this.from = from;
        this.interval = interval;
        this.second = minute.getTime().withSecond(getFromSecond() + (interval - 1));
        this.self = true;
        this.forward = true;
    }

    @Override
    public int getFrom() {
        return getFromSecond();
    }

    private int getFromSecond() {
        int fromSecond = from.apply(minute);
        ChronoField.SECOND_OF_MINUTE.checkValidValue(fromSecond);
        return fromSecond;
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || second.getSecond() + interval <= 59);
        if (!next) {
            if (minute.hasNext()) {
                minute = minute.next();
                second = minute.getTime().withSecond(getFromSecond() + (interval - 1));
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
    public int getInterval() {
        return interval;
    }

    @Override
    public String toCronString() {
        int fromSecond = getFromSecond();
        String str = "";
        boolean slashed = interval > 1;
        if (fromSecond == 0) {
            str = "*";
        } else if (fromSecond > 0) {
            str = String.valueOf(fromSecond);
            slashed = true;
        }
        return slashed ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
