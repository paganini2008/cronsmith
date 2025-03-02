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
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryDay
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryDay implements Day, Serializable {

    private static final long serialVersionUID = -2114922383566430661L;
    private Month month;
    private LocalDateTime day;
    private final int fromDay;
    private final int toDay;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryDay(Month month, Function<Month, Integer> from, Function<Month, Integer> to,
            int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.fromDay = from.apply(month);
        FieldAssertions.checkDayOfMonth(month, fromDay);
        this.day = month.getTime().withDayOfMonth(fromDay).withHour(0).withMinute(0).withSecond(0);
        this.interval = interval;
        this.toDay = to.apply(month);
        FieldAssertions.checkDayOfMonth(month, toDay);
        this.self = true;
        this.forward = true;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || day.getDayOfMonth() + interval <= toDay;
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                day = day.withYear(month.getYear()).withMonth(month.getMonth())
                        .withDayOfMonth(fromDay);
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                day = day.plusDays(interval);
            } else {
                forward = true;
            }
        }
        return this;
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
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public TheHour hour(int hour) {
        final Day copy = (Day) this.copy();
        return new ThisHour(IteratorUtils.getFirst(copy), hour);
    }

    @Override
    public Hour everyHour(Function<Day, Integer> from, Function<Day, Integer> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        String s = toDay != month.getLastDay() ? fromDay + "-" + toDay : fromDay + "";
        return interval > 1 ? s + "/" + interval : "*";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
