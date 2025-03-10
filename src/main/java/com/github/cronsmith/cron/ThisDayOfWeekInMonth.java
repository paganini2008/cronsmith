
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfWeekInMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfWeekInMonth implements TheDayOfWeekInMonth {

    private static final long serialVersionUID = -5853750543470928852L;

    private final TreeSet<TagIterator> iterators = new TreeSet<>();
    private Month month;
    private LocalDateTime day;
    private int index;

    ThisDayOfWeekInMonth(Month month, int weekOfMonth, int dayOfWeek) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.checkValidValue(dayOfWeek);
        this.month = month;
        this.iterators.add(new SingleValueIterator(m -> weekOfMonth, m -> dayOfWeek));
        this.day = month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth)
                .with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
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
    public TheDayOfWeekInMonth and(int weekOfMonth, int dayOfWeek) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.checkValidValue(dayOfWeek);
        this.iterators.add(new SingleValueIterator(m -> weekOfMonth, m -> dayOfWeek));
        return this;
    }

    @Override
    public TheDayOfWeekInMonth andLast(int dayOfWeek) {
        ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.checkValidValue(dayOfWeek);
        this.iterators.add(new LastDayOfWeekInMonth(m -> dayOfWeek));
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

    private class LastDayOfWeekInMonth extends SingleValueIterator {

        private static final long serialVersionUID = -5380738194811061909L;

        LastDayOfWeekInMonth(IntFunction<Month> dayOfWeekFun) {
            super(m -> m.getWeekCountOfMonth(), dayOfWeekFun);
        }

        @Override
        public String toString() {
            return dayOfWeek + "L";
        }
    }

    private class SingleValueIterator implements TagIterator, Comparable<SingleValueIterator> {

        private static final long serialVersionUID = -1561112766226184869L;
        private final IntFunction<Month> weekOfMonthFun;
        private final IntFunction<Month> dayOfWeekFun;
        protected final int weekOfMonth;
        protected final int dayOfWeek;

        SingleValueIterator(IntFunction<Month> weekOfMonthFun, IntFunction<Month> dayOfWeekFun) {
            this.weekOfMonthFun = weekOfMonthFun;
            this.dayOfWeekFun = dayOfWeekFun;
            this.weekOfMonth = weekOfMonthFun.apply(month);
            this.dayOfWeek = dayOfWeekFun.apply(month);
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
            int dayOfWeek = dayOfWeekFun.apply(month);
            int weekOfMonth = weekOfMonthFun.apply(month);
            return month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth)
                    .with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        }

        @Override
        public String getTag() {
            boolean numberFlag = getBuilder().isUseDayOfWeekAsNumber();
            return (numberFlag ? dayOfWeek : AbbreviationUtils.getDayOfWeekName(dayOfWeek)) + "#"
                    + weekOfMonth;
        }

        @Override
        public String toString() {
            return getTag();
        }

        @Override
        public int compareTo(SingleValueIterator other) {
            int left = Integer.parseInt(weekOfMonth + "" + dayOfWeek);
            int right = Integer.parseInt(other.weekOfMonth + "" + other.dayOfWeek);
            return left - right;
        }
    }

}
