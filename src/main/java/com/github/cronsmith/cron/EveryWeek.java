package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.function.Supplier;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryWeek implements Week, Serializable {

    private static final long serialVersionUID = -6457126115562721511L;
    private Month month;
    private final DateTimeSupplier supplier;
    private final IntFunction<Month> from;
    private final IntFunction<Month> to;
    private final int interval;
    private LocalDateTime week;
    private boolean self;
    private boolean forward;
    private LocalDateTime previous;

    EveryWeek(Month month, IntFunction<Month> from, IntFunction<Month> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.from = from;
        this.to = to;
        this.supplier = getSupplier();
        this.week = supplier.get();
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private DateTimeSupplier getSupplier() {
        return () -> month.getTime().with(WeekFields.ISO.weekOfMonth(), getFromWeekOfMonth())
                .with(WeekFields.ISO.dayOfWeek(), 1).withHour(0).withMinute(0).withSecond(0);
    }

    private int getFromWeekOfMonth() {
        int fromWeekOfMonth = from.apply(month);
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(fromWeekOfMonth);
        return fromWeekOfMonth;
    }

    private int getToWeekOfMonth() {
        int toWeekOfMonth = to.apply(month);
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(toWeekOfMonth);
        return toWeekOfMonth;
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || shoudNext());
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                week = supplier.get();
                forward = previous != null && previous.compareTo(week) >= 0;
                next = true;
            }
        }
        return next;
    }

    private boolean shoudNext() {
        int weekOfMonth;
        if (month.getMonth() == week.getMonthValue()) {
            weekOfMonth = week.get(WeekFields.ISO.weekOfMonth());
        } else {
            weekOfMonth = 1;
        }
        return weekOfMonth + interval <= getToWeekOfMonth();
    }

    @Override
    public Week next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                week = week.plusWeeks(interval);
            } else {
                forward = true;
            }
        }
        previous = LocalDateTime.of(week.toLocalDate(), week.toLocalTime());
        return this;
    }

    @Override
    public int getYear() {
        return week.getYear();
    }

    @Override
    public int getMonth() {
        return week.getMonthValue();
    }

    @Override
    public int getWeek() {
        return week.get(WeekFields.ISO.weekOfMonth());
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.ISO.weekOfYear());
    }

    @Override
    public LocalDateTime getTime() {
        return week;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> week.toLocalDate().isBefore(target.toLocalDate());
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
    public TheDayOfWeek day(int day) {
        final Week copy = (Week) this.copy();
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy), day);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, IntFunction<Week> to, int interval) {
        final Week copy = (Week) this.copy();
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return "";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
