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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfWeek implements TheDayOfWeek, Serializable {

    private static final long serialVersionUID = -5353496894925284106L;
    private final TreeMap<Integer, LocalDateTime> siblings = new TreeMap<>();
    private Week week;
    private int index;
    private LocalDateTime day;
    private int lastDayOfWeek;
    private final StringBuilder cron;

    ThisDayOfWeek(Week week, int dayOfWeek) {
        FieldAssertions.checkDayOfWeek(dayOfWeek);
        this.week = week;
        LocalDateTime ldt = week.getTime().with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        this.siblings.put(dayOfWeek, ldt);
        this.lastDayOfWeek = dayOfWeek;
        this.cron = new StringBuilder().append(getDayOfWeekName(dayOfWeek));
    }

    @Override
    public TheDayOfWeek andDay(int dayOfWeek) {
        return andDay(dayOfWeek, true);
    }

    private TheDayOfWeek andDay(int dayOfWeek, boolean writeCron) {
        FieldAssertions.checkDayOfWeek(dayOfWeek);
        LocalDateTime ldt = week.getTime().with(WeekFields.ISO.dayOfWeek(), dayOfWeek);
        this.siblings.put(dayOfWeek, ldt);
        this.lastDayOfWeek = dayOfWeek;
        if (writeCron) {
            this.cron.append(",").append(getDayOfWeekName(dayOfWeek));
        }
        return this;
    }

    private String getDayOfWeekName(int dayOfWeek) {
        if (week instanceof LastWeekOfMonth) {
            return dayOfWeek + week.toCronString();
        } else if (week instanceof ThisWeek) {
            return week.toCronString().replaceAll("%s", String.valueOf(dayOfWeek));
        }
        return CalendarUtils.getDayOfWeekName(dayOfWeek);
    }

    @Override
    public TheDayOfWeek toDay(int dayOfWeek, int interval) {
        FieldAssertions.checkDayOfWeek(dayOfWeek);
        List<Integer> days = new ArrayList<Integer>();
        for (int i = lastDayOfWeek + interval; i <= dayOfWeek; i += interval) {
            andDay(i, false);
            days.add(i);
        }
        for (int day : days) {
            this.cron.append(",").append(getDayOfWeekName(day));
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
    public Hour everyHour(Function<Day, Integer> from, Function<Day, Integer> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (week.hasNext()) {
                week = week.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        day = new ArrayList<LocalDateTime>(siblings.values()).get(index++);
        day.withYear(week.getYear()).withMonth(week.getMonth()).with(WeekFields.ISO.weekOfMonth(),
                week.getWeek());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return week;
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
