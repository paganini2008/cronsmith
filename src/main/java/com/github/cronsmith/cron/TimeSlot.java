package com.github.cronsmith.cron;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;

/**
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
            int year = ldt.getYear();
            int month = ldt.getMonthValue();
            int dayOfMonth = ldt.getDayOfMonth();
            return LocalDateTime.of(LocalDate.of(year, (month - month % span), dayOfMonth),
                    ldt.toLocalTime());
        }
    },

    DAY_OF_YEAR {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            int dayOfYear = ldt.getDayOfYear();
            return LocalDateTime.of(ldt.toLocalDate().withDayOfYear((dayOfYear - dayOfYear % span)),
                    ldt.toLocalTime());
        }
    },

    DAY_OF_WEEK {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            int dayOfWeek = ldt.getDayOfWeek().getValue();
            return LocalDateTime.of(ldt.toLocalDate().with(WeekFields.ISO.dayOfWeek(),
                    (dayOfWeek - dayOfWeek % span)), ldt.toLocalTime());
        }
    },

    DAY {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            int year = ldt.getYear();
            int month = ldt.getMonthValue();
            int dayOfMonth = ldt.getDayOfMonth();
            return LocalDateTime.of(LocalDate.of(year, month, (dayOfMonth - dayOfMonth % span)),
                    ldt.toLocalTime());
        }
    },

    HOUR {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            int hour = ldt.getHour();
            int minute = ldt.getMinute();
            int second = ldt.getSecond();
            return LocalDateTime.of(ldt.toLocalDate(),
                    LocalTime.of(hour - hour % span, minute, second));
        }
    },

    MINUTE {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            int hour = ldt.getHour();
            int minute = ldt.getMinute();
            int second = ldt.getSecond();
            return LocalDateTime.of(ldt.toLocalDate(),
                    LocalTime.of(hour, minute - minute % span, second));
        }
    },

    SECOND {
        @Override
        public LocalDateTime adjust(LocalDateTime ldt, int span) {
            int hour = ldt.getHour();
            int minute = ldt.getMinute();
            int second = ldt.getSecond();
            return LocalDateTime.of(ldt.toLocalDate(),
                    LocalTime.of(hour, minute, second - second % span));
        }
    };

    public abstract LocalDateTime adjust(LocalDateTime ldt, int span);

}
