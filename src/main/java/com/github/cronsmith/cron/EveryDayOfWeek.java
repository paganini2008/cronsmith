package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryDayOfWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryDayOfWeek implements DayOfWeek, IntervalChronoUnit {

    private static final long serialVersionUID = 7871249122497937952L;
    private Week week;
    private LocalDateTime day;
    private final IntFunction<Week> from;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryDayOfWeek(Week week, IntFunction<Week> from, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.week = week;
        this.from = from;
        this.interval = interval;
        this.day = week.getTime().with(WeekFields.ISO.dayOfWeek(),
                getFromDayOfWeek() + (interval - 1));
        this.self = true;
        this.forward = true;
    }

    @Override
    public int getFrom() {
        return getFromDayOfWeek();
    }

    private int getFromDayOfWeek() {
        int fromDayOfWeek = from.apply(week);
        ChronoField.DAY_OF_WEEK.checkValidValue(fromDayOfWeek);
        return fromDayOfWeek;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || day.getDayOfWeek().getValue() + interval <= 7;
        if (!next) {
            if (week.hasNext()) {
                week = week.next();
                day = week.getTime().with(WeekFields.ISO.dayOfWeek(),
                        getFromDayOfWeek() + (interval - 1));
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                day = day.plusDays(interval);
            } else {
                forward = true;
            }
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
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        Supplier<Boolean> supplier = () -> day.toLocalDate().isBefore(target.toLocalDate());
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
    public TheHour hour(int hour) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hour);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public CronExpression getParent() {
        return week;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public String toCronString() {
        int fromDayOfWeek = getFromDayOfWeek();
        if (week instanceof LastWeek) {
            List<String> list = new ArrayList<String>();
            for (int i = fromDayOfWeek; i <= 7; i += interval) {
                list.add(i + "L");
            }
            return String.join(",", list);
        } else {
            String from = getBuilder().isUseDayOfWeekAsNumber() ? "" + fromDayOfWeek
                    : AbbreviationUtils.getDayOfWeekName(fromDayOfWeek);
            String to = getBuilder().isUseDayOfWeekAsNumber() ? "" + 7
                    : AbbreviationUtils.getDayOfWeekName(7);
            if (interval > 1) {
                return String.format("%s-%s/%s", from, to, interval);
            } else {
                return String.format("%s-%s", from, to);
            }
        }
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
