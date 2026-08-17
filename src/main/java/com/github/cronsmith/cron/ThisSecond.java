
package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import com.github.cronsmith.CRON;

/**
 * 
 * @Description: ThisSecond
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisSecond implements TheSecond, PendingValueHolder {

    private static final long serialVersionUID = 6264419114715870528L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Minute minute;
    private int index;
    private LocalDateTime second;
    private int startSecondFlag;

    /**
     * Whether the value reached by the last synchronize is still owed to the caller. Without it the
     * first step after a synchronize would move past that value, and a schedule such as "every day
     * at 12:00" asked at 10:00 would answer with tomorrow rather than with today.
     */
    private boolean pendingCurrent;

    ThisSecond(Minute minute, int second) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        this.minute = minute;
        this.iterators.add(new SingleValueIterator(m -> second));
        this.second = minute.getTime().withSecond(second);
        this.startSecondFlag = second;
    }

    @Override
    public ThisSecond andSecond(int second) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        this.iterators.add(new SingleValueIterator(m -> second));
        this.startSecondFlag = second;
        return this;
    }

    @Override
    public TheSecond toSecond(int second, int interval) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (startSecondFlag >= second) {
            throw new IllegalArgumentException(startSecondFlag + ">=" + second);
        }
        final int fromSecond = startSecondFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromSecond, m -> second, interval);
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(fromSecond)));
        this.iterators.add(rangeIterator);
        startSecondFlag = second;
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return second;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier =
                () -> (second.toLocalDate().compareTo(target.toLocalDate()) < 0)
                        || (second.toLocalDate().compareTo(target.toLocalDate()) == 0
                                && second.toLocalTime().isBefore(target.toLocalTime()));
        if (supplier.get()) {
            if (SyncSupport.behindMinute(minute.getTime(), target)) {
                // Let the enclosing minute catch up in one hop and restart this minute's
                // value list, otherwise catching up costs one iteration per second elapsed.
                minute.sync(SyncSupport.startOfMinute(target));
                SyncSupport.takeOver(minute);
                index = 0;
                iterators.forEach(TagIterator::reset);
            }
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
        return second.getYear();
    }

    @Override
    public int getMonth() {
        return second.getMonthValue();
    }

    @Override
    public int getDay() {
        return second.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return second.getHour();
    }

    @Override
    public int getMinute() {
        return second.getMinute();
    }

    @Override
    public int getSecond() {
        return second.getSecond();
    }

    @Override
    public boolean hasNext() {
        if (pendingCurrent) {
            return true;
        }
        boolean next = index < iterators.size();
        if (!next) {
            if (minute.hasNext()) {
                minute = minute.next();
                index = 0;
                iterators.forEach(i -> i.reset());
                next = true;
            }
        }
        return next && iterators.get(index).hasNext();
    }

    @Override
    public Second next() {
        if (pendingCurrent) {
            pendingCurrent = false;
            return this;
        }
        TagIterator iterator = iterators.get(index);
        second = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        second = second.withYear(minute.getYear()).withMonth(minute.getMonth())
                .withDayOfMonth(minute.getDay()).withHour(minute.getHour())
                .withMinute(minute.getMinute());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return minute;
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
        private final IntFunction<Minute> ifun;
        private final int value;

        SingleValueIterator(IntFunction<Minute> ifun) {
            this.ifun = ifun;
            this.value = ifun.apply(minute);
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
            int second = ifun.apply(minute);
            return minute.getTime().withSecond(second);
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

        ValueRangeIterator(IntFunction<Minute> from, IntFunction<Minute> to, int interval) {
            this.from = from;
            this.to = to;
            this.fromScalar = from.apply(minute);
            this.toScalar = to.apply(minute);
            this.interval = interval;
            reset();
        }

        private final IntFunction<Minute> from;
        private final IntFunction<Minute> to;
        protected final int fromScalar;
        protected final int toScalar;
        protected final int interval;
        private boolean self;

        private LocalDateTime ldt;

        @Override
        public void reset() {
            int from = this.from.apply(minute);
            this.ldt = minute.getTime().withSecond(from);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getSecond() + interval <= to.apply(minute);
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            } else {
                ldt = ldt.plusSeconds(interval);
            }
            return ldt;
        }

        @Override
        public String getTag() {
            boolean slashed = interval > 1;
            String str;
            if (fromScalar >= 0 && toScalar == 59) {
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


    @Override
    public void takePendingValue() {
        this.pendingCurrent = false;
    }
}
