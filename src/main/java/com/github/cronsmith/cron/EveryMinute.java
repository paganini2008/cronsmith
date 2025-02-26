package com.github.cronsmith.cron;

import java.io.Serializable;
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
public class EveryMinute implements Minute, Serializable {

    private static final long serialVersionUID = -7939881133025374416L;
    private Hour hour;
    private LocalDateTime minute;
    private final IntFunction<Hour> from;
    private final IntFunction<Hour> to;
    private final DateTimeSupplier supplier;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryMinute(Hour hour, IntFunction<Hour> from, IntFunction<Hour> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.hour = hour;
        this.from = from;
        this.to = to;
        this.supplier = getSupplier();
        this.minute = supplier.get();
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private DateTimeSupplier getSupplier() {
        return () -> hour.getTime().withMinute(getFromMinute()).withSecond(0);
    }

    private int getFromMinute() {
        int fromMinute = from.apply(hour);
        ChronoField.MINUTE_OF_HOUR.checkValidValue(fromMinute);
        return fromMinute;
    }

    private int getToMinute() {
        int toMinute = to.apply(hour);
        ChronoField.MINUTE_OF_HOUR.checkValidValue(toMinute);
        return toMinute;
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || minute.getMinute() + interval <= getToMinute());
        if (!next) {
            if (hour.hasNext()) {
                hour = hour.next();
                minute = supplier.get();
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
    public Second everySecond(IntFunction<Minute> from, IntFunction<Minute> to, int interval) {
        final Minute copy = (Minute) this.copy();
        return new EverySecond(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return hour;
    }

    @Override
    public String toCronString() {
        int fromMinute = getFromMinute();
        int toMinute = getToMinute();
        String str;
        boolean slashed = false;
        if (fromMinute == 0 && toMinute == 59) {
            str = "*";
        } else if (fromMinute > 0 && toMinute == 59) {
            str = String.valueOf(fromMinute);
            slashed = true;
        } else {
            str = fromMinute + "-" + toMinute;
        }
        return interval > 1 ? str + "/" + interval : slashed ? str + "/1" : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
