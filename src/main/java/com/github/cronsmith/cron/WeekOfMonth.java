package com.github.cronsmith.cron;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Resolves "the nth weekday of the month" and "the last weekday of the month", which is what the
 * cron tags {@code #} and {@code L} mean in the day-of-week field.
 * <p>
 * This is deliberately not an ISO week-of-month: ISO counts calendar weeks, so its first week of a
 * month may start in the previous month, whereas {@code FRI#1} always means the first Friday that
 * falls inside the month itself.
 *
 * @Description: WeekOfMonth
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
abstract class WeekOfMonth {

    /** Ordinal standing for the last occurrence of a weekday in its month. */
    static final int LAST = -1;

    /** Ordinal standing for "every occurrence", which is the plain {@code MON} form. */
    static final int EVERY = 0;

    /** The highest ordinal a month can hold; not every month reaches it. */
    static final int MAX_ORDINAL = 5;

    /**
     * The date of the {@code ordinal}-th {@code dayOfWeek} of the month {@code anyDayOfMonth} falls
     * in, or {@code null} when the month has no such occurrence - a fifth Friday, typically.
     *
     * @param anyDayOfMonth any date inside the month of interest
     * @param ordinal 1..5 or {@link #LAST}
     * @param dayOfWeek MON=1 .. SUN=7
     */
    static LocalDate occurrence(LocalDate anyDayOfMonth, int ordinal, int dayOfWeek) {
        if (ordinal == EVERY) {
            throw new IllegalStateException("An 'every occurrence' week resolves to a list");
        }
        DayOfWeek target = DayOfWeek.of(dayOfWeek);
        LocalDate firstOfMonth = anyDayOfMonth.withDayOfMonth(1);
        if (ordinal == LAST) {
            return firstOfMonth.with(TemporalAdjusters.lastInMonth(target));
        }
        LocalDate candidate =
                firstOfMonth.with(TemporalAdjusters.dayOfWeekInMonth(ordinal, target));
        return candidate.getMonthValue() == firstOfMonth.getMonthValue()
                && candidate.getYear() == firstOfMonth.getYear() ? candidate : null;
    }

    /**
     * Keeps the time of day of {@code template} while moving it onto the resolved occurrence.
     * Returns {@code null} when the month has no such occurrence.
     */
    static LocalDateTime occurrence(LocalDateTime template, int ordinal, int dayOfWeek) {
        LocalDate date = occurrence(template.toLocalDate(), ordinal, dayOfWeek);
        return date != null ? LocalDateTime.of(date, template.toLocalTime()) : null;
    }

    /** Which ordinal a date holds inside its own month: the 8th of a month is always the 2nd. */
    static int ordinalOf(LocalDate date) {
        return (date.getDayOfMonth() - 1) / 7 + 1;
    }

    /** A representative date for an ordinal: the first day of the seven-day block it covers. */
    static LocalDateTime startOf(LocalDateTime anyDayOfMonth, int ordinal) {
        LocalDate firstOfMonth = anyDayOfMonth.toLocalDate().withDayOfMonth(1);
        int dayOfMonth;
        if (ordinal == LAST) {
            dayOfMonth = Math.max(1, firstOfMonth.lengthOfMonth() - 6);
        } else if (ordinal == EVERY) {
            dayOfMonth = 1;
        } else {
            dayOfMonth = Math.min(7 * (ordinal - 1) + 1, firstOfMonth.lengthOfMonth());
        }
        return LocalDateTime.of(firstOfMonth.withDayOfMonth(dayOfMonth),
                anyDayOfMonth.toLocalTime());
    }

    /** Every occurrence of {@code dayOfWeek} inside the month, in calendar order. */
    static List<LocalDateTime> allOccurrences(LocalDateTime anyDayOfMonth,
            int dayOfWeek) {
        List<LocalDateTime> list = new ArrayList<>();
        LocalDate firstOfMonth = anyDayOfMonth.toLocalDate().withDayOfMonth(1);
        LocalDate date = firstOfMonth
                .with(TemporalAdjusters.dayOfWeekInMonth(1, DayOfWeek.of(dayOfWeek)));
        while (date.getMonthValue() == firstOfMonth.getMonthValue()
                && date.getYear() == firstOfMonth.getYear()) {
            list.add(LocalDateTime.of(date, anyDayOfMonth.toLocalTime()));
            date = date.plusWeeks(1);
        }
        return list;
    }

}
