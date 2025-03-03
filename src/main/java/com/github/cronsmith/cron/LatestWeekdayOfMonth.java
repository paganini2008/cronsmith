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
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: LatestWeekdayOfMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LatestWeekdayOfMonth implements Day, Serializable {

    private static final long serialVersionUID = -1745938729702028629L;

    private Month month;
    private LocalDateTime day;
    private boolean self;
    private final int dayOfMonth;

    LatestWeekdayOfMonth(Month month, int dayOfMonth) {
        this.month = month;
        this.day = month.getTime()
                .withDayOfMonth(month.getLatestWeekday(Math.min(dayOfMonth, month.getLastDay())));
        this.dayOfMonth = dayOfMonth;
        this.self = true;
    }

    @Override
    public boolean hasNext() {
        boolean next = self;
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                day = day.withYear(month.getYear()).withMonth(month.getMonth()).withDayOfMonth(
                        month.getLatestWeekday(Math.min(dayOfMonth, month.getLastDay())));
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        if (self) {
            self = false;
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public CronExpression getParent() {
        return month;
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
    public TheHour hour(int hourOfDay) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hourOfDay);
    }

    @Override
    public Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public String toCronString() {
        if (dayOfMonth == month.getLastDay()) {
            return "LW";
        }
        return dayOfMonth + "W";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
