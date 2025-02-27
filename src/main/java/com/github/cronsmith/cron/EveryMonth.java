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
import com.github.cronsmith.CollectionUtils;

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
    private final int fromMonth;
    private final int toMonth;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryMonth(Year year, Function<Year, Integer> from, Function<Year, Integer> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.year = year;
        this.fromMonth = from.apply(year);
        FieldAssertions.checkMonth(fromMonth);
        this.month = year.getTime().withMonth(fromMonth).withDayOfMonth(1).withHour(0).withMinute(0)
                .withSecond(0);
        this.interval = interval;
        this.toMonth = to.apply(year);
        FieldAssertions.checkMonth(toMonth);
        this.self = true;
        this.forward = true;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || month.getMonth().getValue() + interval <= toMonth;
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                month = month.withYear(year.getYear()).withMonth(fromMonth);
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
    public int getLastDay() {
        return month.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
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
        return new ThisDay(CollectionUtils.getFirst(copy), dayOfMonth);
    }

    @Override
    public Day lastDay() {
        final Month copy = (Month) this.copy();
        return new LastDayOfMonth(CollectionUtils.getFirst(copy));
    }

    @Override
    public Day everyDay(Function<Month, Integer> from, Function<Month, Integer> to, int interval) {
        final Month copy = (Month) this.copy();
        return new EveryDay(CollectionUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public TheWeek week(int weekOfMonth) {
        final Month copy = (Month) this.copy();
        return new ThisWeek(CollectionUtils.getFirst(copy), weekOfMonth);
    }

    @Override
    public TheDayOfWeekInMonth dayOfWeek(int week, int dayOfWeek) {
        final Month copy = (Month) this.copy();
        return new ThisDayOfWeekInMonth(CollectionUtils.getFirst(copy), week, dayOfWeek);
    }

    @Override
    public Week lastWeek() {
        final Month copy = (Month) this.copy();
        return new LastWeekOfMonth(CollectionUtils.getFirst(copy));
    }

    @Override
    public Week everyWeek(Function<Month, Integer> from, Function<Month, Integer> to,
            int interval) {
        final Month copy = (Month) this.copy();
        return new EveryWeek(CollectionUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public String toCronString() {
        String s = toMonth != 12 ? fromMonth + "-" + toMonth : fromMonth + "";
        return interval > 1 ? s + "/" + interval : "*";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
