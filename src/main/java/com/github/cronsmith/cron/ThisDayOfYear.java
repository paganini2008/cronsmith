
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfYear implements TheDay, Serializable {

    private static final long serialVersionUID = -8235489088108418524L;
    private final TreeMap<String, DateTimeSupplier> siblings = new TreeMap<>();
    private final List<Range<Integer>> ranges = new ArrayList<Range<Integer>>();
    private Year year;
    private int index;
    private LocalDateTime day;
    private int lastDayFlag;

    ThisDayOfYear(Year year, int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        this.year = year;
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(dayOfYear).withHour(0)
                .withMinute(0).withSecond(0);
        this.siblings.put(String.valueOf(dayOfYear), supplier);
        this.day = supplier.get();
        this.lastDayFlag = dayOfYear;
        this.ranges.add(new Range<Integer>(dayOfYear));
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
    public TheDay andDay(int dayOfYear) {
        this.ranges.add(new Range<Integer>(dayOfYear));
        return doAndDay(dayOfYear);
    }

    private TheDay doAndDay(int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(dayOfYear).withHour(0)
                .withMinute(0).withSecond(0);
        this.siblings.put(String.valueOf(dayOfYear), supplier);
        this.lastDayFlag = dayOfYear;
        this.ranges.add(new Range<Integer>(dayOfYear));
        return this;
    }

    @Override
    public TheDay toDay(int dayOfYear, int interval) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (lastDayFlag == 99) {
            throw new IllegalArgumentException("Unable to append any day to 'L' or 'LW' or 'W'");
        }
        if (lastDayFlag >= dayOfYear) {
            throw new IllegalArgumentException(lastDayFlag + ">=" + dayOfYear);
        }
        for (int i = lastDayFlag + interval; i <= dayOfYear; i += interval) {
            doAndDay(i);
        }
        this.ranges.get(this.ranges.size() - 1).setTo(dayOfYear).setInterval(interval);
        return this;
    }

    @Override
    public Day toLastDay(int interval) {
        return toDay(year.isLeapYear() ? 366 : 365, interval);
    }

    @Override
    public Day andLastDay(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid last day offset: " + n);
        }
        DateTimeSupplier supplier = () -> year.getTime().with(TemporalAdjusters.lastDayOfYear())
                .withHour(0).withMinute(0).withSecond(0).minusDays(n);
        this.siblings.put(n > 0 ? "L-" + n : "L", supplier);
        this.lastDayFlag = 99;
        return this;
    }

    @Override
    public Day andLastWeekday() {
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(year.getLastWeekdayOfYear())
                .withHour(0).withMinute(0).withSecond(0);
        this.siblings.put("LW", supplier);
        this.lastDayFlag = 99;
        return this;
    }

    @Override
    public Day andLatestWeekday(int dayOfYear) {
        DateTimeSupplier supplier =
                () -> year.getTime().withDayOfYear(year.getLatestWeekdayOfYear(dayOfYear))
                        .withHour(0).withMinute(0).withSecond(0);
        this.siblings.put(dayOfYear + "W", supplier);
        this.lastDayFlag = 99;
        return this;
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
    public Day next() {
        Map.Entry<String, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index++);
        day = entry.getValue().get();
        day = day.withYear(year.getYear());
        int dayOfYear = getDayOfYear(entry.getKey());
        day = day.withDayOfYear(dayOfYear);
        return this;
    }

    private int getDayOfYear(String repr) {
        try {
            return Integer.parseInt(repr);
        } catch (RuntimeException e) {
            if ("L".equals(repr)) {
                return year.getLastDayOfYear();
            } else if ("LW".equals(repr)) {
                return year.getLastWeekdayOfYear();
            } else if (repr.matches("(\\d+)W")) {
                return Integer.parseInt(repr.replace("W", ""));
            } else {
                Pattern pattern = Pattern.compile("L\\-(\\d+)");
                Matcher matcher = pattern.matcher(repr);
                if (matcher.matches()) {
                    int n = Integer.parseInt(matcher.group(1));
                    return year.getLastDayOfYear() - n;
                }
                throw new IllegalArgumentException(repr);
            }
        }
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
