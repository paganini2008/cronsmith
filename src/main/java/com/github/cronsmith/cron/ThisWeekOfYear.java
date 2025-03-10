package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisWeekOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisWeekOfYear implements TheWeek {

    private static final long serialVersionUID = -3294283555586718358L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Year year;
    private int index;
    private LocalDateTime week;
    private int startWeekFlag;

    ThisWeekOfYear(Year year, int weekOfYear) {
        ChronoField.ALIGNED_WEEK_OF_YEAR.checkValidValue(weekOfYear);
        this.year = year;
        this.iterators.add(new SingleValueIterator(m -> weekOfYear));
        this.week = year.getTime().with(WeekFields.ISO.weekOfYear(), weekOfYear)
                .with(WeekFields.ISO.dayOfWeek(), 1);
        this.startWeekFlag = weekOfYear;
    }

    @Override
    public TheWeek andWeek(int weekOfYear) {
        ChronoField.ALIGNED_WEEK_OF_YEAR.checkValidValue(weekOfYear);
        if (startWeekFlag > weekOfYear) {
            throw new IllegalArgumentException("Must greater than day: " + startWeekFlag);
        }
        this.iterators.add(new SingleValueIterator(y -> weekOfYear));
        this.startWeekFlag = weekOfYear;
        return this;
    }

    @Override
    public Week andLastWeek() {
        this.iterators.add(new LastWeekIterator());
        return this;
    }

    @Override
    public TheWeek toWeek(int weekOfYear, int interval) {
        ChronoField.ALIGNED_WEEK_OF_YEAR.checkValidValue(weekOfYear);
        if (startWeekFlag >= weekOfYear) {
            throw new IllegalArgumentException(startWeekFlag + ">=" + weekOfYear);
        }
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        final int fromWeek = startWeekFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromWeek, m -> weekOfYear, interval);
        this.iterators.removeIf(iter -> iter.toString()
                .equals(getBuilder().isUseDayOfWeekAsNumber() ? "1#" + fromWeek
                        : AbbreviationUtils.getDayOfWeekName(1) + "#" + fromWeek));
        this.iterators.add(rangeIterator);
        this.startWeekFlag = weekOfYear;
        return this;
    }

    @Override
    public Week toLastWeek(int interval) {
        this.iterators.removeIf(iter -> iter.toString()
                .equals(String.valueOf(getBuilder().isUseDayOfWeekAsNumber() ? "1#" + startWeekFlag
                        : AbbreviationUtils.getDayOfWeekName(1) + "#" + startWeekFlag)));
        this.iterators.add(new ToLastWeekIterator(m -> startWeekFlag, interval));
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return week;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> week.toLocalDate().isBefore(target.toLocalDate());
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
    public TheDayOfWeek day(int dayOfWeek) {
        final Week copy = (Week) this;
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy), dayOfWeek);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, int interval) {
        final Week copy = (Week) this;
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy), from, interval);
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
    public Week next() {
        TagIterator iterator = iterators.get(index);
        week = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        week.withYear(year.getYear());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public boolean supportCronString() {
        return false;
    }

    private class SingleValueIterator implements TagIterator {

        private static final long serialVersionUID = -1561112766226184869L;
        private final IntFunction<Year> ifun;
        protected final int value;

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
            int weekOfYear = ifun.apply(year);
            return year.getTime().with(WeekFields.ISO.weekOfYear(), weekOfYear)
                    .with(WeekFields.ISO.dayOfWeek(), 1);
        }

        @Override
        public String getTag() {
            return getBuilder().isUseDayOfWeekAsNumber() ? "1#" + value
                    : AbbreviationUtils.getDayOfWeekName(1) + "#" + value;
        }

        @Override
        public String toString() {
            return getTag();
        }
    }

    private class LastWeekIterator extends SingleValueIterator {

        private static final long serialVersionUID = -2489263625054251845L;

        LastWeekIterator() {
            super(y -> y.getWeekCountOfYear());
        }

        @Override
        public String getTag() {
            return "1L";
        }

    }

    private class ToLastWeekIterator extends ValueRangeIterator {

        private static final long serialVersionUID = -170983890631725878L;

        ToLastWeekIterator(IntFunction<Year> from, int interval) {
            super(from, y -> y.getWeekCountOfYear(), interval);
        }

        @Override
        public String getTag() {
            boolean flag = getBuilder().isUseDayOfWeekAsNumber();
            List<String> list = new ArrayList<String>();
            for (int i = fromScalar; i < toScalar; i += interval) {
                list.add(flag ? "1#" + i : AbbreviationUtils.getDayOfWeekName(1) + "#" + i);
            }
            list.add("1L");
            return String.join(",", list);
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
            int from = this.from.apply(year);
            this.ldt = year.getTime().with(WeekFields.ISO.weekOfYear(), from)
                    .with(WeekFields.ISO.dayOfWeek(), 1);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.get(WeekFields.ISO.weekOfYear()) + interval <= to.apply(year);
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            } else {
                ldt = ldt.plusWeeks(interval);
            }
            return ldt;
        }

        @Override
        public String getTag() {
            boolean flag = getBuilder().isUseDayOfWeekAsNumber();
            List<String> list = new ArrayList<String>();
            for (int i = fromScalar; i <= toScalar; i += interval) {
                list.add(flag ? "1#" + i : AbbreviationUtils.getDayOfWeekName(1) + "#" + i);
            }
            return String.join(",", list);
        }

        @Override
        public String toString() {
            return getTag();
        }

    }

}
