
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfWeekInMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfWeekInMonth implements TheDayOfWeekInMonth, Serializable {

    private static final long serialVersionUID = -5853750543470928852L;

    private final TreeMap<String, DateTimeSupplier> siblings =
            new TreeMap<>(new WeekInMonthComparator());
    private final StringBuilder cron = new StringBuilder();
    private Month month;
    private LocalDateTime day;
    private int index;

    private static class WeekInMonthComparator implements Comparator<String>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(String a, String b) {
            String x = a.split("#", 2)[1];
            String y = b.split("#", 2)[1];
            return Integer.parseInt(x) - Integer.parseInt(y);
        }

    }

    ThisDayOfWeekInMonth(Month month, int weekOfMonth, int dayOfWeek) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.checkValidValue(dayOfWeek);
        this.month = month;
        DateTimeSupplier supplier =
                () -> month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth)
                        .with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        this.siblings.put(dayOfWeek + "#" + weekOfMonth, supplier);
        this.day = supplier.get();
        this.cron
                .append(month.getWeekCountOfMonth() == weekOfMonth ? dayOfWeek + "L"
                        : (getBuilder().isUseDayOfWeekAsNumber() ? dayOfWeek
                                : AbbreviationUtils.getDayOfWeekName(dayOfWeek)) + "#"
                                + weekOfMonth);
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
    public TheDayOfWeekInMonth and(int weekOfMonth, int dayOfWeek) {
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(weekOfMonth);
        ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH.checkValidValue(dayOfWeek);
        DateTimeSupplier supplier =
                () -> month.getTime().with(WeekFields.ISO.weekOfMonth(), weekOfMonth)
                        .with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        this.siblings.put(dayOfWeek + "#" + weekOfMonth, supplier);
        this.cron.append(",")
                .append(month.getWeekCountOfMonth() == weekOfMonth ? dayOfWeek + "L"
                        : (getBuilder().isUseDayOfWeekAsNumber() ? dayOfWeek
                                : AbbreviationUtils.getDayOfWeekName(dayOfWeek)) + "#"
                                + weekOfMonth);
        return this;
    }

    @Override
    public TheDayOfWeekInMonth andLast(int dayOfWeek) {
        return and(month.getWeekCountOfMonth(), dayOfWeek);
    }

    @Override
    public TheHour hour(int hourOfDay) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hourOfDay);
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
            if (month.hasNext()) {
                month = month.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        Map.Entry<String, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index++);
        String cron = entry.getKey();
        String[] args = cron.split("#", 2);
        day = entry.getValue().get();
        day = day.withYear(month.getYear()).withMonth(month.getMonth())
                .with(WeekFields.ISO.weekOfMonth(),
                        Math.min(Integer.parseInt(args[1]), month.getWeekCountOfMonth()))
                .with(WeekFields.ISO.dayOfWeek(), Integer.parseInt(args[0]));
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
