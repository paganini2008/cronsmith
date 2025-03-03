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
import java.time.temporal.WeekFields;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;
import com.github.cronsmith.parser.AbbreviationUtils;

/**
 * 
 * @Description: EveryDayOfWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryDayOfWeek implements DayOfWeek, Serializable {

    private static final long serialVersionUID = 7871249122497937952L;
    private Week week;
    private LocalDateTime day;
    private final IntFunction<Week> from;
    private final IntFunction<Week> to;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryDayOfWeek(Week week, IntFunction<Week> from, IntFunction<Week> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.week = week;
        this.from = from;
        this.to = to;

        this.day = week.getTime().with(WeekFields.ISO.dayOfWeek(), getFromDayOfWeek()).withHour(0)
                .withMinute(0).withSecond(0);
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private int getFromDayOfWeek() {
        int fromDayOfWeek = from.apply(week);
        FieldAssertions.checkDayOfWeek(fromDayOfWeek);
        return fromDayOfWeek;
    }

    private int getToDayOfWeek() {
        int toDayOfWeek = to.apply(week);
        FieldAssertions.checkDayOfWeek(toDayOfWeek);
        return toDayOfWeek;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || day.getDayOfWeek().getValue() + interval <= getToDayOfWeek();
        if (!next) {
            if (week.hasNext()) {
                week = week.next();
                day = day.withYear(week.getYear()).withMonth(week.getMonth())
                        .with(WeekFields.ISO.weekOfMonth(), week.getWeek())
                        .with(WeekFields.ISO.dayOfWeek(), getFromDayOfWeek());
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
    public Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return week;
    }

    @Override
    public String toCronString() {
        int fromDayOfWeek = getFromDayOfWeek();
        int toDayOfWeek = getToDayOfWeek();
        if (interval > 1) {
            return fromDayOfWeek + "-" + toDayOfWeek + "/" + interval;
        } else {
            return String.format("%s-%s", AbbreviationUtils.getDayOfWeekName(fromDayOfWeek),
                    AbbreviationUtils.getDayOfWeekName(toDayOfWeek));
        }
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
