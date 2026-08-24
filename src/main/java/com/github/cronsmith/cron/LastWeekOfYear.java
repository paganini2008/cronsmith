
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.List;
import com.github.cronsmith.CRON;
import com.github.cronsmith.utils.IteratorUtils;
/**
 * 
 * @Description: LastWeekOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastWeekOfYear implements LastWeek, WeekOrdinal {

    private static final long serialVersionUID = 6390083273112427118L;
    private Year year;
    private LocalDateTime week;
    private boolean self;

    LastWeekOfYear(Year year) {
        this.year = year;
        this.week = lastWeekOfDecember(year);
        this.self = true;
    }

    /**
     * The last week of a year is the last week of its December. Anchoring on December rather than
     * on ISO week 52/53 keeps it inside the year, and lets the day-of-week below it render as the
     * ordinary {@code L} tag.
     */
    private static LocalDateTime lastWeekOfDecember(Year year) {
        return WeekOfMonth.startOf(year.getTime().withMonth(12).withDayOfMonth(1),
                WeekOfMonth.LAST);
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
        return week.get(WeekFields.ISO.weekOfMonth());
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.ISO.weekOfYear());
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
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public boolean hasNext() {
        boolean next = self;
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                week = lastWeekOfDecember(year);
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
        return "L";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }
}
