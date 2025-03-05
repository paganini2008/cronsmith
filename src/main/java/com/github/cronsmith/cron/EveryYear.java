/**
 * Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryYear implements Year, Serializable {

    private static final long serialVersionUID = 1487831872493410360L;

    EveryYear(Epoch epoch, IntFunction<Epoch> from, IntFunction<Epoch> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.epoch = epoch;
        this.from = from;
        this.to = to;

        this.year = epoch.getTime().withYear(getFromYear());
        this.interval = interval;
        this.self = true;

    }

    private final Epoch epoch;
    private LocalDateTime year;
    private final IntFunction<Epoch> from;
    private final IntFunction<Epoch> to;
    private final int interval;
    private boolean self;

    private int getFromYear() {
        int fromYear = from.apply(epoch);
        ChronoField.YEAR.checkValidValue(fromYear);
        return fromYear;
    }

    private int getToYear() {
        int toYear = to.apply(epoch);
        ChronoField.YEAR.checkValidValue(toYear);
        return toYear;
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
        if (n < lastDayOfYear) {
            lastDayOfYear -= n;
        }
        return lastDayOfYear;
    }

    @Override
    public int getLastWeekdayOfYear(int dayOfYear) {
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
        return nextDay.getDayOfMonth();
    }

    @Override
    public boolean hasNext() {
        return self || year.getYear() + interval <= getToYear();
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
    public Month everyMonth(IntFunction<Year> from, IntFunction<Year> to, int interval) {
        final Year copy = (Year) this.copy();
        return new EveryMonth(IteratorUtils.getFirst(copy), from, to, interval);
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

    @Override
    public Week lastWeek() {
        final Year copy = (Year) this.copy();
        return new LastWeekOfYear(IteratorUtils.getFirst(copy));
    }

    @Override
    public CronExpression getParent() {
        return null;
    }

    @Override
    public String toCronString() {
        int fromYear = getFromYear();
        int toYear = getToYear();
        boolean slashed = false;
        String str;
        if (fromYear == LocalDate.now().getYear() && toYear == MAX_YEAR) {
            str = "*";
        } else if (fromYear > LocalDate.now().getYear() && toYear == MAX_YEAR) {
            str = String.valueOf(fromYear);
            slashed = true;
        } else {
            str = fromYear + "-" + toYear;
        }
        return interval > 1 ? str + "/" + interval : slashed ? str + "/1" : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }
}
