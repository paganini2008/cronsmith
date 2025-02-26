
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
 * @Description: ThisSecond
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisSecond implements TheSecond, Serializable {

    private static final long serialVersionUID = 6264419114715870528L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Minute minute;
    private int index;
    private LocalDateTime second;
    private int lastSecond;
    private final List<Range<Integer>> ranges = new ArrayList<>();

    ThisSecond(Minute minute, int second) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        this.minute = minute;
        DateTimeSupplier supplier = () -> minute.getTime().withSecond(second);
        this.siblings.put(second, supplier);
        this.second = supplier.get();
        this.lastSecond = second;
        this.ranges.add(new Range<Integer>(second));
    }

    @Override
    public ThisSecond andSecond(int second) {
        this.ranges.add(new Range<Integer>(second));
        return doAndSecond(second);
    }

    private ThisSecond doAndSecond(int second) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        DateTimeSupplier supplier = () -> minute.getTime().withSecond(second);
        this.siblings.put(second, supplier);
        this.lastSecond = second;
        return this;
    }

    @Override
    public TheSecond toSecond(int second, int interval) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        for (int i = lastSecond + interval; i <= second; i += interval) {
            doAndSecond(i);
        }
        this.ranges.get(this.ranges.size() - 1).setTo(second).setInterval(interval);
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
        boolean next = index < siblings.size();
        if (!next) {
            if (minute.hasNext()) {
                minute = minute.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Second next() {
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        second = supplier.get();
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
        return ranges.stream().map(Range::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
