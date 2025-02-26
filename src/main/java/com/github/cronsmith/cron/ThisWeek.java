
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
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisWeek implements TheWeek, Serializable {

    private static final long serialVersionUID = -4563991137870265612L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Month month;
    private int index;
    private LocalDateTime week;
    private int lastWeek;
    private final StringBuilder cron;

    ThisWeek(Month month, int weekOfMonth) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        this.month = month;
        DateTimeSupplier supplier =
                () -> month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth)
                        .with(WeekFields.ISO.dayOfWeek(), 1);
        this.siblings.put(weekOfMonth, supplier);
        this.week = supplier.get();
        this.lastWeek = weekOfMonth;
        this.cron = new StringBuilder().append("%s#").append(weekOfMonth);
    }

    @Override
    public ThisWeek andWeek(int weekOfMonth) {
        return andWeek(weekOfMonth, true);
    }

    private ThisWeek andWeek(int weekOfMonth, boolean writeCron) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        DateTimeSupplier supplier =
                () -> month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth);
        this.siblings.put(weekOfMonth, supplier);
        this.lastWeek = weekOfMonth;
        if (writeCron) {
            this.cron.append(",%s#").append(weekOfMonth);
        }
        return this;
    }

    @Override
    public ThisWeek toWeek(int weekOfMonth, int interval) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        List<Integer> weeks = new ArrayList<Integer>();
        for (int i = lastWeek + interval; i <= weekOfMonth; i += interval) {
            andWeek(i, false);
            weeks.add(i);
        }
        for (int w : weeks) {
            this.cron.append(",%s#").append(w);
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
    public TheDayOfWeek day(int day) {
        final Week copy = (Week) this.copy();
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy), day);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, IntFunction<Week> to, int interval) {
        final Week copy = (Week) this.copy();
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
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
        week = week.withYear(month.getYear()).withMonth(month.getMonth()).with(
                WeekFields.ISO.weekOfMonth(),
                Math.min(entry.getKey(), month.getWeekCountOfMonth()));
        return this;
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return this.cron.toString();
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
