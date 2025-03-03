/**
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
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
import java.util.Map;
import java.util.TreeMap;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfYear implements TheDay, Serializable {

    private static final long serialVersionUID = -8235489088108418524L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Year year;
    private int index;
    private LocalDateTime day;
    private int lastDay;

    ThisDayOfYear(Year year, int dayOfYear) {
        FieldAssertions.checkDayOfYear(year, dayOfYear);
        this.year = year;
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(dayOfYear).withHour(0)
                .withMinute(0).withSecond(0);
        this.siblings.put(dayOfYear, supplier);
        this.day = supplier.get();
    }

    @Override
    public int getYear() {
        return day.getYear();
    }

    @Override
    public int getMonth() {
        return day.getMonthValue();
    }

    @Override
    public int getDay() {
        return day.getDayOfMonth();
    }

    @Override
    public int getDayOfWeek() {
        return day.getDayOfWeek().getValue();
    }

    @Override
    public int getDayOfYear() {
        return day.getDayOfYear();
    }

    @Override
    public TheHour hour(int hour) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hour);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public TheDay andDay(int dayOfYear) {
        FieldAssertions.checkDayOfYear(year, dayOfYear);
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(dayOfYear).withHour(0)
                .withMinute(0).withSecond(0);
        this.siblings.put(dayOfYear, supplier);
        this.lastDay = dayOfYear;
        return this;
    }


    @Override
    public TheDay toDay(int day, int interval) {
        FieldAssertions.checkDayOfYear(year, day);
        for (int dayOfYear = lastDay + interval; dayOfYear < day; dayOfYear += interval) {
            andDay(dayOfYear);
        }
        return this;
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
    public Day next() {
        Map.Entry<Integer, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index++);
        day = entry.getValue().get();
        day = day.withYear(year.getYear());
        day = day.withDayOfYear(Math.min(entry.getKey(), year.getLastDayOfYear()));
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year.month(getMonth());
    }

    @Override
    public String toCronString() {
        return String.valueOf(getDay());
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
