
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
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
public class ThisMinute implements TheMinute, Serializable {

    private static final long serialVersionUID = 7090607807516357598L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Hour hour;
    private int index;
    private LocalDateTime minute;
    private int lastMinuteFlag;
    private final List<Range<Integer>> ranges = new ArrayList<Range<Integer>>();

    ThisMinute(Hour hour, int minute) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        this.hour = hour;
        DateTimeSupplier supplier = () -> hour.getTime().withMinute(minute);
        this.siblings.put(minute, supplier);
        this.minute = supplier.get();
        this.lastMinuteFlag = minute;
        this.ranges.add(new Range<Integer>(minute));
    }

    @Override
    public ThisMinute andMinute(int minute) {
        this.ranges.add(new Range<Integer>(minute));
        return doAndMinute(minute);
    }

    private ThisMinute doAndMinute(int minute) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        DateTimeSupplier supplier = () -> hour.getTime().withMinute(minute);
        this.siblings.put(minute, supplier);
        this.lastMinuteFlag = minute;
        return this;
    }

    @Override
    public TheMinute toMinute(int minute, int interval) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (lastMinuteFlag >= minute) {
            throw new IllegalArgumentException(lastMinuteFlag + ">=" + minute);
        }
        for (int i = lastMinuteFlag + interval; i <= minute; i += interval) {
            doAndMinute(i);
        }
        this.ranges.get(this.ranges.size() - 1).setTo(minute).setInterval(interval);
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
            index = 0;
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
    public Second everySecond(IntFunction<Minute> from, IntFunction<Minute> to, int interval) {
        final Minute copy = (Minute) this.copy();
        return new EverySecond(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (hour.hasNext()) {
                hour = hour.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Minute next() {
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        minute = supplier.get();
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
        return ranges.stream().map(Range::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
