package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;
import java.util.Objects;

/**
 *
 * A list of "nth weekday of the month" entries, the direct counterpart of the {@code #} and
 * {@code L} tags: {@code TUE#2,WED#3,FRIL}.
 * <p>
 * Entries are sorted by the date they land on within each month so the schedule runs forward, and
 * a month that has no fifth Friday simply contributes nothing for that entry.
 *
 * @Description: ThisDayOfWeekInMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfWeekInMonth implements TheDayOfWeekInMonth, PendingValueHolder {

    private static final long serialVersionUID = -5853750543470928853L;
    private final List<Entry> entries = new ArrayList<>();
    private Month month;
    private LocalDateTime day;
    private LocalDateTime pending;
    private List<LocalDateTime> occurrences = new ArrayList<>();
    private int index;
    private boolean self;

    ThisDayOfWeekInMonth(Month month, int weekOfMonth, int dayOfWeek) {
        checkOrdinalOrLast(weekOfMonth);
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.month = month;
        this.entries.add(new Entry(weekOfMonth, dayOfWeek));
        rewind();
    }

    @Override
    public TheDayOfWeekInMonth and(int weekOfMonth, int dayOfWeek) {
        checkOrdinalOrLast(weekOfMonth);
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.entries.add(new Entry(weekOfMonth, dayOfWeek));
        rewind();
        return this;
    }

    @Override
    public TheDayOfWeekInMonth andLast(int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.entries.add(new Entry(WeekOfMonth.LAST, dayOfWeek));
        rewind();
        return this;
    }

    private static void checkOrdinalOrLast(int weekOfMonth) {
        if (weekOfMonth != WeekOfMonth.LAST) {
            ThisWeek.checkOrdinal(weekOfMonth);
        }
    }

    /** Recomputes this month's occurrences and puts the iteration back at the first of them. */
    private void rewind() {
        this.day = null;
        this.pending = null;
        this.occurrences = occurrencesOf(month.getTime());
        this.index = 0;
        this.day = findNext();
        this.self = this.day != null;
    }

    private List<LocalDateTime> occurrencesOf(LocalDateTime anyDayOfMonth) {
        return entries.stream()
                .map(entry -> WeekOfMonth.occurrence(anyDayOfMonth, entry.ordinal, entry.dayOfWeek))
                .filter(Objects::nonNull).distinct().sorted()
                .collect(Collectors.toList());
    }

    private LocalDateTime findNext() {
        while (true) {
            if (index < occurrences.size()) {
                LocalDateTime candidate = occurrences.get(index++);
                if (day == null || candidate.isAfter(day)) {
                    return candidate;
                }
                continue;
            }
            if (!month.hasNext()) {
                return null;
            }
            month = month.next();
            occurrences = occurrencesOf(month.getTime());
            index = 0;
        }
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
    public TheHour hour(int hourOfDay) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy, copy), hourOfDay);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy, copy), from, interval);
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
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return entries.stream().map(Entry::getTag).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    /** One {@code &lt;weekday&gt;#&lt;n&gt;} or {@code &lt;weekday&gt;L} entry. */
    private class Entry implements Serializable {

        private static final long serialVersionUID = -1561112766226184869L;
        private final int ordinal;
        private final int dayOfWeek;

        Entry(int ordinal, int dayOfWeek) {
            this.ordinal = ordinal;
            this.dayOfWeek = dayOfWeek;
        }

        String getTag() {
            String name = getBuilder().isUseDayOfWeekAsNumber()
                    ? String.valueOf(AbbreviationUtils.toCronDayOfWeek(dayOfWeek))
                    : AbbreviationUtils.getDayOfWeekName(dayOfWeek);
            return ordinal == WeekOfMonth.LAST ? name + "L" : name + "#" + ordinal;
        }

        @Override
        public String toString() {
            return getTag();
        }

    }


    @Override
    public void takePendingValue() {
        this.self = false;
    }
}
