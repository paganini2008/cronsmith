
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
public class ThisDayOfWeek implements TheDayOfWeek, Serializable {

    private static final long serialVersionUID = -5353496894925284106L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private final List<Range<Integer>> ranges = new ArrayList<>();
    private Week week;
    private int index;
    private LocalDateTime day;
    private int lastDayOfWeekFlag;

    ThisDayOfWeek(Week week, int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.week = week;
        DateTimeSupplier supplier =
                () -> week.getTime().with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        this.siblings.put(dayOfWeek, supplier);
        this.day = supplier.get();
        this.lastDayOfWeekFlag = dayOfWeek;
        this.ranges.add(new Range<>(dayOfWeek));
    }

    @Override
    public TheDayOfWeek andDay(int dayOfWeek) {
        this.ranges.add(new Range<>(dayOfWeek));
        return doAndDay(dayOfWeek);
    }

    private TheDayOfWeek doAndDay(int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        DateTimeSupplier supplier =
                () -> week.getTime().with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        this.siblings.put(dayOfWeek, supplier);
        this.lastDayOfWeekFlag = dayOfWeek;
        return this;
    }

    @Override
    public TheDayOfWeek toDay(int dayOfWeek, int interval) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (lastDayOfWeekFlag >= dayOfWeek) {
            throw new IllegalArgumentException(lastDayOfWeekFlag + ">=" + dayOfWeek);
        }
        for (int i = lastDayOfWeekFlag + interval; i <= dayOfWeek; i += interval) {
            doAndDay(i);
        }
        this.ranges.get(ranges.size() - 1).setTo(dayOfWeek).setInterval(interval);
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
            index = 0;
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
    public Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (week.hasNext()) {
                week = week.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        Map.Entry<Integer, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index++);
        day = entry.getValue().get();
        day = day.withYear(week.getYear()).withMonth(week.getMonth())
                .with(WeekFields.ISO.weekOfMonth(), week.getWeek())
                .with(WeekFields.ISO.dayOfWeek(), entry.getKey());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return week;
    }

    @Override
    public String toCronString() {
        return ranges.stream().map(this::getRangeString).collect(Collectors.joining(","));
    }

    public String getRangeString(Range<Integer> range) {
        StringBuilder str = new StringBuilder();
        if (range.getTo() != null) {
            str.append(getDayOfWeekName(range.getFrom())).append("-")
                    .append(getDayOfWeekName(range.getTo()));
        } else {
            str.append(getDayOfWeekName(range.getFrom()));
        }
        if (range.getInterval() != null && range.getInterval() > 1) {
            str.append("/").append(range.getInterval());
        }
        return str.toString();
    }

    private String getDayOfWeekName(int dayOfWeek) {
        if (week instanceof LastWeek) {
            return String.format(week.toCronString(), dayOfWeek);
        } else if (week instanceof TheWeek) {
            return week.toCronString().replaceAll("%s", String.valueOf(dayOfWeek));
        }
        return getBuilder().isUseDayOfWeekAsNumber() ? String.valueOf(dayOfWeek)
                : AbbreviationUtils.getDayOfWeekName(dayOfWeek);
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
