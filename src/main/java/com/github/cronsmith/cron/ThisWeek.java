package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.github.cronsmith.CRON;
import com.github.cronsmith.utils.IteratorUtils;
/**
 *
 * One or more numbered weeks of a month, the parent of a {@code #} day-of-week expression.
 * <p>
 * A week here is an occurrence counter rather than a calendar week: week 2 holds the second Monday,
 * the second Tuesday and so on, which is exactly what {@code MON#2} means. Ordinals are kept sorted
 * so the schedule they produce never runs backwards.
 *
 * @Description: ThisWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisWeek implements TheWeek, WeekOrdinal, PendingValueHolder {

    private static final long serialVersionUID = -4563991137870265613L;
    private final List<Integer> ordinals = new ArrayList<>();
    private Month month;
    private int index;
    private LocalDateTime week;
    private int ordinal;
    private int startWeekFlag;
    private boolean pendingCurrent;

    ThisWeek(Month month, int weekOfMonth) {
        checkOrdinal(weekOfMonth);
        this.month = month;
        this.ordinals.add(weekOfMonth);
        this.week = WeekOfMonthUtils.startOf(month.getTime(), weekOfMonth);
        this.ordinal = weekOfMonth;
        this.startWeekFlag = weekOfMonth;
    }

    static void checkOrdinal(int weekOfMonth) {
        if (weekOfMonth < 1 || weekOfMonth > WeekOfMonthUtils.MAX_ORDINAL) {
            throw new IllegalArgumentException("Invalid week of month: " + weekOfMonth);
        }
    }

    @Override
    public ThisWeek andWeek(int weekOfMonth) {
        checkOrdinal(weekOfMonth);
        if (startWeekFlag > weekOfMonth) {
            throw new IllegalArgumentException("Must greater than week: " + startWeekFlag);
        }
        this.ordinals.add(weekOfMonth);
        this.startWeekFlag = weekOfMonth;
        return this;
    }

    @Override
    public Week andLastWeek() {
        this.ordinals.add(WeekOfMonthUtils.LAST);
        return this;
    }

    @Override
    public ThisWeek toWeek(int weekOfMonth, int interval) {
        checkOrdinal(weekOfMonth);
        if (startWeekFlag >= weekOfMonth) {
            throw new IllegalArgumentException(startWeekFlag + ">=" + weekOfMonth);
        }
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        final int fromWeek = startWeekFlag;
        this.ordinals.remove(Integer.valueOf(fromWeek));
        for (int i = fromWeek; i <= weekOfMonth; i += interval) {
            this.ordinals.add(i);
        }
        this.startWeekFlag = weekOfMonth;
        return this;
    }

    @Override
    public Week toLastWeek(int interval) {
        final int fromWeek = startWeekFlag;
        this.ordinals.remove(Integer.valueOf(fromWeek));
        for (int i = fromWeek; i <= WeekOfMonthUtils.MAX_ORDINAL; i += interval) {
            this.ordinals.add(i);
        }
        this.ordinals.add(WeekOfMonthUtils.LAST);
        return this;
    }

    @Override
    public int currentOrdinal() {
        return ordinal;
    }

    @Override
    public List<Integer> ordinals() {
        return ordinals;
    }

    @Override
    public LocalDateTime getTime() {
        return week;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        boolean moved = false;
        while (week.toLocalDate().isBefore(target.toLocalDate()) && hasNext()) {
            next();
            moved = true;
        }
        if (moved) {
            pendingCurrent = true;
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
        return currentOrdinal();
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.ISO.weekOfYear());
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
    public boolean hasNext() {
        return pendingCurrent || index < ordinals.size() || month.hasNext();
    }

    @Override
    public Week next() {
        if (pendingCurrent) {
            pendingCurrent = false;
            return this;
        }
        if (index >= ordinals.size()) {
            month = month.next();
            index = 0;
        }
        ordinal = ordinals.get(index++);
        week = WeekOfMonthUtils.startOf(month.getTime(), ordinal);
        return this;
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return ordinals.stream().map(o -> o == WeekOfMonthUtils.LAST ? "L" : String.valueOf(o))
                .collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }


    @Override
    public void takePendingValue() {
        this.pendingCurrent = false;
    }
}
