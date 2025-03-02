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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDayOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDayOfYear implements TheDay, Serializable {

    private static final long serialVersionUID = -8235489088108418524L;
    private final TreeMap<Integer, LocalDateTime> siblings = new TreeMap<>();
    private Year year;
    private int index;
    private LocalDateTime day;
    private int lastDay;

    ThisDayOfYear(Year year, int day) {
        FieldAssertions.checkDayOfYear(year, day);
        this.year = year;
        LocalDateTime ldt =
                year.getTime().withDayOfYear(day).withHour(0).withMinute(0).withSecond(0);
        this.siblings.put(day, ldt);
        this.lastDay = day;
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
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public TheDay andDay(int dayOfYear) {
        FieldAssertions.checkDayOfYear(year, dayOfYear);
        LocalDateTime ldt =
                year.getTime().withDayOfYear(dayOfYear).withHour(0).withMinute(0).withSecond(0);
        this.siblings.put(dayOfYear, ldt);
        this.lastDay = dayOfYear;
        return this;
    }

    @Override
    public TheDay toDay(int day, int interval) {
        FieldAssertions.checkDayOfYear(year, day);
        for (int dayOfYear = lastDay + interval; dayOfYear < day; dayOfYear += interval) {
            andDay(dayOfYear);
        }
        return this;
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (year.hasNext()) {
                year = year.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Day next() {
        Map.Entry<Integer, LocalDateTime> entry = new ArrayList<>(siblings.entrySet()).get(index++);
        day = entry.getValue();
        day = day.withYear(year.getYear());
        day = day.withDayOfYear(Math.min(entry.getKey(), year.getLastDayOfYear()));
        return this;
    }

    @Override
    public CronExpression getParent() {
        return null;
    }

}
