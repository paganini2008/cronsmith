
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDay
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDay implements TheDay {

    private static final long serialVersionUID = -6007054113405112202L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Month month;
    private int index;
    private LocalDateTime day;
    private int startDayFlag;

    ThisDay(Month month, int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        this.month = month;
        this.iterators.add(new SingleValueIterator(m -> dayOfMonth));
        this.day = month.getTime().withDayOfMonth(dayOfMonth);
        this.startDayFlag = dayOfMonth;
    }

    @Override
    public TheDay andDay(int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        if (startDayFlag > dayOfMonth) {
            throw new IllegalArgumentException("Must greater than day: " + startDayFlag);
        }
        this.iterators.add(new SingleValueIterator(m -> dayOfMonth));
        this.startDayFlag = dayOfMonth;
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
    public TheDay toDay(int dayOfMonth, int interval) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        if (startDayFlag >= dayOfMonth) {
            throw new IllegalArgumentException(startDayFlag + ">=" + dayOfMonth);
        }
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        final int fromDay = startDayFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromDay, m -> dayOfMonth, interval);
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(fromDay)));
        this.iterators.add(rangeIterator);
        startDayFlag = dayOfMonth;
        return this;
    }

    @Override
    public Day toLastDay(int interval) {
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(startDayFlag)));
        this.iterators.add(new ToLastDayIterator(m -> startDayFlag, interval));
        return this;
    }

    @Override
    public Day toLastWeekday(int interval) {
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(startDayFlag)));
        this.iterators.add(new ToLastWeekdayIterator(m -> startDayFlag, interval));
        return this;
    }

    @Override
    public TheDay toLatestWeekday(int dayOfMonth, int interval) {
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(startDayFlag)));
        this.iterators.add(new ToLatestWeekdayIterator(m -> startDayFlag, dayOfMonth, interval));
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return day;
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
    public TheHour hour(int hour) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hour);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, interval);
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
        return next && iterators.get(index).hasNext();
    }

    @Override
    public Day next() {
        TagIterator iterator = iterators.get(index);
        day = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        day = day.withYear(month.getYear()).withMonth(month.getMonth());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return iterators.stream().map(iter -> iter.toString()).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    private class ToLastWeekdayIterator extends ValueRangeIterator {

        private static final long serialVersionUID = -286992600993560352L;

        ToLastWeekdayIterator(IntFunction<Month> from, int interval) {
            super(from, m -> m.getLastWeekday(), interval);
        }

        @Override
        public String getTag() {
            String str = fromScalar + "-LW";
            return interval > 1 ? str + "/" + interval : str;
        }

    }

    private class ToLatestWeekdayIterator extends ValueRangeIterator {

        private static final long serialVersionUID = -6562272372696442395L;

        ToLatestWeekdayIterator(IntFunction<Month> from, int dayOfMonth, int interval) {
            super(from, m -> m.getLatestWeekday(dayOfMonth), interval);
            this.dayOfMonth = dayOfMonth;
        }

        private final int dayOfMonth;

        @Override
        public String getTag() {
            String str = fromScalar + "-" + dayOfMonth + "W";
            return interval > 1 ? str + "/" + interval : str;
        }

    }

    private class ToLastDayIterator extends ValueRangeIterator {

        private static final long serialVersionUID = -9151384314936251635L;

        ToLastDayIterator(IntFunction<Month> from, int interval) {
            super(from, m -> m.getLastDay(), interval);
        }

        @Override
        public String getTag() {
            String str = String.valueOf(fromScalar);
            return interval > 1 ? str + "/" + interval : str;
        }

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

    }

    private class ValueRangeIterator implements TagIterator {

        private static final long serialVersionUID = -2056254572492151394L;

        ValueRangeIterator(IntFunction<Month> from, IntFunction<Month> to, int interval) {
            this.from = from;
            this.to = to;
            this.fromScalar = from.apply(month);
            this.toScalar = to.apply(month);
            this.interval = interval;
            reset();
        }

        private final IntFunction<Month> from;
        private final IntFunction<Month> to;
        protected final int fromScalar;
        protected final int toScalar;
        protected final int interval;
        private boolean self;

        private LocalDateTime ldt;

        @Override
        public void reset() {
            int from = this.from.apply(month);
            this.ldt = month.getTime().withDayOfMonth(from);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getDayOfMonth() + interval <= to.apply(month);
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            } else {
                ldt = ldt.plusDays(interval);
            }
            return ldt;
        }

        @Override
        public String getTag() {
            String str = fromScalar + "-" + toScalar;
            return interval > 1 ? str + "/" + interval : str;
        }

        @Override
        public String toString() {
            return getTag();
        }

    }

}
