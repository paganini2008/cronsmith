
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: LastWeekOfMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastWeekOfMonth implements LastWeek {

    private static final long serialVersionUID = 2658610900522209361L;
    private Month month;
    private LocalDateTime week;
    private boolean self;

    LastWeekOfMonth(Month month) {
        this.month = month;
        this.week = month.getTime().with(WeekFields.ISO.weekOfMonth(), month.getWeekCountOfMonth())
                .with(WeekFields.ISO.dayOfWeek(), 1);
        this.self = true;
    }

    @Override
    public LocalDateTime getTime() {
        return week;
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
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public TheDayOfWeek day(int day) {
        final Week copy = (Week) this.copy();
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy), day);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, int interval) {
        final Week copy = (Week) this.copy();
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = self;
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                week = month.getTime()
                        .with(WeekFields.ISO.weekOfMonth(), month.getWeekCountOfMonth())
                        .with(WeekFields.ISO.dayOfWeek(), 1);
                next = true;
            }
        }
        return next;
    }

    @Override
    public Week next() {
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
        return "1L";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
