
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.github.cronsmith.YCRON;
import com.github.cronsmith.utils.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfYear implements TheDay, PendingValueHolder {

    private static final long serialVersionUID = -8235489088108418524L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Year year;
    private int index;
    private LocalDateTime day;
    private int startDayFlag;

    /**
     * Whether the value reached by the last synchronize is still owed to the caller. Without it the
     * first step after a synchronize would move past that value, and a schedule such as "every day
     * at 12:00" asked at 10:00 would answer with tomorrow rather than with today.
     */
    private boolean pendingCurrent;

    /**
     * The last day of the year, recomputed year by year so a leap year keeps its 366th day rather
     * than firing on 30 December.
     */
    ThisDayOfYear(Year year) {
        this.year = year;
        this.iterators.add(new LastDayIterator(0));
        this.day = year.getTime().withDayOfYear(year.getLastDayOfYear());
        this.startDayFlag = year.getLastDayOfYear();
    }

    ThisDayOfYear(Year year, int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        this.year = year;
        this.iterators.add(new SingleValueIterator(y -> dayOfYear));
        this.day = year.getTime().withDayOfYear(dayOfYear);
        this.startDayFlag = dayOfYear;
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
            pendingCurrent = true;
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
    public TheDay andDay(int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        if (startDayFlag > dayOfYear) {
            throw new IllegalArgumentException("Must greater than day: " + startDayFlag);
        }
        this.iterators.add(new SingleValueIterator(y -> dayOfYear));
        this.startDayFlag = dayOfYear;
        return this;
    }

    @Override
    public TheDay toDay(int dayOfYear, int interval) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        if (startDayFlag >= dayOfYear) {
            throw new IllegalArgumentException(startDayFlag + ">=" + dayOfYear);
        }
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        final int fromDay = startDayFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromDay, m -> dayOfYear, interval);
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(fromDay)));
        this.iterators.add(rangeIterator);
        startDayFlag = dayOfYear;
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
    public Day andLastDay(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid last day offset: " + n);
        }
        this.iterators.add(new LastDayIterator(n));
        return this;
    }

    @Override
    public Day andLastWeekday() {
        this.iterators.add(new LastWeekdayIterator());
        return this;
    }

    @Override
    public TheDay andLatestWeekday(int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        this.iterators.add(new LatestWeekdayIterator(dayOfYear));
        return this;
    }

    @Override
    public boolean hasNext() {
        if (pendingCurrent) {
            return true;
        }
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
    public Day next() {
        if (pendingCurrent) {
            pendingCurrent = false;
            return this;
        }
        TagIterator iterator = iterators.get(index);
        day = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        day = day.withYear(year.getYear());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public CronType getCronType() {
        return CronType.YCRON;
    }

    /**
     * Day-of-year has no field in traditional cron, so it stays unrenderable there - the CRON path
     * keeps refusing it. YCRON, which does have a day-of-year field, calls {@link #toCronString()}
     * directly instead.
     */
    @Override
    public boolean supportCronString() {
        return false;
    }

    /**
     * The day-of-year field body, e.g. {@code 100}, {@code 100-200/10}, {@code L}, {@code L-3},
     * {@code 100W} or {@code LW}. Placed by {@link YCRON}, not by the traditional CRON path.
     */
    @Override
    public String toCronString() {
        return iterators.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return YCRON.toYCronString(this);
    }

    private class ToLastWeekdayIterator extends ValueRangeIterator {

        private static final long serialVersionUID = -286992600993560352L;

        ToLastWeekdayIterator(IntFunction<Year> from, int interval) {
            super(from, m -> m.getLastWeekdayOfYear(), interval);
        }

        @Override
        public String getTag() {
            String str = fromScalar + "-LW";
            return interval > 1 ? str + "/" + interval : str;
        }

    }

    private class ToLatestWeekdayIterator extends ValueRangeIterator {

        private static final long serialVersionUID = -6562272372696442395L;

        ToLatestWeekdayIterator(IntFunction<Year> from, int dayOfYear, int interval) {
            super(from, y -> y.getLatestWeekdayOfYear(dayOfYear), interval);
            this.dayOfYear = dayOfYear;
        }

        private final int dayOfYear;

        @Override
        public String getTag() {
            String str = fromScalar + "-" + dayOfYear + "W";
            return interval > 1 ? str + "/" + interval : str;
        }

    }

    private class ToLastDayIterator extends ValueRangeIterator {

        private static final long serialVersionUID = -9151384314936251635L;

        ToLastDayIterator(IntFunction<Year> from, int interval) {
            super(from, y -> y.getLastDayOfYear(), interval);
        }

        @Override
        public String getTag() {
            String str = fromScalar + "-L";
            return interval > 1 ? str + "/" + interval : str;
        }

    }

    private class LastWeekdayIterator extends SingleValueIterator {

        private static final long serialVersionUID = 4743397573090324283L;

        LastWeekdayIterator() {
            super(m -> m.getLastWeekdayOfYear());
        }

        @Override
        public String getTag() {
            return "LW";
        }

    }

    private class LatestWeekdayIterator extends SingleValueIterator {

        private static final long serialVersionUID = 8781405992563059020L;

        LatestWeekdayIterator(int dayOfYear) {
            super(y -> y.getLatestWeekdayOfYear(dayOfYear));
            this.dayOfYear = dayOfYear;
        }

        private final int dayOfYear;

        @Override
        public String getTag() {
            return dayOfYear + "W";
        }

    }

    private class LastDayIterator extends SingleValueIterator {

        private static final long serialVersionUID = -212509240909099044L;

        LastDayIterator(int n) {
            super(y -> y.getLastDayOfYear(n));
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
            int dayOfYear = ifun.apply(year);
            return year.getTime().withDayOfYear(dayOfYear);
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
            this.ldt = year.getTime().withDayOfYear(from);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getDayOfYear() + interval <= to.apply(year);
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


    @Override
    public void takePendingValue() {
        this.pendingCurrent = false;
    }
}
