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
 * @Description: LastWeekOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastWeekOfYear implements Week, Serializable {

    private static final long serialVersionUID = -2099892494149322184L;
    private Year year;
    private LocalDateTime week;
    private boolean self;

    LastWeekOfYear(Year year) {
        this.year = year;
        this.week = init(year.getTime());
        this.self = true;
    }

    private LocalDateTime init(LocalDateTime ldt) {
        LocalDateTime lastDayOfYear = ldt.with(TemporalAdjusters.lastDayOfYear());
        if (lastDayOfYear.getDayOfWeek() != DayOfWeek.SUNDAY) {
            lastDayOfYear = lastDayOfYear.minusWeeks(1);
        }
        return lastDayOfYear.with(WeekFields.ISO.dayOfWeek(), 1).withHour(0).withMinute(0)
                .withSecond(0);
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
        return week.get(WeekFields.ISO.weekOfMonth());
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.ISO.weekOfYear());
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
            if (year.hasNext()) {
                year = year.next();
                week = week.withYear(year.getYear());
                week = init(week);
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
        return year.Dec();
    }

    @Override
    public String toCronString() {
        return "";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
