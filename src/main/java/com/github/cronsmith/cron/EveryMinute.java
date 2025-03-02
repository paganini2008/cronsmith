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
import java.util.function.Function;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: EveryMinute
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EveryMinute implements Minute, Serializable {

    private static final long serialVersionUID = -7939881133025374416L;
    private Hour hour;
    private LocalDateTime minute;
    private final int fromMinute;
    private final int toMinute;
    private final int interval;
    private boolean self;
    private boolean forward;

    EveryMinute(Hour hour, Function<Hour, Integer> from, Function<Hour, Integer> to, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.hour = hour;
        this.fromMinute = from.apply(hour);
        this.minute = hour.getTime().withMinute(fromMinute).withSecond(0);
        FieldAssertions.checkMinute(fromMinute);
        this.interval = interval;
        this.toMinute = to.apply(hour);
        FieldAssertions.checkMinute(toMinute);
        this.self = true;
        this.forward = true;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || minute.getMinute() + interval <= toMinute;
        if (!next) {
            if (hour.hasNext()) {
                hour = hour.next();
                minute = minute.withYear(hour.getYear()).withMonth(hour.getMonth())
                        .withDayOfMonth(hour.getDay()).withHour(hour.getHour())
                        .withMinute(fromMinute);
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Minute next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                minute = minute.plusMinutes(interval);
            } else {
                forward = true;
            }
        }
        return this;
    }

    @Override
    public int getYear() {
        return minute.getYear();
    }

    @Override
    public int getMonth() {
        return minute.getMonthValue();
    }

    @Override
    public int getDay() {
        return minute.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return minute.getHour();
    }

    @Override
    public int getMinute() {
        return minute.getMinute();
    }

    @Override
    public LocalDateTime getTime() {
        return minute;
    }

    @Override
    public TheSecond second(int second) {
        final Minute copy = (Minute) this.copy();
        return new ThisSecond(IteratorUtils.getFirst(copy), second);
    }

    @Override
    public Second everySecond(Function<Minute, Integer> from, Function<Minute, Integer> to,
            int interval) {
        final Minute copy = (Minute) this.copy();
        return new EverySecond(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public CronExpression getParent() {
        return hour;
    }

    @Override
    public String toCronString() {
        String s = toMinute != 59 ? fromMinute + "-" + toMinute : fromMinute + "";
        return interval > 1 ? s + "/" + interval : "*";
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
