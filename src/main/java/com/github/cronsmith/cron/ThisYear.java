
package com.github.cronsmith.cron;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
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
public class ThisYear implements TheYear {

    private static final long serialVersionUID = -5316436238766770045L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private CronBuilder builder;
    private LocalDateTime year;
    private int index;
    private int startYearFlag;

    ThisYear(CronBuilder builder, int year) {
        if (year > MAX_YEAR) {
            throw new IllegalArgumentException("Great than the maximum year: " + MAX_YEAR);
        }
        if (year < builder.getStartTime().getYear()) {
            throw new IllegalArgumentException(
                    "Less than the minimum year: " + builder.getStartTime().getYear());
        }
        this.builder = builder;
        this.iterators.add(new SingleValueIterator(b -> year));
        this.year = builder.getTime().withYear(year);
        this.startYearFlag = year;
    }

    @Override
    public TheYear andYear(int year) {
        ChronoField.YEAR.checkValidValue(year);
        this.iterators.add(new SingleValueIterator(b -> year));
        this.startYearFlag = year;
        return this;
    }

    @Override
    public TheYear toYear(int year, int interval) {
        ChronoField.YEAR.checkValidValue(year);
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (startYearFlag >= year) {
            throw new IllegalArgumentException(startYearFlag + ">=" + year);
        }
        final int fromYear = startYearFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(b -> fromYear, b -> year, interval);
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(fromYear)));
        this.iterators.add(rangeIterator);
        this.startYearFlag = year;
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
    public Month everyMonth(IntFunction<Year> from, int interval) {
        final Year copy = (Year) this.copy();
        return new EveryMonth(IteratorUtils.getFirst(copy), from, interval);
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
        return index < iterators.size();
    }

    @Override
    public Year next() {
        TagIterator iterator = iterators.get(index);
        year = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
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
        return iterators.stream().map(iter -> iter.toString()).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    private class SingleValueIterator implements TagIterator {

        private static final long serialVersionUID = -1561112766226184869L;
        private final IntFunction<CronBuilder> ifun;
        private final int value;

        SingleValueIterator(IntFunction<CronBuilder> ifun) {
            this.ifun = ifun;
            this.value = ifun.apply(builder);
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
            int year = ifun.apply(builder);
            return builder.getTime().withYear(year);
        }

        @Override
        public String getTag() {
            return String.valueOf(value);
        }

        @Override
        public String toString() {
            return getTag();
        }

    }

    private class ValueRangeIterator implements TagIterator {

        private static final long serialVersionUID = -2056254572492151394L;

        ValueRangeIterator(IntFunction<CronBuilder> from, IntFunction<CronBuilder> to,
                int interval) {
            this.from = from;
            this.to = to;
            this.fromScalar = from.apply(builder);
            this.toScalar = to.apply(builder);
            this.interval = interval;
            reset();
        }

        private final IntFunction<CronBuilder> from;
        private final IntFunction<CronBuilder> to;
        protected final int fromScalar;
        protected final int toScalar;
        protected final int interval;
        private boolean self;

        private LocalDateTime ldt;

        @Override
        public void reset() {
            this.ldt = builder.getTime().withYear(from.apply(builder));
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getYear() + interval <= to.apply(builder);
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            } else {
                ldt = ldt.plusYears(interval);
            }
            return ldt;
        }

        @Override
        public String getTag() {
            String str;
            boolean slashed = interval > 1;
            if (fromScalar >= getBuilder().getStartTime().getYear() && toScalar == MAX_YEAR) {
                str = String.valueOf(fromScalar);
                slashed = true;
            } else {
                str = fromScalar + "-" + toScalar;
            }
            return slashed ? str + "/" + interval : str;
        }

        @Override
        public String toString() {
            return getTag();
        }

    }

}
