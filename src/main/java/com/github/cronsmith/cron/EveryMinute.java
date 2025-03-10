package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.function.Supplier;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryMinute
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryMinute implements Minute, IntervalChronoUnit {

    private static final long serialVersionUID = -7939881133025374416L;
    private Hour hour;
    private LocalDateTime minute;
    private final IntFunction<Hour> from;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryMinute(Hour hour, IntFunction<Hour> from, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.hour = hour;
        this.from = from;
        this.interval = interval;
        this.minute = hour.getTime().withMinute(getFromMinute() + (interval - 1));
        this.self = true;
        this.forward = true;
    }

    private int getFromMinute() {
        int fromMinute = from.apply(hour);
        ChronoField.MINUTE_OF_HOUR.checkValidValue(fromMinute);
        return fromMinute;
    }

    @Override
    public int getFrom() {
        return getFromMinute();
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || minute.getMinute() + interval <= 59);
        if (!next) {
            if (hour.hasNext()) {
                hour = hour.next();
                minute = hour.getTime().withMinute(getFromMinute() + (interval - 1));
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Minute next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                minute = minute.plusMinutes(interval);
            } else {
                forward = true;
            }
        }
        return this;
    }

    @Override
    public int getYear() {
        return minute.getYear();
    }

    @Override
    public int getMonth() {
        return minute.getMonthValue();
    }

    @Override
    public int getDay() {
        return minute.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return minute.getHour();
    }

    @Override
    public int getMinute() {
        return minute.getMinute();
    }

    @Override
    public LocalDateTime getTime() {
        return minute;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier =
                () -> (minute.toLocalDate().compareTo(target.toLocalDate()) < 0)
                        || (minute.toLocalDate().compareTo(target.toLocalDate()) == 0
                                && minute.toLocalTime().isBefore(target.toLocalTime()));
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
    public TheSecond second(int second) {
        final Minute copy = (Minute) this.copy();
        return new ThisSecond(IteratorUtils.getFirst(copy), second);
    }

    @Override
    public Second everySecond(IntFunction<Minute> from, int interval) {
        final Minute copy = (Minute) this.copy();
        return new EverySecond(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public CronExpression getParent() {
        return hour;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public String toCronString() {
        int fromMinute = getFromMinute();
        String str = "";
        boolean slashed = interval > 1;
        if (fromMinute == 0) {
            str = "*";
        } else if (fromMinute > 0) {
            str = String.valueOf(fromMinute);
            slashed = true;
        }
        return slashed ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
