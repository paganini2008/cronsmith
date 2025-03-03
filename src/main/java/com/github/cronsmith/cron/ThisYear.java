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
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Epoch epoch;
    private LocalDateTime year;
    private int index;
    private int lastYear;
    private final StringBuilder cron;

    public ThisYear(Epoch epoch, int year) {
        FieldAssertions.checkYear(year);
        this.epoch = epoch;
        DateTimeSupplier supplier = () -> epoch.getTime().withYear(year);
        this.siblings.put(year, supplier);
        this.year = supplier.get();
        this.lastYear = year;
        this.cron = new StringBuilder().append(year);
    }

    @Override
    public TheYear andYear(int year) {
        return andYear(year, true);
    }

    private TheYear andYear(int year, boolean writeCron) {
        FieldAssertions.checkYear(year);
        DateTimeSupplier supplier = () -> epoch.getTime().withYear(year);
        siblings.put(year, supplier);
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
    public int getLastDayOfYear(int n) {
        int lastDayOfYear = year.with(TemporalAdjusters.lastDayOfYear()).getDayOfYear();
        if (n < lastDayOfYear) {
            lastDayOfYear -= n;
        }
        return lastDayOfYear;
    }

    @Override
    public Month everyMonth(IntFunction<Year> from, IntFunction<Year> to, int interval) {
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
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        year = supplier.get();
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
        TheYear singleYear = Epoch.getInstance().year(2025);
        singleYear = singleYear.andYear(2028).andYear(2030).toYear(2040, 2);
        // CronExpression cronExpression = singleYear.Apr().andJuly().andSept().toDec().week(1)
        // .andWeek(2).Sat().andSun().at(9, 15);
        // cronExpression = singleYear.week(40).Mon().andTues().toDay(7, 2).at(9, 20);
        CronExpression cronExpression = singleYear.day(205);
        // Day day = singleYear.everyMonth().everyWeek().Mon().toFri();
        // System.out.println(day);
        // Hour hour = day.everyHour(3);

        System.out.println(cronExpression);
        // day.forEach(l -> {
        // System.out.println(l);
        // }, 100);
    }

}
