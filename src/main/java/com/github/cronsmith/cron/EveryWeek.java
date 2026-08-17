package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Every nth week of a month, counted as occurrences of a weekday rather than as calendar weeks.
 * <p>
 * An interval of one means "every occurrence", which is the plain {@code MON} form; a wider
 * interval picks out the 1st, 3rd, 5th ... occurrence and renders as {@code MON#1,MON#3,MON#5}.
 *
 * @Description: EveryWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryWeek implements Week, IntervalChronoUnit, WeekOrdinal, PendingValueHolder {

    private static final long serialVersionUID = -6457126115562721512L;
    private Month month;
    private final IntFunction<Month> from;
    private final int interval;
    private LocalDateTime week;
    private int ordinal;
    private boolean self;
    private boolean forward;

    EveryWeek(Month month, IntFunction<Month> from, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.from = from;
        this.interval = interval;
        this.ordinal = getFromWeekOfMonth();
        this.week = WeekOfMonth.startOf(month.getTime(), ordinal);
        this.self = true;
        this.forward = true;
    }

    private int getFromWeekOfMonth() {
        int fromWeekOfMonth = from.apply(month);
        ThisWeek.checkOrdinal(fromWeekOfMonth);
        return fromWeekOfMonth;
    }

    @Override
    public int getFrom() {
        return getFromWeekOfMonth();
    }

    @Override
    public int currentOrdinal() {
        // Interval one means every occurrence, which a day-of-week expression resolves by listing
        // the whole month instead of picking a single ordinal out of it.
        return interval > 1 ? ordinal : WeekOfMonth.EVERY;
    }

    @Override
    public List<Integer> ordinals() {
        List<Integer> list = new ArrayList<>();
        for (int i = getFromWeekOfMonth(); i <= WeekOfMonth.MAX_ORDINAL; i += interval) {
            list.add(i);
        }
        return list;
    }

    @Override
    public boolean isEveryOrdinal() {
        return interval == 1;
    }

    @Override
    public boolean hasNext() {
        boolean next =
                self || (interval > 1 && ordinal + interval <= WeekOfMonth.MAX_ORDINAL);
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                ordinal = getFromWeekOfMonth();
                week = WeekOfMonth.startOf(month.getTime(), ordinal);
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Week next() {
        if (self) {
            self = false;
        } else if (forward) {
            ordinal += interval;
            week = WeekOfMonth.startOf(month.getTime(), ordinal);
        } else {
            forward = true;
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
        return ordinal;
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.ISO.weekOfYear());
    }

    @Override
    public LocalDateTime getTime() {
        return week;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        if (week.toLocalDate().isBefore(target.toLocalDate())) {
            while (week.toLocalDate().isBefore(target.toLocalDate()) && hasNext()) {
                next();
            }
            forward = false;
        }
        return this;
    }

    @Override
    public TheDayOfWeek day(int day) {
        final Week copy = (Week) this.copy();
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy, copy), day);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, int interval) {
        final Week copy = (Week) this.copy();
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy, copy), from, interval);
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public String toCronString() {
        return interval > 1 ? getFromWeekOfMonth() + "/" + interval
                : String.valueOf(getFromWeekOfMonth());
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    @Override
    public void takePendingValue() {
        this.forward = true;
    }

}
