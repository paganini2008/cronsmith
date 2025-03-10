
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfWeek implements TheDayOfWeek {

    private static final long serialVersionUID = -5353496894925284106L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Week week;
    private int index;
    private LocalDateTime day;
    private int startDayOfWeekFlag;

    ThisDayOfWeek(Week week, int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.week = week;
        this.iterators.add(new SingleValueIterator(m -> dayOfWeek));
        this.day = week.getTime().with(WeekFields.ISO.weekOfMonth(), week.getWeek())
                .with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        this.startDayOfWeekFlag = dayOfWeek;
    }

    @Override
    public TheDayOfWeek andDay(int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.iterators.add(new SingleValueIterator(m -> dayOfWeek));
        this.startDayOfWeekFlag = dayOfWeek;
        return this;
    }

    @Override
    public TheDayOfWeek toDay(int dayOfWeek, int interval) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        if (startDayOfWeekFlag >= dayOfWeek) {
            throw new IllegalArgumentException(startDayOfWeekFlag + ">=" + dayOfWeek);
        }
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        final int fromDay = startDayOfWeekFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromDay, m -> dayOfWeek, interval);
        this.iterators.removeIf(iter -> iter.toString().equals(getDayOfWeekName(fromDay)));
        this.iterators.add(rangeIterator);
        this.startDayOfWeekFlag = dayOfWeek;
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
            if (week.hasNext()) {
                week = week.next();
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
        return this;
    }

    @Override
    public CronExpression getParent() {
        return week;
    }

    @Override
    public String toCronString() {
        return iterators.stream().map(iter -> iter.toString()).collect(Collectors.joining(","));
    }

    private String getDayOfWeekName(int dayOfWeek) {
        String repr = getBuilder().isUseDayOfWeekAsNumber() ? String.valueOf(dayOfWeek)
                : AbbreviationUtils.getDayOfWeekName(dayOfWeek);
        if (week instanceof LastWeek) {
            return dayOfWeek + "L";
        } else if (week instanceof TheWeek) {
            return repr + "#" + week.getWeek();
        } else if (week instanceof IntervalChronoUnit) {
            int from = ((IntervalChronoUnit) week).getFrom();
            int interval = ((IntervalChronoUnit) week).getInterval();
            if (interval == 1) {
                return repr;
            }
            return IntStream.range(from + (interval - 1), 5)
                    .filter(i -> (i + interval) % interval == 0).mapToObj(i -> repr + "#" + i)
                    .collect(Collectors.joining(","));
        }
        return repr;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    private class SingleValueIterator implements TagIterator {

        private static final long serialVersionUID = -1561112766226184869L;
        private final IntFunction<Week> ifun;
        private final int value;

        SingleValueIterator(IntFunction<Week> ifun) {
            this.ifun = ifun;
            this.value = ifun.apply(week);
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
            int dayOfWeek = ifun.apply(week);
            return week.getTime().with(WeekFields.ISO.weekOfMonth(), week.getWeek())
                    .with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        }

        @Override
        public String getTag() {
            return getDayOfWeekName(value);
        }

        @Override
        public String toString() {
            return getTag();
        }

    }

    private class ValueRangeIterator implements TagIterator {

        private static final long serialVersionUID = -2056254572492151394L;

        ValueRangeIterator(IntFunction<Week> from, IntFunction<Week> to, int interval) {
            this.from = from;
            this.to = to;
            this.fromScalar = from.apply(week);
            this.toScalar = to.apply(week);
            this.interval = interval;
            reset();
        }

        private final IntFunction<Week> from;
        private final IntFunction<Week> to;
        protected final int fromScalar;
        protected final int toScalar;
        protected final int interval;
        private boolean self;

        private LocalDateTime ldt;

        @Override
        public void reset() {
            int from = this.from.apply(week);
            this.ldt = week.getTime().with(WeekFields.ISO.weekOfMonth(), week.getWeek())
                    .with(WeekFields.ISO.dayOfWeek(), from);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getDayOfWeek().getValue() + interval <= to.apply(week);
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
            boolean numberFlag = getBuilder().isUseDayOfWeekAsNumber();
            if (week instanceof LastWeek) {
                return IntStream.range(fromScalar, toScalar)
                        .filter(i -> (i - fromScalar) % interval == 0).mapToObj(i -> i + "L")
                        .collect(Collectors.joining(","));
            } else if (week instanceof TheWeek) {
                return IntStream.range(fromScalar, toScalar)
                        .filter(i -> (i - fromScalar) % interval == 0)
                        .mapToObj(i -> numberFlag ? i + "#" + week.getWeek()
                                : AbbreviationUtils.getDayOfWeekName(i) + "#" + week.getWeek())
                        .collect(Collectors.joining(","));
            }
            String str = (numberFlag ? fromScalar : AbbreviationUtils.getDayOfWeekName(fromScalar))
                    + "-" + (numberFlag ? toScalar : AbbreviationUtils.getDayOfWeekName(toScalar));
            return interval > 1 ? str + "/" + interval : str;
        }

        @Override
        public String toString() {
            return getTag();
        }

    }

}
