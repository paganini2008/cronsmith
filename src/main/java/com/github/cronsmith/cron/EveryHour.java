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
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryHour
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryHour implements Hour, Serializable {

    private static final long serialVersionUID = -5459905273757712271L;
    private Day day;
    private LocalDateTime hour;
    private final IntFunction<Day> from;
    private final IntFunction<Day> to;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryHour(Day day, IntFunction<Day> from, IntFunction<Day> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.day = day;
        this.from = from;
        this.to = to;

        int fromHour = getFromHour();
        this.hour = day.getTime().withHour(fromHour).withMinute(0).withSecond(0);
        this.interval = interval;
        this.self = true;
        this.forward = true;

    }

    private int getFromHour() {
        int fromHour = from.apply(day);
        FieldAssertions.checkHourOfDay(fromHour);
        return fromHour;
    }

    public int getToHour() {
        int toHour = to.apply(day);
        FieldAssertions.checkHourOfDay(toHour);
        return toHour;
    }

    @Override
    public boolean hasNext() {
        int toHour = getToHour();
        boolean next = self || hour.getHour() + interval <= toHour;
        if (!next) {
            if (day.hasNext()) {
                day = day.next();
                int fromHour = getFromHour();
                hour = hour.withYear(day.getYear()).withMonth(day.getMonth())
                        .withDayOfMonth(day.getDay()).withHour(fromHour);
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Hour next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                hour = hour.plusHours(interval);
            } else {
                forward = true;
            }
        }
        return this;
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
    public LocalDateTime getTime() {
        return hour;
    }

    @Override
    public TheMinute minute(int minute) {
        final Hour copy = (Hour) this.copy();
        return new ThisMinute(IteratorUtils.getFirst(copy), minute);
    }

    @Override
    public Minute everyMinute(IntFunction<Hour> from, IntFunction<Hour> to, int interval) {
        final Hour copy = (Hour) this.copy();
        return new EveryMinute(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return day;
    }

    @Override
    public String toCronString() {
        int fromHour = getFromHour();
        int toHour = getToHour();
        String str;
        if (fromHour == 0 && toHour == 23) {
            str = "*";
        } else {
            str = fromHour + "-" + toHour;
        }
        return interval > 1 ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
