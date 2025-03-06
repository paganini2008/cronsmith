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
import java.time.temporal.ChronoField;
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
    private final DateTimeSupplier supplier;
    private final IntFunction<Month> from;
    private final IntFunction<Month> to;
    private final int interval;
    private LocalDateTime week;
    private boolean self;
    private boolean forward;

    EveryWeek(Month month, IntFunction<Month> from, IntFunction<Month> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.from = from;
        this.to = to;
        this.supplier = () -> month.getTime()
                .with(WeekFields.ISO.weekOfMonth(), getFromWeekOfMonth())
                .with(WeekFields.ISO.dayOfWeek(), 1).withHour(0).withMinute(0).withSecond(0);
        this.week = supplier.get();
        this.interval = interval;
        this.self = true;
        this.forward = true;

    }

    private int getFromWeekOfMonth() {
        int fromWeekOfMonth = from.apply(month);
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(fromWeekOfMonth);
        return fromWeekOfMonth;
    }

    private int getToWeekOfMonth() {
        int toWeekOfMonth = to.apply(month);
        ChronoField.ALIGNED_WEEK_OF_MONTH.checkValidValue(toWeekOfMonth);
        return toWeekOfMonth;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || shoudNext();
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                week = supplier.get();
                forward = false;
                next = true;
            }
        }
        return next;
    }

    private boolean shoudNext() {
        if (month.getMonth() == week.getMonthValue()) {
            boolean next = (week.getDayOfMonth() + 7 <= month.getLastDay());
            next &= (week.get(WeekFields.ISO.weekOfMonth()) + interval <= getToWeekOfMonth());
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
