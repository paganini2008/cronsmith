
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
 * @Description: ThisHour
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisHour implements TheHour {

    private static final long serialVersionUID = 8124589572544886753L;
    private final List<TagIterator> iterators = new ArrayList<>();
    private Day day;
    private int index;
    private LocalDateTime hour;
    private int startHourFlag;

    ThisHour(Day day, int hour) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        this.day = day;
        this.iterators.add(new SingleValueIterator(d -> hour));
        this.hour = day.getTime().withHour(hour);
        this.startHourFlag = hour;
    }

    @Override
    public ThisHour andHour(int hour) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        this.iterators.add(new SingleValueIterator(d -> hour));
        this.startHourFlag = hour;
        return this;
    }

    @Override
    public TheHour toHour(int hour, int interval) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (startHourFlag >= hour) {
            throw new IllegalArgumentException(startHourFlag + ">=" + hour);
        }
        final int fromHour = startHourFlag;
        ValueRangeIterator rangeIterator =
                new ValueRangeIterator(m -> fromHour, m -> hour, interval);
        this.iterators.removeIf(iter -> iter.toString().equals(String.valueOf(fromHour)));
        this.iterators.add(rangeIterator);
        startHourFlag = hour;
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return hour;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> (hour.toLocalDate().compareTo(target.toLocalDate()) < 0)
                || (hour.toLocalDate().compareTo(target.toLocalDate()) == 0
                        && hour.toLocalTime().isBefore(target.toLocalTime()));
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
        return hour.getYear();
    }

    @Override
    public int getMonth() {
        return hour.getMonthValue();
    }

    @Override
    public int getDay() {
        return hour.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return hour.getHour();
    }

    @Override
    public TheMinute minute(int minute) {
        final Hour copy = (Hour) this.copy();
        return new ThisMinute(IteratorUtils.getFirst(copy), minute);
    }

    @Override
    public Minute everyMinute(IntFunction<Hour> from, int interval) {
        final Hour copy = (Hour) this.copy();
        return new EveryMinute(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < iterators.size();
        if (!next) {
            if (day.hasNext()) {
                day = day.next();
                index = 0;
                iterators.forEach(i -> i.reset());
                next = true;
            }
        }
        return next && iterators.get(index).hasNext();
    }

    @Override
    public Hour next() {
        TagIterator iterator = iterators.get(index);
        hour = iterator.next();
        if (!iterator.hasNext()) {
            index++;
            iterator.reset();
        }
        hour = hour.withYear(day.getYear()).withMonth(day.getMonth()).withDayOfMonth(day.getDay());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return day;
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
        private final IntFunction<Day> ifun;
        private final int value;

        SingleValueIterator(IntFunction<Day> ifun) {
            this.ifun = ifun;
            this.value = ifun.apply(day);
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
            int hour = ifun.apply(day);
            return day.getTime().withHour(hour);
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

        ValueRangeIterator(IntFunction<Day> from, IntFunction<Day> to, int interval) {
            this.from = from;
            this.to = to;
            this.fromScalar = from.apply(day);
            this.toScalar = to.apply(day);
            this.interval = interval;
            reset();
        }

        private final IntFunction<Day> from;
        private final IntFunction<Day> to;
        protected final int fromScalar;
        protected final int toScalar;
        protected final int interval;
        private boolean self;

        private LocalDateTime ldt;

        @Override
        public void reset() {
            int from = this.from.apply(day);
            this.ldt = day.getTime().withHour(from);
            this.self = true;
        }

        @Override
        public boolean hasNext() {
            return self || ldt.getHour() + interval <= to.apply(day);
        }

        @Override
        public LocalDateTime next() {
            if (self) {
                self = false;
            } else {
                ldt = ldt.plusHours(interval);
            }
            return ldt;
        }

        @Override
        public String getTag() {
            boolean slashed = interval > 1;
            String str;
            if (fromScalar >= 0 && toScalar == 23) {
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
