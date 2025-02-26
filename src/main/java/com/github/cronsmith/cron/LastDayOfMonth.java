
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: LastDayOfMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastDayOfMonth implements Day, Serializable {

    private static final long serialVersionUID = 3379984313144390130L;

    private Month month;
    private final int n;
    private final DateTimeSupplier supplier;
    private LocalDateTime day;
    private boolean self;

    LastDayOfMonth(Month month, int n) {
        this.month = month;
        this.n = n;
        this.supplier = getSupplier();
        this.day = supplier.get();
        this.self = true;
    }

    private DateTimeSupplier getSupplier() {
        return () -> month.getTime().withDayOfMonth(month.getLastDay(n)).withHour(0).withMinute(0)
                .withSecond(0);
    }

    @Override
    public LocalDateTime getTime() {
        return day;
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
    public boolean hasNext() {
        boolean next = self;
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                day = supplier.get();
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
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        if (n > 0) {
            return "L-" + n;
        }
        return "L";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
