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
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.CollectionUtils;

/**
 * 
 * @Description: LastWeekOfMonth
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastWeekOfMonth implements Week, Serializable {

    private static final long serialVersionUID = 2658610900522209361L;
    private Month month;
    private LocalDateTime week;
    private boolean self;

    LastWeekOfMonth(Month month) {
        this.month = month;
        this.week = month.getTime().with(TemporalAdjusters.lastDayOfMonth())
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).withHour(0).withMinute(0)
                .withSecond(0);
        this.self = true;
    }

    @Override
    public LocalDateTime getTime() {
        return week;
    }

    @Override
    public int getYear() {
        return week.getYear();
    }

    @Override
    public int getMonth() {
        return week.getMonthValue();
    }

    @Override
    public int getWeek() {
        return week.get(WeekFields.of(Locale.getDefault()).weekOfMonth());
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.of(Locale.getDefault()).weekOfYear());
    }

    @Override
    public TheDayOfWeek day(int day) {
        final Week copy = (Week) this.copy();
        return new ThisDayOfWeek(CollectionUtils.getFirst(copy), day);
    }

    @Override
    public Day everyDay(Function<Week, Integer> from, Function<Week, Integer> to, int interval) {
        final Week copy = (Week) this.copy();
        return new EveryDayOfWeek(CollectionUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = self;
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                week = week.withYear(month.getYear()).withMonth(month.getMonth()).with(
                        WeekFields.of(Locale.getDefault()).weekOfMonth(),
                        month.getWeekCountOfMonth());
                next = true;
            }
        }
        return next;
    }

    @Override
    public Week next() {
        if (self) {
            self = false;
        }
        return this;
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return "L";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
