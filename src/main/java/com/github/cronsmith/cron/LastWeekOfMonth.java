package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.List;

/**
 *
 * The last week of a month, parent of the {@code L} day-of-week tag: {@code FRIL} is the last
 * Friday of the month, whichever of the last seven days that turns out to be.
 *
 * @Description: LastWeekOfMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastWeekOfMonth implements LastWeek, WeekOrdinal, PendingValueHolder {

    private static final long serialVersionUID = 2658610900522209362L;
    private Month month;
    private LocalDateTime week;
    private boolean self;

    LastWeekOfMonth(Month month) {
        this.month = month;
        this.week = WeekOfMonth.startOf(month.getTime(), WeekOfMonth.LAST);
        this.self = true;
    }

    @Override
    public int currentOrdinal() {
        return WeekOfMonth.LAST;
    }

    @Override
    public List<Integer> ordinals() {
        return Collections.singletonList(WeekOfMonth.LAST);
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
        return WeekOfMonth.ordinalOf(week.toLocalDate());
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.ISO.weekOfYear());
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        boolean moved = false;
        while (week.toLocalDate().isBefore(target.toLocalDate()) && hasNext()) {
            next();
            moved = true;
        }
        if (moved) {
            self = true;
        }
        return this;
    }

    @Override
    public TheDayOfWeek day(int day) {
        final Week copy = (Week) this.copy();
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy, copy), day);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, int interval) {
        final Week copy = (Week) this.copy();
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy, copy), from, interval);
    }

    @Override
    public boolean hasNext() {
        return self || month.hasNext();
    }

    @Override
    public Week next() {
        if (self) {
            self = false;
        } else {
            month = month.next();
            week = WeekOfMonth.startOf(month.getTime(), WeekOfMonth.LAST);
        }
        return this;
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return "L";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }


    @Override
    public void takePendingValue() {
        this.self = false;
    }
}
