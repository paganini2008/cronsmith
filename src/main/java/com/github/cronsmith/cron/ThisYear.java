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
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.TreeMap;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisYear implements TheYear, Serializable {

    private static final long serialVersionUID = -5316436238766770045L;
    private final TreeMap<Integer, LocalDateTime> siblings = new TreeMap<>();
    private LocalDateTime year;
    private int index;
    private int lastYear;
    private final StringBuilder cron;

    public ThisYear(int year) {
        FieldAssertions.checkYear(year);
        LocalDateTime ldt = LocalDateTime.now().withYear(year);
        this.siblings.put(year, ldt);
        this.year = ldt;
        this.lastYear = year;
        this.cron = new StringBuilder().append(year);
    }

    @Override
    public TheYear andYear(int year) {
        return andYear(year, true);
    }

    private TheYear andYear(int year, boolean writeCron) {
        FieldAssertions.checkYear(year);
        LocalDateTime ldt = LocalDateTime.now().withYear(year);
        siblings.put(year, ldt);
        this.lastYear = year;
        if (writeCron) {
            this.cron.append(",").append(year);
        }
        return this;
    }

    @Override
    public TheYear toYear(int year, int interval) {
        FieldAssertions.checkYear(year);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        for (int i = lastYear + interval; i <= year; i += interval) {
            andYear(i, false);
        }
        if (interval > 1) {
            this.cron.append("-").append(year).append("/").append(interval);
        } else {
            this.cron.append("-").append(year);
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return year;
    }

    @Override
    public int getYear() {
        return year.getYear();
    }

    @Override
    public int getWeekCountOfYear() {
        return year.with(TemporalAdjusters.lastDayOfYear()).get(WeekFields.ISO.weekOfYear());
    }

    @Override
    public int getLastDayOfYear() {
        return year.with(TemporalAdjusters.lastDayOfYear()).getDayOfYear();
    }

    @Override
    public Month everyMonth(Function<Year, Integer> from, Function<Year, Integer> to,
            int interval) {
        final Year copy = (Year) this.copy();
        return new EveryMonth(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public TheDay day(int day) {
        final Year copy = (Year) this.copy();
        return new ThisDayOfYear(IteratorUtils.getFirst(copy), day);
    }

    @Override
    public TheWeek week(int week) {
        final Year copy = (Year) this.copy();
        return new ThisWeekOfYear(IteratorUtils.getFirst(copy), week);
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
    public boolean hasNext() {
        return index < siblings.size();
    }

    @Override
    public Year next() {
        year = IteratorUtils.get(siblings.values().iterator(), index++);
        return this;
    }

    @Override
    public CronExpression getParent() {
        return null;
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
        TheYear singleYear = new ThisYear(2021);
        singleYear = singleYear.andYear(2028).andYear(2024);
        Day day = singleYear.lastWeek().Mon().toFri();
        System.out.println(day);
    }

}
