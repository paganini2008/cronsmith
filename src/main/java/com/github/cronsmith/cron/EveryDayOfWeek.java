package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.github.cronsmith.CRON;
import com.github.cronsmith.utils.AbbreviationUtils;
import com.github.cronsmith.utils.IteratorUtils;
/**
 *
 * Every nth weekday from a starting weekday up to Sunday, inside the week its parent stands on -
 * the {@code MON-SUN/2} form. Occurrences the month does not hold are skipped, exactly as for a
 * single weekday.
 *
 * @Description: EveryDayOfWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryDayOfWeek implements DayOfWeek, IntervalChronoUnit, PendingValueHolder {

    private static final long serialVersionUID = 7871249122497937953L;
    private Week week;
    private LocalDateTime day;
    private LocalDateTime pending;
    private final IntFunction<Week> from;
    private final int interval;
    private List<LocalDateTime> current;
    private int index;
    private boolean self;

    EveryDayOfWeek(Week week, IntFunction<Week> from, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.week = week;
        this.from = from;
        this.interval = interval;
        this.current = candidates();
        this.index = 0;
        this.day = findNext();
        this.self = this.day != null;
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

    /** The dates the selected weekdays land on in the week the parent stands on. */
    private List<LocalDateTime> candidates() {
        List<LocalDateTime> list = new ArrayList<>();
        int ordinal = week instanceof WeekOrdinal ? ((WeekOrdinal) week).currentOrdinal()
                : WeekOfMonth.LAST;
        for (int value = getFromDayOfWeek(); value <= 7; value += interval) {
            if (!(week instanceof WeekOrdinal)) {
                list.add(week.getTime().with(WeekFields.ISO.dayOfWeek(), value));
            } else if (ordinal == WeekOfMonth.EVERY) {
                list.addAll(WeekOfMonth.allOccurrences(week.getTime(), value));
            } else {
                LocalDateTime occurrence = WeekOfMonth.occurrence(week.getTime(), ordinal, value);
                if (occurrence != null) {
                    list.add(occurrence);
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    /** Walks to the next existing occurrence, rolling into later weeks when the week runs out. */
    private LocalDateTime findNext() {
        while (true) {
            if (index < current.size()) {
                LocalDateTime candidate = current.get(index++);
                if (day == null || candidate.isAfter(day)) {
                    return candidate;
                }
                continue;
            }
            if (!week.hasNext()) {
                return null;
            }
            week = week.next();
            current = candidates();
            index = 0;
        }
    }

    @Override
    public boolean hasNext() {
        if (self) {
            return true;
        }
        if (pending == null) {
            pending = findNext();
        }
        return pending != null;
    }

    @Override
    public Day next() {
        if (self) {
            self = false;
            return this;
        }
        if (pending == null) {
            pending = findNext();
        }
        if (pending != null) {
            day = pending;
            pending = null;
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
        boolean moved = false;
        while (day != null && day.toLocalDate().isBefore(target.toLocalDate()) && hasNext()) {
            next();
            moved = true;
        }
        if (moved) {
            // The value just reached still has to be handed out by the next step.
            self = true;
        }
        return this;
    }

    @Override
    public TheHour hour(int hour) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy, copy), hour);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy, copy), from, interval);
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
        int ordinal = week instanceof WeekOrdinal && !((WeekOrdinal) week).isEveryOrdinal()
                ? ((WeekOrdinal) week).currentOrdinal()
                : 0;
        if (ordinal != 0) {
            // Neither '#' nor 'L' has a range form, so the weekdays are listed one by one.
            List<String> list = new ArrayList<>();
            for (int i = fromDayOfWeek; i <= 7; i += interval) {
                list.add(name(i) + (ordinal == WeekOfMonth.LAST ? "L" : "#" + ordinal));
            }
            return String.join(",", list);
        }
        String range = name(fromDayOfWeek) + "-" + name(7);
        return interval > 1 ? range + "/" + interval : range;
    }

    private String name(int value) {
        return getBuilder().isUseDayOfWeekAsNumber()
                ? String.valueOf(AbbreviationUtils.toCronDayOfWeek(value))
                : AbbreviationUtils.getDayOfWeekName(value);
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }


    @Override
    public void takePendingValue() {
        this.self = false;
    }
}
