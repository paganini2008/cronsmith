package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.cronsmith.CRON;
import com.github.cronsmith.utils.AbbreviationUtils;
import com.github.cronsmith.utils.IteratorUtils;
/**
 *
 * One or more weekdays inside the week its parent currently stands on.
 * <p>
 * What a weekday resolves to depends on that parent: under a plain week it is every Monday, under a
 * numbered week the nth Monday ({@code MON#2}), and under the last week the last Monday
 * ({@code MONL}). Months without the requested occurrence - a fifth Friday, say - are skipped
 * rather than silently rolled into the next month.
 *
 * @Description: ThisDayOfWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfWeek implements TheDayOfWeek, PendingValueHolder {

    private static final long serialVersionUID = -5353496894925284107L;
    private final List<DayOfWeekTag> tags = new ArrayList<>();
    private Week week;
    private List<Integer> values;
    private List<LocalDateTime> current;
    private int index;
    private LocalDateTime day;
    private LocalDateTime pending;
    private boolean self;
    private int startDayOfWeekFlag;

    ThisDayOfWeek(Week week, int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.week = week;
        this.tags.add(new DayOfWeekTag(dayOfWeek));
        this.startDayOfWeekFlag = dayOfWeek;
        rewind();
    }

    @Override
    public TheDayOfWeek andDay(int dayOfWeek) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        this.tags.add(new DayOfWeekTag(dayOfWeek));
        this.startDayOfWeekFlag = dayOfWeek;
        rewind();
        return this;
    }

    @Override
    public TheDayOfWeek toDay(int dayOfWeek, int interval) {
        ChronoField.DAY_OF_WEEK.checkValidValue(dayOfWeek);
        if (startDayOfWeekFlag >= dayOfWeek) {
            throw new IllegalArgumentException(startDayOfWeekFlag + ">=" + dayOfWeek);
        }
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        final int fromDay = startDayOfWeekFlag;
        this.tags.removeIf(tag -> tag.from == fromDay && tag.to == fromDay);
        this.tags.add(new DayOfWeekTag(fromDay, dayOfWeek, interval));
        this.startDayOfWeekFlag = dayOfWeek;
        rewind();
        return this;
    }

    /** Rebuilds the weekday list and puts the iteration back at its first occurrence. */
    private void rewind() {
        Set<Integer> distinct = new LinkedHashSet<>();
        tags.forEach(tag -> tag.collectInto(distinct));
        this.values = distinct.stream().sorted().collect(Collectors.toList());
        this.day = null;
        this.pending = null;
        this.current = candidates();
        this.index = 0;
        this.day = findNext();
        this.self = this.day != null;
    }

    /**
     * The date a weekday resolves to inside the week the parent stands on, or {@code null} when
     * that month holds no such occurrence.
     */
    private int currentOrdinal() {
        return week instanceof WeekOrdinal ? ((WeekOrdinal) week).currentOrdinal()
                : WeekOfMonth.LAST;
    }

    /** The dates the configured weekdays land on in the week the parent stands on. */
    private List<LocalDateTime> candidates() {
        List<LocalDateTime> list = new ArrayList<>();
        if (!(week instanceof WeekOrdinal)) {
            // A week of the year is an ISO calendar week, so each weekday simply sits inside it.
            for (Integer value : values) {
                list.add(week.getTime().with(WeekFields.ISO.dayOfWeek(), value));
            }
            return list;
        }
        int ordinal = currentOrdinal();
        for (Integer value : values) {
            if (ordinal == WeekOfMonth.EVERY) {
                list.addAll(WeekOfMonth.allOccurrences(week.getTime(), value));
            } else {
                LocalDateTime occurrence =
                        WeekOfMonth.occurrence(week.getTime(), ordinal, value);
                if (occurrence != null) {
                    list.add(occurrence);
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    /** Walks forward until the next existing occurrence, rolling into later weeks as needed. */
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
        return new ThisHour(IteratorUtils.getFirst(copy, copy), hour);
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
    public CronExpression getParent() {
        return week;
    }

    @Override
    public String toCronString() {
        return tags.stream().map(DayOfWeekTag::getTag).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    /** Renders one weekday, or one weekday range, the way the parent week requires. */
    private class DayOfWeekTag implements Serializable {

        private static final long serialVersionUID = -2056254572492151394L;
        private final int from;
        private final int to;
        private final int interval;

        DayOfWeekTag(int dayOfWeek) {
            this(dayOfWeek, dayOfWeek, 1);
        }

        DayOfWeekTag(int from, int to, int interval) {
            this.from = from;
            this.to = to;
            this.interval = interval;
        }

        void collectInto(Set<Integer> distinct) {
            for (int i = from; i <= to; i += interval) {
                distinct.add(i);
            }
        }

        String getTag() {
            List<Integer> ordinals = ordinals();
            if (ordinals.isEmpty() && from != to) {
                // A plain range keeps the compact 'MON-FRI' form; an ordinal has to be repeated
                // per weekday because cron offers no range form for '#' or 'L'.
                String range = name(from) + "-" + name(to);
                return interval > 1 ? range + "/" + interval : range;
            }
            List<String> list = new ArrayList<>();
            for (int i = from; i <= to; i += interval) {
                if (ordinals.isEmpty()) {
                    list.add(name(i));
                } else {
                    for (Integer ordinal : ordinals) {
                        list.add(name(i) + suffix(ordinal));
                    }
                }
            }
            return String.join(",", list);
        }

        /** The ordinals to render, empty when the weekday needs no {@code #} or {@code L}. */
        private List<Integer> ordinals() {
            if (week instanceof WeekOrdinal && !((WeekOrdinal) week).isEveryOrdinal()) {
                return ((WeekOrdinal) week).ordinals();
            }
            return Collections.emptyList();
        }

        private String suffix(int ordinal) {
            return ordinal == WeekOfMonth.LAST ? "L" : "#" + ordinal;
        }

        private String name(int dayOfWeek) {
            return getBuilder().isUseDayOfWeekAsNumber()
                    ? String.valueOf(AbbreviationUtils.toCronDayOfWeek(dayOfWeek))
                    : AbbreviationUtils.getDayOfWeekName(dayOfWeek);
        }

    }


    @Override
    public void takePendingValue() {
        this.self = false;
    }
}
