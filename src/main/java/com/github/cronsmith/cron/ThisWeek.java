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
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.CollectionUtils;

/**
 * 
 * @Description: ThisWeek
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisWeek implements TheWeek, Serializable {

    private static final long serialVersionUID = -4563991137870265612L;
    private final TreeMap<Integer, LocalDateTime> siblings = new TreeMap<>();
    private Month month;
    private int index;
    private LocalDateTime week;
    private int lastWeek;
    private final StringBuilder cron;

    ThisWeek(Month month, int week) {
        FieldAssertions.checkWeekOfMonth(month, week);
        this.month = month;
        LocalDateTime ldt = month.getTime().with(WeekFields.ISO.weekOfMonth(), week);
        this.siblings.put(week, ldt);
        this.week = ldt;
        this.lastWeek = week;
        this.cron = new StringBuilder().append("%s#").append(week);
    }

    @Override
    public ThisWeek andWeek(int week) {
        return andWeek(week, true);
    }

    private ThisWeek andWeek(int week, boolean writeCron) {
        FieldAssertions.checkWeekOfMonth(month, week);
        LocalDateTime ldt = month.getTime().with(WeekFields.ISO.weekOfMonth(), week);
        this.siblings.put(week, ldt);
        this.lastWeek = week;
        if (writeCron) {
            this.cron.append(",%s#").append(week);
        }
        return this;
    }

    @Override
    public ThisWeek toWeek(int week, int interval) {
        FieldAssertions.checkWeekOfMonth(month, week);
        List<Integer> weeks = new ArrayList<Integer>();
        for (int i = lastWeek + interval; i < week; i += interval) {
            andWeek(i, false);
            weeks.add(i);
        }
        for (int w : weeks) {
            this.cron.append(",%s#").append(w);
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
        boolean next = index < siblings.size();
        if (!next) {
            if (month.hasNext()) {
                month = month.next();
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
        week = week.withYear(month.getYear()).withMonth(month.getMonth()).with(
                WeekFields.ISO.weekOfMonth(),
                Math.min(entry.getKey(), month.getWeekCountOfMonth()));
        return this;
    }

    @Override
    public CronExpression getParent() {
        return month;
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
