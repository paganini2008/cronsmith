
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
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
 * @Description: ThisWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisWeek implements TheWeek {

    private static final long serialVersionUID = -4563991137870265612L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Month month;
    private int index;
    private LocalDateTime week;
    private int startWeekFlag;

    ThisWeek(Month month, int weekOfMonth) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        this.month = month;
        this.iterators.add(new SingleValueIterator(m -> weekOfMonth));
        this.week = month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth)
                .with(WeekFields.ISO.dayOfWeek(), 1);
        this.startWeekFlag = weekOfMonth;
    }

    @Override
    public ThisWeek andWeek(int weekOfMonth) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        if (startWeekFlag > weekOfMonth) {
            throw new IllegalArgumentException("Must greater than day: " + startWeekFlag);
        }
        this.iterators.add(new SingleValueIterator(y -> weekOfMonth));
        this.startWeekFlag = weekOfMonth;
        return this;
    }

    @Override
    public Week andLastWeek() {
        this.iterators.add(new LastWeekIterator());
        return this;
    }

    @Override
    public ThisWeek toWeek(int weekOfMonth, int interval) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        if (startWeekFlag >= weekOfMonth) {
            throw new IllegalArgumentException(startWeekFlag + ">=" + weekOfMonth);
        }
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        final int fromWeek = startWeekFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromWeek, m -> weekOfMonth, interval);
        this.iterators.removeIf(iter -> iter.toString()
                .equals(getBuilder().isUseDayOfWeekAsNumber() ? "1#" + fromWeek
                        : AbbreviationUtils.getDayOfWeekName(1) + "#" + fromWeek));
        this.iterators.add(rangeIterator);
        this.startWeekFlag = weekOfMonth;
        return this;
    }

    @Override
    public Week toLastWeek(int interval) {
        this.iterators.removeIf(iter -> iter.toString()
                .equals(getBuilder().isUseDayOfWeekAsNumber() ? "1#" + startWeekFlag
                        : AbbreviationUtils.getDayOfWeekName(1) + "#" + startWeekFlag));
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
    public Week next() {
        TagIterator iterator = iterators.get(index);
        week = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        week = week.withYear(month.getYear()).withMonth(month.getMonth());
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

    private class LastWeekIterator extends SingleValueIterator {

        private static final long serialVersionUID = -6391926646443858080L;

        LastWeekIterator() {
            super(m -> m.getWeekCountOfMonth());
        }

        @Override
        public String getTag() {
            return "1L";
        }

    }

    private class SingleValueIterator implements TagIterator {

        private static final long serialVersionUID = -1561112766226184869L;
        private final IntFunction<Month> ifun;
        protected final int value;

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
            int weekOfMonth = ifun.apply(month);
            return month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth)
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

    private class ToLastWeekIterator extends ValueRangeIterator {

        private static final long serialVersionUID = 2559764881040612498L;

        ToLastWeekIterator(IntFunction<Month> from, int interval) {
            super(from, m -> m.getWeekCountOfMonth(), interval);
        }

        @Override
        public String getTag() {
            boolean numberFlag = getBuilder().isUseDayOfWeekAsNumber();
            List<String> list = new ArrayList<String>();
            for (int i = fromScalar; i < toScalar; i += interval) {
                list.add(numberFlag ? "1#" + i : AbbreviationUtils.getDayOfWeekName(1) + "#" + i);
            }
            list.add("1L");
            return String.join(",", list);
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
            this.ldt = month.getTime().with(WeekFields.ISO.weekOfMonth(), from)
                    .with(WeekFields.ISO.dayOfWeek(), 1);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.get(WeekFields.ISO.weekOfMonth()) + interval <= to.apply(month);
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
            boolean numberFlag = getBuilder().isUseDayOfWeekAsNumber();
            List<String> list = new ArrayList<String>();
            for (int i = fromScalar; i <= toScalar; i += interval) {
                list.add(numberFlag ? "1#" + i : AbbreviationUtils.getDayOfWeekName(1) + "#" + i);
            }
            return String.join(",", list);
        }

        @Override
        public String toString() {
            return getTag();
        }
    }

}
