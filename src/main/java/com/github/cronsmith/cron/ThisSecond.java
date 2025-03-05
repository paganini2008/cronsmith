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
import java.time.temporal.ChronoField;
import java.util.TreeMap;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisSecond
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisSecond implements TheSecond, Serializable {

    private static final long serialVersionUID = 6264419114715870528L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Minute minute;
    private int index;
    private LocalDateTime second;
    private int lastSecond;
    private final StringBuilder cron;

    ThisSecond(Minute minute, int second) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        this.minute = minute;
        DateTimeSupplier supplier = () -> minute.getTime().withSecond(second);
        this.siblings.put(second, supplier);
        this.second = supplier.get();
        this.lastSecond = second;
        this.cron = new StringBuilder().append(second);
    }

    @Override
    public ThisSecond andSecond(int second) {
        return andSecond(second, true);
    }

    private ThisSecond andSecond(int second, boolean writeCron) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        DateTimeSupplier supplier = () -> minute.getTime().withSecond(second);
        this.siblings.put(second, supplier);
        this.lastSecond = second;
        if (writeCron) {
            this.cron.append(",").append(second);
        }
        return this;
    }

    @Override
    public TheSecond toSecond(int second, int interval) {
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        for (int i = lastSecond + interval; i < second; i += interval) {
            andSecond(i, false);
        }
        if (interval > 1) {
            this.cron.append("-").append(second).append("/").append(interval);
        } else {
            this.cron.append("-").append(second);
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return second;
    }

    @Override
    public int getYear() {
        return second.getYear();
    }

    @Override
    public int getMonth() {
        return second.getMonthValue();
    }

    @Override
    public int getDay() {
        return second.getDayOfMonth();
    }

    @Override
    public int getHour() {
        return second.getHour();
    }

    @Override
    public int getMinute() {
        return second.getMinute();
    }

    @Override
    public int getSecond() {
        return second.getSecond();
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (minute.hasNext()) {
                minute = minute.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Second next() {
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        second = supplier.get();
        second = second.withYear(minute.getYear()).withMonth(minute.getMonth())
                .withDayOfMonth(minute.getDay()).withHour(minute.getHour())
                .withMinute(minute.getMinute());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return minute;
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
