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
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.paganini2008.devtools.collection.CollectionUtils;

/**
 * 
 * @Description: EveryDayOfWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryDayOfWeek implements Day, Serializable {

    private static final long serialVersionUID = 7871249122497937952L;
    private Week week;
    private LocalDateTime day;
    private final int fromDay;
    private final int toDay;
    private final int interval;
    private boolean self;
    private boolean forward = true;

    EveryDayOfWeek(Week week, Function<Week, Integer> from, Function<Week, Integer> to,
            int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.week = week;
        this.fromDay = from.apply(week);
        FieldAssertions.checkDayOfWeek(fromDay);
        this.day = week.getTime().with(ChronoField.DAY_OF_WEEK, fromDay).withHour(0).withMinute(0)
                .withSecond(0);
        this.interval = interval;
        this.self = true;
        this.toDay = to.apply(week);
        FieldAssertions.checkDayOfWeek(toDay);
    }

    @Override
    public boolean hasNext() {
        boolean next = self || day.getDayOfWeek().getValue() + interval <= toDay;
        if (!next) {
            if (week.hasNext()) {
                week = week.next();
                day = day.withYear(week.getYear()).withMonth(week.getMonth())
                        .with(ChronoField.ALIGNED_WEEK_OF_MONTH, week.getWeek())
                        .with(ChronoField.DAY_OF_WEEK, fromDay);
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
        return day.getMonth().getValue();
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
        return new ThisHour(CollectionUtils.getFirst(copy), hour);
    }

    @Override
    public Hour everyHour(Function<Day, Integer> from, Function<Day, Integer> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(CollectionUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return week;
    }

    @Override
    public String toCronString() {
        String s = toDay != Calendar.SATURDAY ? fromDay + "-" + toDay : fromDay + "/";
        return interval > 1 ? s + "/" + interval : "?";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
