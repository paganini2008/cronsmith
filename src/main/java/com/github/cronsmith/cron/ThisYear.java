
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisYear implements TheYear, Serializable {

    private static final long serialVersionUID = -5316436238766770045L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private CronBuilder builder;
    private LocalDateTime year;
    private int index;
    private int lastYear;
    private final List<Range<Integer>> ranges = new ArrayList<>();

    public ThisYear(CronBuilder builder, int year) {
        if (year > MAX_YEAR) {
            throw new IllegalArgumentException("Exceed the max year: " + MAX_YEAR);
        }
        this.builder = builder;
        DateTimeSupplier supplier = () -> builder.getTime().withYear(year);
        this.siblings.put(year, supplier);
        this.year = supplier.get();
        this.lastYear = year;
        this.ranges.add(new YearRange(year));
    }

    @Override
    public TheYear andYear(int year) {
        this.ranges.add(new YearRange(year));
        return doAndYear(year);
    }

    private TheYear doAndYear(int year) {
        ChronoField.YEAR.checkValidValue(year);
        DateTimeSupplier supplier = () -> builder.getTime().withYear(year);
        siblings.put(year, supplier);
        this.lastYear = year;
        return this;
    }

    @Override
    public TheYear toYear(int year, int interval) {
        ChronoField.YEAR.checkValidValue(year);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        for (int i = lastYear + interval; i <= year; i += interval) {
            doAndYear(i);
        }
        this.ranges.get(ranges.size() - 1).setTo(year).setInterval(interval);
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return year;
    }

    @Override
    public int getYear() {
        return year.getYear();
    }

    @Override
    public int getWeekCountOfYear() {
        return year.with(TemporalAdjusters.lastDayOfYear()).get(WeekFields.ISO.weekOfYear());
    }

    @Override
    public int getLastDayOfYear(int n) {
        int lastDayOfYear = year.with(TemporalAdjusters.lastDayOfYear()).getDayOfYear();
        lastDayOfYear -= n;
        return lastDayOfYear;
    }

    @Override
    public Month everyMonth(IntFunction<Year> from, IntFunction<Year> to, int interval) {
        final Year copy = (Year) this.copy();
        return new EveryMonth(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public int getLatestWeekdayOfYear(int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        LocalDateTime ldt = year.withDayOfYear(dayOfYear);
        LocalDateTime nextDay;
        if (ldt.getDayOfWeek() == DayOfWeek.SATURDAY) {
            nextDay = ldt.minusDays(1);
            if (nextDay.getMonthValue() != ldt.getMonthValue()) {
                nextDay = ldt.plusDays(2);
            }
        } else if (ldt.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = ldt.plusDays(1);
            if (nextDay.getMonthValue() != ldt.getMonthValue()) {
                nextDay = ldt.minusDays(2);
            }
        } else {
            nextDay = ldt;
        }
        return nextDay.getDayOfYear();
    }

    @Override
    public TheDay day(int day) {
        final Year copy = (Year) this.copy();
        return new ThisDayOfYear(IteratorUtils.getFirst(copy), day);
    }

    @Override
    public TheWeek week(int week) {
        final Year copy = (Year) this.copy();
        return new ThisWeekOfYear(IteratorUtils.getFirst(copy), week);
    }

    @Override
    public TheMonth month(int month) {
        final Year copy = (Year) this.copy();
        return new ThisMonth(IteratorUtils.getFirst(copy), month);
    }

    @Override
    public Week lastWeek() {
        final Year copy = (Year) this.copy();
        return new LastWeekOfYear(IteratorUtils.getFirst(copy));
    }

    @Override
    public boolean hasNext() {
        return index < siblings.size();
    }

    @Override
    public Year next() {
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        year = supplier.get();
        return this;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public CronExpression getParent() {
        return builder;
    }

    @Override
    public String toCronString() {
        return ranges.stream().map(Range::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    static class YearRange extends Range<Integer> {

        public YearRange(Integer from) {
            super(from);
        }

        public Integer getTo() {
            if (Integer.valueOf(Year.MAX_YEAR).equals(super.getTo())) {
                return null;
            }
            return super.getTo();
        }

        private static final long serialVersionUID = -7908364182076275624L;

    }

}
