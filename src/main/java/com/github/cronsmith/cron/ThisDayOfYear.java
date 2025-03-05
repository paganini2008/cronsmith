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
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.github.cronsmith.CRON;
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
    private final TreeMap<String, DateTimeSupplier> siblings = new TreeMap<>();
    private Year year;
    private int index;
    private LocalDateTime day;
    private int lastDayFlag;

    ThisDayOfYear(Year year, int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        this.year = year;
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(dayOfYear).withHour(0)
                .withMinute(0).withSecond(0);
        this.siblings.put(String.valueOf(dayOfYear), supplier);
        this.day = supplier.get();
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
    public Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval) {
        final Day copy = (Day) this.copy();
        return new EveryHour(IteratorUtils.getFirst(copy), from, to, interval);
    }

    @Override
    public LocalDateTime getTime() {
        return day;
    }

    @Override
    public TheDay andDay(int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(dayOfYear).withHour(0)
                .withMinute(0).withSecond(0);
        this.siblings.put(String.valueOf(dayOfYear), supplier);
        this.lastDayFlag = dayOfYear;
        return this;
    }

    @Override
    public TheDay toDay(int dayOfYear, int interval) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (lastDayFlag != 999) {
            for (int i = lastDayFlag + interval; i < dayOfYear; i += interval) {
                andDay(i);
            }
        }
        return this;
    }

    @Override
    public TheDay andLastDay(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid last day offset: " + n);
        }
        DateTimeSupplier supplier = () -> year.getTime().with(TemporalAdjusters.lastDayOfYear())
                .withHour(0).withMinute(0).withSecond(0).minusDays(n);
        this.siblings.put(n > 0 ? "L-" + n : "L", supplier);
        this.lastDayFlag = 999;
        return this;
    }

    @Override
    public TheDay andLastWeekday() {
        DateTimeSupplier supplier = () -> year.getTime().withDayOfYear(year.getLastWeekdayOfYear())
                .withHour(0).withMinute(0).withSecond(0);
        this.siblings.put("LW", supplier);
        this.lastDayFlag = 999;
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
        Map.Entry<String, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index++);
        day = entry.getValue().get();
        day = day.withYear(year.getYear());
        day = day.withDayOfYear(getDayOfYear(entry.getKey()));
        return this;
    }

    private int getDayOfYear(String repr) {
        try {
            return Integer.parseInt(repr);
        } catch (RuntimeException e) {
            if ("L".equals(repr)) {
                return year.getLastDayOfYear();
            } else if ("LW".equals(repr)) {
                return year.getLastWeekdayOfYear();
            } else {
                Pattern pattern = Pattern.compile("L\\-(\\d+)");
                Matcher matcher = pattern.matcher(repr);
                if (matcher.matches()) {
                    int n = Integer.parseInt(matcher.group(1));
                    return year.getLastDayOfYear() - n;
                }
                throw new IllegalArgumentException(repr);
            }
        }
    }

    @Override
    public CronExpression getParent() {
        return year.month(getMonth());
    }

    @Override
    public String toCronString() {
        Map.Entry<String, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index);
        try {
            Integer.parseInt(entry.getKey());
            return String.valueOf(getDay());
        } catch (RuntimeException e) {
            return entry.getKey();
        }
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
