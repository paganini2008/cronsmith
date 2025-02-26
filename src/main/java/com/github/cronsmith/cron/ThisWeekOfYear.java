package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisWeekOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisWeekOfYear implements TheWeek, Serializable {

    private static final long serialVersionUID = -3294283555586718358L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Year year;
    private int index;
    private LocalDateTime week;
    private int lastWeek;

    ThisWeekOfYear(Year year, int weekOfYear) {
        ChronoField.ALIGNED_WEEK_OF_YEAR.checkValidValue(weekOfYear);
        this.year = year;
        DateTimeSupplier supplier = () -> year.getTime()
                .with(WeekFields.ISO.weekOfYear(), weekOfYear).with(WeekFields.ISO.dayOfWeek(), 1);
        this.siblings.put(weekOfYear, supplier);
        this.week = supplier.get();
        this.lastWeek = weekOfYear;
    }

    @Override
    public TheWeek andWeek(int weekOfYear) {
        ChronoField.ALIGNED_WEEK_OF_YEAR.checkValidValue(weekOfYear);
        DateTimeSupplier supplier = () -> year.getTime()
                .with(WeekFields.ISO.weekOfYear(), weekOfYear).with(WeekFields.ISO.dayOfWeek(), 1);
        this.siblings.put(weekOfYear, supplier);
        this.lastWeek = weekOfYear;
        return this;
    }

    @Override
    public TheWeek toWeek(int weekOfYear, int interval) {
        ChronoField.ALIGNED_WEEK_OF_YEAR.checkValidValue(weekOfYear);
        for (int i = lastWeek + interval; i < weekOfYear; i += interval) {
            andWeek(i);
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return week;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> week.toLocalDate().isBefore(target.toLocalDate());
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
        return week.getYear();
    }

    @Override
    public int getMonth() {
        return week.getMonthValue();
    }

    @Override
    public int getWeek() {
        return week.get(WeekFields.ISO.weekOfMonth());
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.ISO.weekOfYear());
    }

    @Override
    public TheDayOfWeek day(int dayOfWeek) {
        final Week copy = (Week) this;
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy), dayOfWeek);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, IntFunction<Week> to, int interval) {
        final Week copy = (Week) this;
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Week next() {
        Map.Entry<Integer, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index++);
        week = entry.getValue().get();
        week.withYear(year.getYear()).with(WeekFields.ISO.weekOfYear(),
                Math.min(entry.getKey(), year.getWeekCountOfYear()));
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public boolean supportCronString() {
        return false;
    }
}
