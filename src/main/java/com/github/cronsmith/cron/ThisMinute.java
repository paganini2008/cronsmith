
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
 * @Description: ThisMinute
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisMinute implements TheMinute {

    private static final long serialVersionUID = 7090607807516357598L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Hour hour;
    private int index;
    private LocalDateTime minute;
    private int startMinuteFlag;

    ThisMinute(Hour hour, int minute) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        this.hour = hour;
        this.iterators.add(new SingleValueIterator(h -> minute));
        this.minute = hour.getTime().withMinute(minute);
        this.startMinuteFlag = minute;
    }

    @Override
    public ThisMinute andMinute(int minute) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        this.iterators.add(new SingleValueIterator(h -> minute));
        this.startMinuteFlag = minute;
        return this;
    }

    @Override
    public TheMinute toMinute(int minute, int interval) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (startMinuteFlag >= minute) {
            throw new IllegalArgumentException(startMinuteFlag + ">=" + minute);
        }
        final int fromMinte = startMinuteFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromMinte, m -> minute, interval);
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(fromMinte)));
        this.iterators.add(rangeIterator);
        startMinuteFlag = minute;
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return minute;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier =
                () -> (minute.toLocalDate().compareTo(target.toLocalDate()) < 0)
                        || (minute.toLocalDate().compareTo(target.toLocalDate()) == 0
                                && minute.toLocalTime().isBefore(target.toLocalTime()));
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
        return minute.getYear();
    }

    @Override
    public int getMonth() {
        return minute.getMonthValue();
    }

    @Override
    public int getDay() {
        return minute.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return minute.getHour();
    }

    @Override
    public int getMinute() {
        return minute.getMinute();
    }

    @Override
    public TheSecond second(int second) {
        final Minute copy = (Minute) this.copy();
        return new ThisSecond(IteratorUtils.getFirst(copy), second);
    }

    @Override
    public Second everySecond(IntFunction<Minute> from, int interval) {
        final Minute copy = (Minute) this.copy();
        return new EverySecond(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < iterators.size();
        if (!next) {
            if (hour.hasNext()) {
                hour = hour.next();
                index = 0;
                iterators.forEach(i -> i.reset());
                next = true;
            }
        }
        return next && iterators.get(index).hasNext();
    }

    @Override
    public Minute next() {
        TagIterator iterator = iterators.get(index);
        minute = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        minute = minute.withYear(hour.getYear()).withMonth(hour.getMonth())
                .withDayOfMonth(hour.getDay()).withHour(hour.getHour());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return hour;
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
        private final IntFunction<Hour> ifun;
        private final int value;

        SingleValueIterator(IntFunction<Hour> ifun) {
            this.ifun = ifun;
            this.value = ifun.apply(hour);
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
            int minute = ifun.apply(hour);
            return hour.getTime().withMinute(minute);
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

        ValueRangeIterator(IntFunction<Hour> from, IntFunction<Hour> to, int interval) {
            this.from = from;
            this.to = to;
            this.fromScalar = from.apply(hour);
            this.toScalar = to.apply(hour);
            this.interval = interval;
            reset();
        }

        private final IntFunction<Hour> from;
        private final IntFunction<Hour> to;
        protected final int fromScalar;
        protected final int toScalar;
        protected final int interval;
        private boolean self;

        private LocalDateTime ldt;

        @Override
        public void reset() {
            int from = this.from.apply(hour);
            this.ldt = hour.getTime().withMinute(from);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getMinute() + interval <= to.apply(hour);
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            } else {
                ldt = ldt.plusMinutes(interval);
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

}
