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
import java.util.Map;
import java.util.TreeMap;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDay
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDay implements TheDay, Serializable {

    private static final long serialVersionUID = -6007054113405112202L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Month month;
    private int index;
    private LocalDateTime day;
    private int lastDay;
    private final StringBuilder cron;

    ThisDay(Month month, int dayOfMonth) {
        FieldAssertions.checkDayOfMonth(month, dayOfMonth);
        this.month = month;
        DateTimeSupplier supplier = () -> month.getTime().withDayOfMonth(dayOfMonth);
        this.siblings.put(dayOfMonth, supplier);
        this.day = supplier.get();
        this.lastDay = dayOfMonth;
        this.cron = new StringBuilder().append(dayOfMonth);
    }

    @Override
    public TheDay andDay(int dayOfMonth) {
        return andDay(dayOfMonth, true);
    }

    private TheDay andDay(int dayOfMonth, boolean writeCron) {
        FieldAssertions.checkDayOfMonth(month, dayOfMonth);
        DateTimeSupplier supplier = () -> month.getTime().withDayOfMonth(dayOfMonth);
        this.siblings.put(dayOfMonth, supplier);
        this.lastDay = dayOfMonth;
        if (writeCron) {
            this.cron.append(",").append(dayOfMonth);
        }
        return this;
    }

    @Override
    public TheDay toDay(int dayOfMonth, int interval) {
        FieldAssertions.checkDayOfMonth(month, dayOfMonth);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        for (int i = lastDay + interval; i <= dayOfMonth; i += interval) {
            andDay(i, false);
        }
        if (interval > 1) {
            this.cron.append("-").append(dayOfMonth).append("/").append(interval);
        } else {
            this.cron.append("-").append(dayOfMonth);
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return day;
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
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
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
        day = day.withYear(month.getYear()).withMonth(month.getMonth())
                .withDayOfMonth(Math.min(entry.getKey(), month.getLastDay()));
        return this;
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return this.cron.toString();
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
