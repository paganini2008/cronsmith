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
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.CollectionUtils;

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
    private final int fromHour;
    private final int toHour;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryHour(Day day, Function<Day, Integer> from, Function<Day, Integer> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.day = day;
        this.fromHour = from.apply(day);
        FieldAssertions.checkHourOfDay(fromHour);
        this.hour = day.getTime().withHour(fromHour).withMinute(0).withSecond(0);
        this.interval = interval;
        this.self = true;
        this.forward = true;
        this.toHour = to.apply(day);
        FieldAssertions.checkHourOfDay(toHour);
    }

    @Override
    public boolean hasNext() {
        boolean next = self || hour.getHour() + interval <= toHour;
        if (!next) {
            if (day.hasNext()) {
                day = day.next();
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
        return new ThisMinute(CollectionUtils.getFirst(copy), minute);
    }

    @Override
    public Minute everyMinute(Function<Hour, Integer> from, Function<Hour, Integer> to,
            int interval) {
        final Hour copy = (Hour) this.copy();
        return new EveryMinute(CollectionUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return day;
    }

    @Override
    public String toCronString() {
        String s = toHour != 23 ? fromHour + "-" + toHour : fromHour + "";
        return interval > 1 ? s + "/" + interval : "*";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
