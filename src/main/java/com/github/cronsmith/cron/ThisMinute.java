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
 * @Description: ThisMinute
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisMinute implements TheMinute, Serializable {

    private static final long serialVersionUID = 7090607807516357598L;
    private final TreeMap<Integer, DateTimeSupplier> siblings = new TreeMap<>();
    private Hour hour;
    private int index;
    private LocalDateTime minute;
    private int lastMinute;
    private final StringBuilder cron;

    ThisMinute(Hour hour, int minute) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        this.hour = hour;
        DateTimeSupplier supplier = () -> hour.getTime().withMinute(minute);
        this.siblings.put(minute, supplier);
        this.minute = supplier.get();
        this.lastMinute = minute;
        this.cron = new StringBuilder().append(minute);
    }

    @Override
    public ThisMinute andMinute(int minute) {
        return andMinute(minute, true);
    }

    private ThisMinute andMinute(int minute, boolean writeCron) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        DateTimeSupplier supplier = () -> hour.getTime().withMinute(minute);
        this.siblings.put(minute, supplier);
        this.lastMinute = minute;
        if (writeCron) {
            this.cron.append(",").append(minute);
        }
        return this;
    }

    @Override
    public TheMinute toMinute(int minute, int interval) {
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        for (int i = lastMinute + interval; i < minute; i += interval) {
            andMinute(i, false);
        }
        if (interval > 1) {
            this.cron.append("-").append(minute).append("/").append(interval);
        } else {
            this.cron.append("-").append(minute);
        }
        return this;
    }

    @Override
    public LocalDateTime getTime() {
        return minute;
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
    public TheSecond second(int second) {
        final Minute copy = (Minute) this.copy();
        return new ThisSecond(IteratorUtils.getFirst(copy), second);
    }

    @Override
    public Second everySecond(IntFunction<Minute> from, IntFunction<Minute> to, int interval) {
        final Minute copy = (Minute) this.copy();
        return new EverySecond(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public boolean hasNext() {
        boolean next = index < siblings.size();
        if (!next) {
            if (hour.hasNext()) {
                hour = hour.next();
                index = 0;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Minute next() {
        DateTimeSupplier supplier = IteratorUtils.get(siblings.values().iterator(), index++);
        minute = supplier.get();
        minute = minute.withYear(hour.getYear()).withMonth(hour.getMonth())
                .withDayOfMonth(hour.getDay()).withHour(hour.getHour());
        return this;
    }

    @Override
    public CronExpression getParent() {
        return hour;
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
