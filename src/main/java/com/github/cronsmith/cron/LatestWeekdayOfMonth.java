
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: LatestWeekdayOfMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LatestWeekdayOfMonth implements TheDay {

    private static final long serialVersionUID = -1669405684277881421L;
    private final Set<TagIterator> iterators = new LinkedHashSet<>();
    private Month month;
    private int index;
    private LocalDateTime day;

    LatestWeekdayOfMonth(Month month, int dayOfMonth) {
        this.month = month;
        this.iterators.add(new LatestWeekdayIterator(dayOfMonth));
        this.day = month.getTime().withDayOfMonth(month.getLatestWeekday(dayOfMonth));
    }

    @Override
    public boolean hasNext() {
        boolean next = index < iterators.size();
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                index = 0;
                iterators.forEach(i -> i.reset());
                next = true;
            }
        }
        return next && IteratorUtils.get(iterators.iterator(), index).hasNext();
    }

    @Override
    public Day next() {
        TagIterator iterator = IteratorUtils.get(iterators.iterator(), index);
        day = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        day = day.withYear(month.getYear()).withMonth(month.getMonth());
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public CronExpression getParent() {
        return month;
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
        Supplier<Boolean> supplier = () -> day.toLocalDate().compareTo(target.toLocalDate()) < 0;
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
    public TheHour hour(int hourOfDay) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hourOfDay);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public TheDay andDay(int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        this.iterators.add(new SingleValueIterator(m -> dayOfMonth));
        return this;
    }

    @Override
    public Day andLastDay(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid last day offset: " + n);
        }
        this.iterators.add(new LastDayIterator(n));
        return this;
    }

    @Override
    public TheDay andLatestWeekday(int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        this.iterators.add(new LatestWeekdayIterator(dayOfMonth));
        return this;
    }

    @Override
    public Day andLastWeekday() {
        this.iterators.add(new LastWeekdayIterator());
        return this;
    }

    @Override
    public Day toLastDay(int interval) {
        throw new UnsupportedOperationException("toLastDay");
    }

    @Override
    public Day toLastWeekday(int interval) {
        throw new UnsupportedOperationException("toLastWeekday");
    }

    @Override
    public TheDay toLatestWeekday(int dayOfMonth, int interval) {
        throw new UnsupportedOperationException("toLatestWeekday");
    }

    @Override
    public TheDay toDay(int dayOfMonth, int interval) {
        throw new UnsupportedOperationException("toDay");
    }

    @Override
    public String toCronString() {
        return iterators.stream().map(iter -> iter.toString()).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    private class LastWeekdayIterator extends SingleValueIterator {

        private static final long serialVersionUID = 4743397573090324283L;

        LastWeekdayIterator() {
            super(m -> m.getLastWeekday());
        }

        @Override
        public String getTag() {
            return "LW";
        }

    }

    private class LatestWeekdayIterator extends SingleValueIterator {

        private static final long serialVersionUID = 8781405992563059020L;

        LatestWeekdayIterator(int dayOfMonth) {
            super(m -> m.getLatestWeekday(dayOfMonth));
            this.dayOfMonth = dayOfMonth;
        }

        private final int dayOfMonth;

        @Override
        public String getTag() {
            return dayOfMonth + "W";
        }

    }

    private class LastDayIterator extends SingleValueIterator {

        private static final long serialVersionUID = -212509240909099044L;

        LastDayIterator(int n) {
            super(m -> m.getLastDay(n));
            this.n = n;
        }

        private final int n;

        @Override
        public String getTag() {
            return n > 0 ? "L-" + n : "L";
        }

    }

    private class SingleValueIterator implements TagIterator {

        private static final long serialVersionUID = -1561112766226184869L;
        private final IntFunction<Month> ifun;
        private final int value;

        SingleValueIterator(IntFunction<Month> ifun) {
            this.ifun = ifun;
            this.value = ifun.apply(month);
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
            int dayOfMonth = ifun.apply(month);
            return month.getTime().withDayOfMonth(dayOfMonth);
        }

        @Override
        public String getTag() {
            return String.valueOf(value);
        }

        @Override
        public String toString() {
            return getTag();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof SingleValueIterator)) {
                return false;
            }
            SingleValueIterator iter = (SingleValueIterator) other;
            return iter.getTag().equals(getTag());
        }

        @Override
        public int hashCode() {
            return getTag().hashCode() * 31;
        }
    }

}
