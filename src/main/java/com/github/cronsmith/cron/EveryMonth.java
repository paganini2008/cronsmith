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
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;
import com.github.cronsmith.parser.Utils;

/**
 * 
 * @Description: EveryMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryMonth implements Month, Serializable {

    private static final long serialVersionUID = -7085376125910878673L;
    private Year year;
    private LocalDateTime month;
    private final Function<Year, Integer> from;
    private final Function<Year, Integer> to;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryMonth(Year year, Function<Year, Integer> from, Function<Year, Integer> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.year = year;
        this.from = from;
        this.to = to;

        this.month = year.getTime().withMonth(getFromMonth()).withDayOfMonth(1).withHour(0)
                .withMinute(0).withSecond(0);
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private int getFromMonth() {
        int fromMonth = from.apply(year);
        FieldAssertions.checkMonth(fromMonth);
        return fromMonth;
    }

    private int getToMonth() {
        int toMonth = to.apply(year);
        FieldAssertions.checkMonth(toMonth);
        return toMonth;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || month.getMonthValue() + interval <= getToMonth();
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                month = month.withYear(year.getYear()).withMonth(getFromMonth());
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Month next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                month = month.plusMonths(interval);
            } else {
                forward = true;
            }
        }
        return this;
    }

    @Override
    public int getYear() {
        return month.getYear();
    }

    @Override
    public int getMonth() {
        return month.getMonthValue();
    }

    @Override
    public int getLastDay(int n) {
        int lastDayOfMonth = month.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
        if (n < lastDayOfMonth) {
            lastDayOfMonth -= n;
        }
        return lastDayOfMonth;
    }

    @Override
    public int getLastWeekDay() {
        return getLatestWeekday(getLastDay());
    }

    @Override
    public Day latestWeekday(int dayOfMonth) {
        return new LatestWeekdayOfMonth(this, dayOfMonth);
    }

    @Override
    public int getLatestWeekday(int dayOfMonth) {
        FieldAssertions.checkDayOfMonth(this, dayOfMonth);
        LocalDateTime ldt = month.withDayOfMonth(dayOfMonth);
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
    public int getWeekCountOfMonth() {
        return month.get(WeekFields.ISO.weekOfMonth());
    }

    @Override
    public LocalDateTime getTime() {
        return month;
    }

    @Override
    public TheDay day(int dayOfMonth) {
        final Month copy = (Month) this.copy();
        return new ThisDay(IteratorUtils.getFirst(copy), dayOfMonth);
    }

    @Override
    public Day lastDay(int n) {
        final Month copy = (Month) this.copy();
        return new LastDayOfMonth(IteratorUtils.getFirst(copy), n);
    }

    @Override
    public Day everyDay(Function<Month, Integer> from, Function<Month, Integer> to, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryDay(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public TheWeek week(int weekOfMonth) {
        final Month copy = (Month) this.copy();
        return new ThisWeek(IteratorUtils.getFirst(copy), weekOfMonth);
    }

    @Override
    public TheDayOfWeekInMonth dayOfWeek(int weekOfMonth, int dayOfWeek) {
        final Month copy = (Month) this.copy();
        return new ThisDayOfWeekInMonth(IteratorUtils.getFirst(copy), weekOfMonth, dayOfWeek);
    }

    @Override
    public Week lastWeek() {
        final Month copy = (Month) this.copy();
        return new LastWeekOfMonth(IteratorUtils.getFirst(copy));
    }

    @Override
    public Week everyWeek(Function<Month, Integer> from, Function<Month, Integer> to,
            int interval) {
        final Month copy = (Month) this.copy();
        return new EveryWeek(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public String toCronString() {
        int fromMonth = getFromMonth();
        int toMonth = getToMonth();
        String str;
        if (fromMonth == 1 && toMonth == 12) {
            str = "*";
        } else {
            str = String.format("%s-%s", Utils.getMonthName(fromMonth),
                    Utils.getMonthName(toMonth));
        }
        return interval > 1 ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
