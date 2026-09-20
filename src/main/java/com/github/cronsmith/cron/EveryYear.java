/*
 * Copyright 2026 Fred Feng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cronsmith.cron;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import com.github.cronsmith.CRON;
import com.github.cronsmith.utils.IteratorUtils;

/**
 * 
 * @Description: EveryYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryYear implements Year {

    private static final long serialVersionUID = 1487831872493410360L;

    EveryYear(CronBuilder builder, IntFunction<CronBuilder> from, int interval) {
        if (interval < 1) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.builder = builder;
        this.from = from;
        this.interval = interval;
        this.year = builder.getTime().withYear(getFromYear());
        this.self = true;

    }

    private final CronBuilder builder;
    private LocalDateTime year;
    private final IntFunction<CronBuilder> from;
    private final int interval;
    private boolean self;

    private int getFromYear() {
        int fromYear = from.apply(builder);
        if (fromYear > MAX_YEAR) {
            throw new IllegalArgumentException("Great than the maximum year: " + MAX_YEAR);
        }
        if (fromYear < builder.getStartTime().getYear()) {
            throw new IllegalArgumentException(
                    "Less than the minimum year: " + builder.getStartTime().getYear());
        }
        return fromYear;
    }

    @Override
    public int getYear() {
        return year.getYear();
    }

    @Override
    public LocalDateTime getTime() {
        return year;
    }

    @Override
    public int getWeekCountOfYear() {
        return year.with(TemporalAdjusters.lastDayOfYear()).get(WeekFields.ISO.weekOfYear());
    }

    @Override
    public int getLastDayOfYear(int n) {
        int lastDayOfYear = year.with(TemporalAdjusters.lastDayOfYear()).getDayOfYear();
        lastDayOfYear -= n;
        return lastDayOfYear;
    }

    @Override
    public int getLatestWeekdayOfYear(int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        LocalDateTime ldt = year.withDayOfYear(dayOfYear);
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
        return nextDay.getDayOfYear();
    }

    @Override
    public boolean hasNext() {
        return (self || year.getYear() + interval <= MAX_YEAR);
    }

    @Override
    public Year next() {
        if (self) {
            self = false;
        } else {
            year = year.plusYears(interval);
        }
        return this;
    }

    @Override
    public Month everyMonth(IntFunction<Year> from, int interval) {
        final Year copy = (Year) this.copy();
        return new EveryMonth(IteratorUtils.getFirst(copy), from, interval);
    }

    @Override
    public TheDay day(int dayOfMonth) {
        final Year copy = (Year) this.copy();
        return new ThisDayOfYear(IteratorUtils.getFirst(copy), dayOfMonth);
    }

    @Override
    public TheWeek week(int weekOfYear) {
        final Year copy = (Year) this.copy();
        return new ThisWeekOfYear(IteratorUtils.getFirst(copy), weekOfYear);
    }

    @Override
    public TheMonth month(int month) {
        final Year copy = (Year) this.copy();
        return new ThisMonth(IteratorUtils.getFirst(copy), month);
    }

    // lastDay() intentionally not overridden: the Year default routes the year's last day to
    // traditional "L in December" (a fixed calendar date), keeping it out of YCRON.

    @Override
    public Week lastWeek() {
        final Year copy = (Year) this.copy();
        return new LastWeekOfYear(IteratorUtils.getFirst(copy, copy));
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public CronExpression getParent() {
        return builder;
    }

    @Override
    public String toCronString() {
        int fromYear = getFromYear();
        String str = "";
        boolean slashed = interval > 1;
        if (fromYear == getBuilder().getStartTime().getYear()) {
            if (slashed) {
                str = String.valueOf(fromYear);
            } else {
                str = "*";
            }
        } else if (fromYear > getBuilder().getStartTime().getYear()) {
            str = String.valueOf(fromYear);
            slashed = true;
        }
        return slashed ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }
}
