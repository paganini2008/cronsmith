
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
 * @Description: ThisHour
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisHour implements TheHour, Serializable {

    private static final long serialVersionUID = 8124589572544886753L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Day day;
    private int index;
    private LocalDateTime hour;
    private int lastHourFlag;
    private final List<Range<Integer>> ranges = new ArrayList<Range<Integer>>();

    ThisHour(Day day, int hour) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        this.day = day;
        DateTimeSupplier supplier = () -> day.getTime().withHour(hour);
        this.siblings.put(hour, supplier);
        this.hour = supplier.get();
        this.lastHourFlag = hour;
        this.ranges.add(new Range<Integer>(hour));
    }

    @Override
    public ThisHour andHour(int hour) {
        this.ranges.add(new Range<Integer>(hour));
        return doAndHour(hour);
    }

    private ThisHour doAndHour(int hour) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        DateTimeSupplier supplier = () -> day.getTime().withHour(hour);
        siblings.put(hour, supplier);
        this.lastHourFlag = hour;
        return this;
    }

    @Override
    public TheHour toHour(int hour, int interval) {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (lastHourFlag >= hour) {
            throw new IllegalArgumentException(lastHourFlag + ">=" + hour);
        }
        for (int i = lastHourFlag + interval; i <= hour; i += interval) {
            doAndHour(i);
        }
        this.ranges.get(this.ranges.size() - 1).setTo(hour).setInterval(interval);
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
            index = 0;
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
    public Minute everyMinute(IntFunction<Hour> from, IntFunction<Hour> to, int interval) {
        final Hour copy = (Hour) this.copy();
        return new EveryMinute(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (day.hasNext()) {
                day = day.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Hour next() {
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        hour = supplier.get();
        hour = hour.withYear(day.getYear()).withMonth(day.getMonth()).withDayOfMonth(day.getDay());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return day;
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
