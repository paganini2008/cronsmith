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
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisWeekOfYear
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisWeekOfYear implements TheWeek, Serializable {

    private static final long serialVersionUID = -3294283555586718358L;
    private final TreeMap<Integer, LocalDateTime> siblings = new TreeMap<>();
    private Year year;
    private int index;
    private LocalDateTime week;
    private int lastWeek;

    ThisWeekOfYear(Year year, int weekOfYear) {
        FieldAssertions.checkWeekOfYear(year, weekOfYear);
        this.year = year;
        LocalDateTime ldt = year.getTime().with(WeekFields.ISO.weekOfYear(), weekOfYear);
        this.siblings.put(weekOfYear, ldt);
        this.week = ldt;
        this.lastWeek = weekOfYear;
    }

    @Override
    public TheWeek andWeek(int weekOfYear) {
        FieldAssertions.checkWeekOfYear(year, weekOfYear);
        LocalDateTime ldt = year.getTime().with(WeekFields.ISO.weekOfYear(), weekOfYear);
        this.siblings.put(weekOfYear, ldt);
        this.lastWeek = weekOfYear;
        return this;
    }

    @Override
    public TheWeek toWeek(int weekOfYear, int interval) {
        FieldAssertions.checkWeekOfYear(year, weekOfYear);
        for (int i = lastWeek + interval; i < weekOfYear; i += interval) {
            andWeek(i);
        }
        return this;
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
    public TheDayOfWeek day(int dayOfWeek) {
        final Week copy = (Week) this;
        return new ThisDayOfWeek(IteratorUtils.getFirst(copy), dayOfWeek);
    }

    @Override
    public Day everyDay(Function<Week, Integer> from, Function<Week, Integer> to, int interval) {
        final Week copy = (Week) this;
        return new EveryDayOfWeek(IteratorUtils.getFirst(copy), from, to, interval);
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
    public Week next() {
        Map.Entry<Integer, LocalDateTime> entry = new ArrayList<>(siblings.entrySet()).get(index++);
        week = entry.getValue();
        week.withYear(year.getYear()).with(WeekFields.ISO.weekOfYear(),
                Math.min(entry.getKey(), year.getWeekCountOfYear()));
        return this;
    }

    @Override
    public CronExpression getParent() {
        return year;
    }
}
