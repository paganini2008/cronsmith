package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.function.Supplier;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryMonth implements Month, Serializable {

    private static final long serialVersionUID = -7085376125910878673L;
    private Year year;
    private LocalDateTime month;
    private final IntFunction<Year> from;
    private final IntFunction<Year> to;
    private final DateTimeSupplier supplier;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryMonth(Year year, IntFunction<Year> from, IntFunction<Year> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.year = year;
        this.from = from;
        this.to = to;
        this.supplier = getSuppiler();
        this.month = supplier.get();
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private DateTimeSupplier getSuppiler() {
        return () -> year.getTime().withMonth(getFromMonth()).withDayOfMonth(1).withHour(0)
                .withMinute(0).withSecond(0);
    }

    private int getFromMonth() {
        int fromMonth = from.apply(year);
        ChronoField.MONTH_OF_YEAR.checkValidValue(fromMonth);
        return fromMonth;
    }

    private int getToMonth() {
        int toMonth = to.apply(year);
        ChronoField.MONTH_OF_YEAR.checkValidValue(toMonth);
        return toMonth;
    }

    @Override
    public boolean hasNext() {
        boolean next = (self || month.getMonthValue() + interval <= getToMonth());
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                month = supplier.get();
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Month next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                month = month.plusMonths(interval);
            } else {
                forward = true;
            }
        }
        return this;
    }

    @Override
    public int getYear() {
        return month.getYear();
    }

    @Override
    public int getMonth() {
        return month.getMonthValue();
    }

    @Override
    public int getLastDay(int n) {
        int lastDayOfMonth = month.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        lastDayOfMonth -= n;
        return lastDayOfMonth;
    }

    @Override
    public int getLastWeekday() {
        return getLatestWeekday(getLastDay());
    }

    @Override
    public Day latestWeekday(int dayOfMonth) {
        return new LatestWeekdayOfMonth(this, dayOfMonth);
    }

    @Override
    public int getLatestWeekday(int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        LocalDateTime ldt = month.withDayOfMonth(dayOfMonth);
        LocalDateTime nextDay;
        if (ldt.getDayOfWeek() == DayOfWeek.SATURDAY) {
            nextDay = ldt.minusDays(1);
            if (nextDay.getMonthValue() != ldt.getMonthValue()) {
                nextDay = ldt.plusDays(2);
            }
        } else if (ldt.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = ldt.plusDays(1);
            if (nextDay.getMonthValue() != ldt.getMonthValue()) {
                nextDay = ldt.minusDays(2);
            }
        } else {
            nextDay = ldt;
        }
        return nextDay.getDayOfMonth();
    }

    @Override
    public int getWeekCountOfMonth() {
        return month.with(TemporalAdjusters.lastDayOfMonth()).get(WeekFields.ISO.weekOfMonth());
    }

    @Override
    public LocalDateTime getTime() {
        return month;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> month.toLocalDate().isBefore(target.toLocalDate());
        if (supplier.get()) {
            while (supplier.get()) {
                if (hasNext()) {
                    next();
                } else {
                    break;
                }
            }
            forward = false;
        }
        return this;
    }

    @Override
    public TheDay day(int dayOfMonth) {
        final Month copy = (Month) this.copy();
        return new ThisDay(IteratorUtils.getFirst(copy), dayOfMonth);
    }

    @Override
    public Day lastDay(int n) {
        final Month copy = (Month) this.copy();
        return new LastDayOfMonth(IteratorUtils.getFirst(copy), n);
    }

    @Override
    public Day everyDay(IntFunction<Month> from, IntFunction<Month> to, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryDay(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public TheWeek week(int weekOfMonth) {
        final Month copy = (Month) this.copy();
        return new ThisWeek(IteratorUtils.getFirst(copy), weekOfMonth);
    }

    @Override
    public TheDayOfWeekInMonth dayOfWeek(int weekOfMonth, int dayOfWeek) {
        final Month copy = (Month) this.copy();
        return new ThisDayOfWeekInMonth(IteratorUtils.getFirst(copy), weekOfMonth, dayOfWeek);
    }

    @Override
    public Week lastWeek() {
        final Month copy = (Month) this.copy();
        return new LastWeekOfMonth(IteratorUtils.getFirst(copy));
    }

    @Override
    public Week everyWeek(IntFunction<Month> from, IntFunction<Month> to, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryWeek(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public String toCronString() {
        int fromMonth = getFromMonth();
        int toMonth = getToMonth();
        String str;
        if (fromMonth == 1 && toMonth == 12) {
            str = "*";
        } else if (fromMonth != 1 && toMonth == 12) {
            str = String.valueOf(fromMonth);
        } else {
            str = String.format("%s-%s", AbbreviationUtils.getMonthName(fromMonth),
                    AbbreviationUtils.getMonthName(toMonth));
        }
        return interval > 1 ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
