
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisMonth
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class ThisMonth implements TheMonth, Serializable {

    private static final long serialVersionUID = 229203112866380942L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Year year;
    private int index;
    private LocalDateTime month;
    private int lastMonthFlag;
    private final List<Range<Object>> ranges = new ArrayList<>();

    ThisMonth(Year year, int month) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        this.year = year;
        DateTimeSupplier supplier = () -> year.getTime().withMonth(month);
        this.siblings.put(month, supplier);
        this.month = supplier.get();
        this.lastMonthFlag = month;
        this.ranges.add(new MonthRange(month));
    }

    @Override
    public TheMonth andMonth(int month) {
        this.ranges.add(new MonthRange(month));
        return doAndMonth(month);
    }

    private TheMonth doAndMonth(int month) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        DateTimeSupplier supplier = () -> year.getTime().withMonth(month);
        this.siblings.put(month, supplier);
        this.lastMonthFlag = month;
        return this;
    }

    @Override
    public TheMonth toMonth(int month, int interval) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (lastMonthFlag >= month) {
            throw new IllegalArgumentException(lastMonthFlag + ">=" + month);
        }
        for (int i = lastMonthFlag + interval; i <= month; i += interval) {
            doAndMonth(i);
        }
        this.ranges.get(this.ranges.size() - 1).setTo(month).setInterval(interval);
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return month;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> month.toLocalDate().isBefore(target.toLocalDate());
        if (supplier.get()) {
            while (supplier.get()) {
                if (hasNext()) {
                    next();
                } else {
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public int getYear() {
        return month.getYear();
    }

    @Override
    public int getMonth() {
        return month.getMonthValue();
    }

    @Override
    public int getLastDay(int n) {
        int lastDayOfMonth = month.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        lastDayOfMonth -= n;
        return lastDayOfMonth;
    }

    @Override
    public int getLastWeekday() {
        return getLatestWeekday(getLastDay());
    }

    @Override
    public Day latestWeekday(int dayOfMonth) {
        return new LatestWeekdayOfMonth(this, dayOfMonth);
    }

    @Override
    public int getLatestWeekday(int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        LocalDateTime ldt = month.withDayOfMonth(dayOfMonth);
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
        return nextDay.getDayOfMonth();
    }

    @Override
    public int getWeekCountOfMonth() {
        return month.with(TemporalAdjusters.lastDayOfMonth()).get(WeekFields.ISO.weekOfMonth());
    }

    @Override
    public TheDay day(int day) {
        final Month copy = (Month) this.copy();
        return new ThisDay(IteratorUtils.getFirst(copy), day);
    }

    @Override
    public Day lastDay(int n) {
        final Month copy = (Month) this.copy();
        return new LastDayOfMonth(IteratorUtils.getFirst(copy), n);
    }

    @Override
    public Day everyDay(IntFunction<Month> from, IntFunction<Month> to, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryDay(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public TheWeek week(int week) {
        final Month copy = (Month) this.copy();
        return new ThisWeek(IteratorUtils.getFirst(copy), week);
    }

    @Override
    public TheDayOfWeekInMonth dayOfWeek(int week, int dayOfWeek) {
        final Month copy = (Month) this.copy();
        return new ThisDayOfWeekInMonth(IteratorUtils.getFirst(copy), week, dayOfWeek);
    }

    @Override
    public Week lastWeek() {
        final Month copy = (Month) this.copy();
        return new LastWeekOfMonth(IteratorUtils.getFirst(copy));
    }

    @Override
    public Week everyWeek(IntFunction<Month> from, IntFunction<Month> to, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryWeek(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Month next() {
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        month = supplier.get();
        month = month.withYear(year.getYear());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public String toCronString() {
        return ranges.stream().map(Range::toString).collect(Collectors.joining(","));
    }

    class MonthRange extends Range<Object> {

        private static final long serialVersionUID = -71288722336240277L;

        MonthRange(Object from) {
            super(from);
        }

        public String getFrom() {
            int from = (Integer) super.getFrom();
            return getBuilder().isUseMonthAsNumber() ? String.valueOf(from)
                    : AbbreviationUtils.getMonthName(from);
        }

        public String getTo() {
            if (super.getTo() != null) {
                int to = (Integer) super.getTo();
                return getBuilder().isUseMonthAsNumber() ? String.valueOf(to)
                        : AbbreviationUtils.getMonthName(to);
            }
            return null;
        }

    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    public static void main(String[] args) {
        TheYear singleYear = new CronBuilder().year(2025);
        singleYear = singleYear.andYear(2030).andYear(2028);
        TheMonth singleMonth = singleYear.July().andAug().andMonth(11);
        CronExpression cronExpression = singleMonth.lastWeek().Tues().andWed().toSun().everyHour(2)
                .minute(10).toMinute(50, 3).second(6);
        System.out.println(cronExpression);
    }

}
