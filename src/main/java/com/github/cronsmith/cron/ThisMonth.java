
package com.github.cronsmith.cron;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
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
public class ThisMonth implements TheMonth {

    private static final long serialVersionUID = 229203112866380942L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Year year;
    private int index;
    private LocalDateTime month;
    private int startMonthFlag;

    ThisMonth(Year year, int month) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        this.year = year;
        this.iterators.add(new SingleValueIterator(y -> month));
        this.month = year.getTime().withMonth(month);
        this.startMonthFlag = month;
    }

    @Override
    public TheMonth andMonth(int month) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        if (startMonthFlag > month) {
            throw new IllegalArgumentException("Must greater than day: " + startMonthFlag);
        }
        this.iterators.add(new SingleValueIterator(y -> month));
        this.startMonthFlag = month;
        return this;
    }

    @Override
    public TheMonth toMonth(int month, int interval) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(month);
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (startMonthFlag >= month) {
            throw new IllegalArgumentException(startMonthFlag + ">=" + month);
        }
        final int fromMonth = startMonthFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromMonth, m -> month, interval);
        this.iterators.removeIf(iter -> iter.toString()
                .equals(getBuilder().isUseMonthAsNumber() ? String.valueOf(fromMonth)
                        : AbbreviationUtils.getMonthName(fromMonth)));
        this.iterators.add(rangeIterator);
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
    public TheDay latestWeekday(int dayOfMonth) {
        final Month copy = (Month) this.copy();
        return new LatestWeekdayOfMonth(IteratorUtils.getFirst(copy), dayOfMonth);
    }

    @Override
    public Day lastWeekday() {
        final Month copy = (Month) this.copy();
        return new LastWeekdayOfMonth(IteratorUtils.getFirst(copy));
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
    public Day everyDay(IntFunction<Month> from, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryDay(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public TheWeek week(int week) {
        final Month copy = (Month) this.copy();
        return new ThisWeek(IteratorUtils.getFirst(copy), week);
    }

    @Override
    public TheDayOfWeekInMonth dayOfWeek(int weekOfMonth, int dayOfWeek) {
        final Month copy = (Month) this.copy();
        return new ThisDayOfWeekInMonth(IteratorUtils.getFirst(copy), weekOfMonth, dayOfWeek);
    }

    @Override
    public Week lastWeek() {
        final Month copy = (Month) this.copy();
        return new LastWeekOfMonth(IteratorUtils.getFirst(copy));
    }

    @Override
    public Week everyWeek(IntFunction<Month> from, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryWeek(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < iterators.size();
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                index = 0;
                iterators.forEach(i -> i.reset());
                next = true;
            }
        }
        return next && iterators.get(index).hasNext();
    }

    @Override
    public Month next() {
        TagIterator iterator = iterators.get(index);
        month = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        month = month.withYear(year.getYear());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public String toCronString() {
        return iterators.stream().map(iter -> iter.toString()).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    private class SingleValueIterator implements TagIterator {

        private static final long serialVersionUID = -1561112766226184869L;
        private final IntFunction<Year> ifun;
        private final int value;

        SingleValueIterator(IntFunction<Year> ifun) {
            this.ifun = ifun;
            this.value = ifun.apply(year);
            this.self = true;
        }

        private boolean self;

        @Override
        public void reset() {
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self;
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            }
            int month = ifun.apply(year);
            return year.getTime().withMonth(month);
        }

        @Override
        public String getTag() {
            return getBuilder().isUseMonthAsNumber() ? String.valueOf(value)
                    : AbbreviationUtils.getMonthName(value);
        }

        @Override
        public String toString() {
            return getTag();
        }
    }

    private class ValueRangeIterator implements TagIterator {

        private static final long serialVersionUID = -2056254572492151394L;

        ValueRangeIterator(IntFunction<Year> from, IntFunction<Year> to, int interval) {
            this.from = from;
            this.to = to;
            this.fromScalar = from.apply(year);
            this.toScalar = to.apply(year);
            this.interval = interval;
            reset();
        }

        private final IntFunction<Year> from;
        private final IntFunction<Year> to;
        protected final int fromScalar;
        protected final int toScalar;
        protected final int interval;
        private boolean self;

        private LocalDateTime ldt;

        @Override
        public void reset() {
            this.ldt = year.getTime().withMonth(from.apply(year));
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getMonthValue() + interval <= to.apply(year);
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            } else {
                ldt = ldt.plusMonths(interval);
            }
            return ldt;
        }

        @Override
        public String getTag() {
            boolean numberFlag = getBuilder().isUseMonthAsNumber();
            String str = (numberFlag ? String.valueOf(fromScalar)
                    : AbbreviationUtils.getMonthName(fromScalar)) + "-"
                    + (numberFlag ? String.valueOf(toScalar)
                            : AbbreviationUtils.getMonthName(toScalar));
            return interval > 1 ? str + "/" + interval : str;
        }

        @Override
        public String toString() {
            return getTag();
        }

    }

}
