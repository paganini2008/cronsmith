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
import java.util.Locale;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.paganini2008.devtools.collection.CollectionUtils;

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
    private final int fromWeek;
    private final int toWeek;
    private final int interval;
    private boolean self;
    private boolean forward = true;
    private LocalDateTime previous;

    EveryWeek(Month month, Function<Month, Integer> from, Function<Month, Integer> to,
            int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.month = month;
        this.fromWeek = from.apply(month);
        FieldAssertions.checkWeekOfMonth(month, fromWeek);
        this.week = month.getTime().with(WeekFields.of(Locale.getDefault()).weekOfMonth(), fromWeek)
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).withHour(0).withMinute(0)
                .withSecond(0);
        this.interval = interval;
        this.self = true;
        this.toWeek = to.apply(month);
        FieldAssertions.checkWeekOfMonth(month, toWeek);
    }

    @Override
    public boolean hasNext() {
        boolean next = self || shoudNext();
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
                week = week.withYear(month.getYear()).withMonth(month.getMonth())
                        .with(WeekFields.of(Locale.getDefault()).weekOfMonth(), fromWeek);
                forward = previous != null && previous.compareTo(week) >= 0;
                next = true;
            }
        }
        return next;
    }

    private boolean shoudNext() {
        if (month.getMonth() == week.getMonthValue()) {
            boolean next = (week.getDayOfMonth() + 7 <= month.getLastDay());
            next &= (week.get(WeekFields.of(Locale.getDefault()).weekOfMonth())
                    + interval <= toWeek);
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
        return week.get(WeekFields.of(Locale.getDefault()).weekOfMonth());
    }

    @Override
    public int getWeekOfYear() {
        return week.get(WeekFields.of(Locale.getDefault()).weekOfYear());
    }

    @Override
    public LocalDateTime getTime() {
        return week;
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
