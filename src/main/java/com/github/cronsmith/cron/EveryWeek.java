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
import java.time.temporal.WeekFields;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryWeek implements Week, Serializable {

    private static final long serialVersionUID = -6457126115562721511L;
    private Month month;
    private LocalDateTime week;
    private final IntFunction<Month> from;
    private final IntFunction<Month> to;
    private final int interval;
    private boolean self;
    private boolean forward = true;
    private LocalDateTime previous;

    EveryWeek(Month month, IntFunction<Month> from, IntFunction<Month> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.from = from;
        this.to = to;

        this.week = month.getTime().with(WeekFields.ISO.weekOfMonth(), getFromWeek())
                .with(WeekFields.ISO.dayOfWeek(), 1).withHour(0).withMinute(0).withSecond(0);
        this.interval = interval;
        this.self = true;

    }

    private int getFromWeek() {
        int fromWeek = from.apply(month);
        FieldAssertions.checkWeekOfMonth(month, fromWeek);
        return fromWeek;
    }

    private int getToWeek() {
        int toWeek = to.apply(month);
        FieldAssertions.checkWeekOfMonth(month, toWeek);
        return toWeek;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || shoudNext();
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                week = week.withYear(month.getYear()).withMonth(month.getMonth())
                        .with(WeekFields.ISO.weekOfMonth(), getFromWeek());
                forward = previous != null && previous.compareTo(week) >= 0;
                next = true;
            }
        }
        return next;
    }

    private boolean shoudNext() {
        if (month.getMonth() == week.getMonthValue()) {
            boolean next = (week.getDayOfMonth() + 7 <= month.getLastDay());
            next &= (week.get(WeekFields.ISO.weekOfMonth()) + interval <= getToWeek());
            return next;
        }
        return false;
    }

    @Override
    public Week next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                week = week.plusWeeks(interval);
            } else {
                forward = true;
            }
        }
        previous = LocalDateTime.of(week.toLocalDate(), week.toLocalTime());
        return this;
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
    public LocalDateTime getTime() {
        return week;
    }

    @Override
    public TheDayOfWeek day(int day) {
        final Week copy = (Week) this.copy();
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy), day);
    }

    @Override
    public Day everyDay(IntFunction<Week> from, IntFunction<Week> to, int interval) {
        final Week copy = (Week) this.copy();
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return month;
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
