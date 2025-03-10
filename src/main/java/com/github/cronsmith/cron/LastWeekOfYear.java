
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: LastWeekOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastWeekOfYear implements LastWeek {

    private static final long serialVersionUID = 6390083273112427117L;
    private Year year;
    private LocalDateTime week;
    private boolean self;

    LastWeekOfYear(Year year) {
        this.year = year;
        this.week = year.getTime().with(WeekFields.ISO.weekOfYear(), year.getWeekCountOfYear())
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
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public boolean hasNext() {
        boolean next = self;
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                week = year.getTime().with(WeekFields.ISO.weekOfYear(), year.getWeekCountOfYear())
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
        return year.Dec();
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
