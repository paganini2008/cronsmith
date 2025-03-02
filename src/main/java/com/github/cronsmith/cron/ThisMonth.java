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
import java.util.TreeMap;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisMonth
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class ThisMonth implements TheMonth, Serializable {

    private static final long serialVersionUID = 229203112866380942L;
    private final TreeMap<Integer, LocalDateTime> siblings = new TreeMap<>();
    private Year year;
    private int index;
    private LocalDateTime month;
    private int lastMonth;
    private final StringBuilder cron;

    ThisMonth(Year year, int month) {
        FieldAssertions.checkMonth(month);
        this.year = year;
        LocalDateTime ldt = year.getTime().withMonth(month);
        this.siblings.put(month, ldt);
        this.month = ldt;
        this.lastMonth = month;
        this.cron = new StringBuilder().append(month);
    }

    @Override
    public TheMonth andMonth(int month) {
        return andMonth(month, true);
    }

    private TheMonth andMonth(int month, boolean writeCron) {
        FieldAssertions.checkMonth(month);
        LocalDateTime ldt = year.getTime().withMonth(month);
        this.siblings.put(month, ldt);
        this.lastMonth = month;
        if (writeCron) {
            this.cron.append(",").append(CalendarUtils.getMonthName(month));
        }
        return this;
    }

    @Override
    public TheMonth toMonth(int month, int interval) {
        FieldAssertions.checkMonth(month);
        for (int i = lastMonth + interval; i <= month; i += interval) {
            andMonth(i, false);
        }
        if (interval > 1) {
            this.cron.append("-").append(month).append("/").append(interval);
        } else {
            this.cron.append("-").append(month);
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return month;
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
    public TheDay day(int day) {
        final Month copy = (Month) this.copy();
        return new ThisDay(IteratorUtils.getFirst(copy), day);
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
    public TheWeek week(int week) {
        final Month copy = (Month) this.copy();
        return new ThisWeek(IteratorUtils.getFirst(copy), week);
    }

    @Override
    public TheDayOfWeekInMonth dayOfWeek(int week, int dayOfWeek) {
        final Month copy = (Month) this.copy();
        return new ThisDayOfWeekInMonth(IteratorUtils.getFirst(copy), week, dayOfWeek);
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
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Month next() {
        month = IteratorUtils.get(siblings.values().iterator(), index++);
        month = month.withYear(year.getYear());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year;
    }

    @Override
    public String toCronString() {
        return this.cron.toString();
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    public static void main(String[] args) {
        TheYear singleYear = Epoch.getInstance().year(2021);
        singleYear = singleYear.andYear(2024).andYear(2028);
        TheMonth singleMonth = singleYear.July().andAug().andMonth(11);
        Day every = singleMonth.lastWeek().Fri().andSat();
        System.out.println(every);
    }

}
