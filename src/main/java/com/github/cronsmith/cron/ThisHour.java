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
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.CollectionUtils;

/**
 * 
 * @Description: ThisHour
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisHour implements TheHour, Serializable {

    private static final long serialVersionUID = 8124589572544886753L;
    private final TreeMap<Integer, LocalDateTime> siblings = new TreeMap<>();
    private Day day;
    private int index;
    private LocalDateTime hour;
    private int lastHour;
    private final StringBuilder cron;

    ThisHour(Day day, int hour) {
        FieldAssertions.checkHourOfDay(hour);
        this.day = day;
        LocalDateTime ldt = day.getTime().withHour(hour);
        this.siblings.put(hour, ldt);
        this.hour = ldt;
        this.lastHour = hour;
        this.cron = new StringBuilder().append(hour);
    }

    @Override
    public ThisHour andHour(int hour) {
        return andHour(hour, true);
    }

    private ThisHour andHour(int hour, boolean writeCron) {
        FieldAssertions.checkHourOfDay(hour);
        LocalDateTime ldt = day.getTime().withHour(hour);
        siblings.put(hour, ldt);
        this.lastHour = hour;
        if (writeCron) {
            cron.append(",").append(hour);
        }
        return this;
    }

    @Override
    public TheHour toHour(int hour, int interval) {
        FieldAssertions.checkHourOfDay(hour);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        for (int i = lastHour + interval; i < hour; i += interval) {
            andHour(i, false);
        }
        if (interval > 1) {
            cron.append("-").append(hour).append("/").append(interval);
        } else {
            cron.append("-").append(hour);
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return hour;
    }

    @Override
    public int getYear() {
        return hour.getYear();
    }

    @Override
    public int getMonth() {
        return hour.getMonthValue();
    }

    @Override
    public int getDay() {
        return hour.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return hour.getHour();
    }

    @Override
    public TheMinute minute(int minute) {
        final Hour copy = (Hour) this.copy();
        return new ThisMinute(CollectionUtils.getFirst(copy), minute);
    }

    @Override
    public Minute everyMinute(Function<Hour, Integer> from, Function<Hour, Integer> to,
            int interval) {
        final Hour copy = (Hour) this.copy();
        return new EveryMinute(CollectionUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (day.hasNext()) {
                day = day.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Hour next() {
        hour = List.copyOf(siblings.values()).get(index++);
        hour = hour.withYear(day.getYear()).withMonth(day.getMonth()).withDayOfMonth(day.getDay());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return day;
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
