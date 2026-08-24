package com.github.cronsmith.cron;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;

/**
 *
 * TimeSlot rounds a date-time down to the beginning of the slot it belongs to.
 * <p>
 * Fields that start at 1 (month, day-of-month, day-of-year, day-of-week) are snapped with a
 * one-based formula, otherwise a value such as month 1 with a span of 2 would be rounded down to 0
 * and rejected by {@link LocalDate}.
 *
 * @Description: TimeSlot
 * @Author: Fred Feng
 * @Date: 07/04/2025
 * @Version 1.0.0
 */
public enum TimeSlot {

    MONTH {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            checkSpan(span);
            int month = floorOneBased(ldt.getMonthValue(), span);
            LocalDate date = LocalDate.of(ldt.getYear(), month, 1);
            return LocalDateTime.of(
                    date.withDayOfMonth(Math.min(ldt.getDayOfMonth(), date.lengthOfMonth())),
                    ldt.toLocalTime());
        }
    },

    DAY_OF_YEAR {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            checkSpan(span);
            int dayOfYear = floorOneBased(ldt.getDayOfYear(), span);
            return LocalDateTime.of(ldt.toLocalDate().withDayOfYear(dayOfYear), ldt.toLocalTime());
        }
    },

    DAY_OF_WEEK {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            checkSpan(span);
            int dayOfWeek = floorOneBased(ldt.getDayOfWeek().getValue(), span);
            return LocalDateTime.of(
                    ldt.toLocalDate().with(WeekFields.ISO.dayOfWeek(), dayOfWeek),
                    ldt.toLocalTime());
        }
    },

    DAY {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            checkSpan(span);
            int dayOfMonth = floorOneBased(ldt.getDayOfMonth(), span);
            return LocalDateTime.of(
                    LocalDate.of(ldt.getYear(), ldt.getMonthValue(), dayOfMonth),
                    ldt.toLocalTime());
        }
    },

    HOUR {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            checkSpan(span);
            int hour = ldt.getHour();
            return LocalDateTime.of(ldt.toLocalDate(),
                    LocalTime.of(hour - hour % span, ldt.getMinute(), ldt.getSecond()));
        }
    },

    MINUTE {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            checkSpan(span);
            int minute = ldt.getMinute();
            return LocalDateTime.of(ldt.toLocalDate(),
                    LocalTime.of(ldt.getHour(), minute - minute % span, ldt.getSecond()));
        }
    },

    SECOND {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            checkSpan(span);
            int second = ldt.getSecond();
            return LocalDateTime.of(ldt.toLocalDate(),
                    LocalTime.of(ldt.getHour(), ldt.getMinute(), second - second % span));
        }
    };

    public abstract LocalDateTime adjust(LocalDateTime ldt, int span);

    static void checkSpan(int span) {
        if (span < 1) {
            throw new IllegalArgumentException("Invalid span: " + span);
        }
    }

    static int floorOneBased(int value, int span) {
        return ((value - 1) / span) * span + 1;
    }

}
