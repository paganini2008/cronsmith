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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.github.cronsmith.CRON;
import com.github.cronsmith.IteratorUtils;

/**
 * 
 * @Description: ThisDay
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class ThisDay implements TheDay, Serializable {

    private static final long serialVersionUID = -6007054113405112202L;
    private final TreeMap<String, DateTimeSupplier> siblings = new TreeMap<>();
    private final List<Range> ranges = new ArrayList<>();
    private Month month;
    private int index;
    private LocalDateTime day;
    private int lastDayFlag;


    ThisDay(Month month, int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        this.month = month;
        DateTimeSupplier supplier = () -> month.getTime().withDayOfMonth(dayOfMonth);
        this.siblings.put(String.valueOf(dayOfMonth), supplier);
        this.day = supplier.get();
        this.lastDayFlag = dayOfMonth;
        this.ranges.add(new Range(dayOfMonth));
    }

    @Override
    public TheDay andDay(int dayOfMonth) {
        this.ranges.add(new Range(dayOfMonth));
        return doAndDay(dayOfMonth);
    }

    private TheDay doAndDay(int dayOfMonth) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        DateTimeSupplier supplier = () -> month.getTime().withDayOfMonth(dayOfMonth);
        this.siblings.put(String.valueOf(dayOfMonth), supplier);
        this.lastDayFlag = dayOfMonth;
        return this;
    }

    @Override
    public TheDay andLastDay(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Invalid last day offset: " + n);
        }
        this.ranges.add(new Range(n > 0 ? "L-" + n : "L"));
        return doAndLastDay(n);
    }

    private TheDay doAndLastDay(int n) {
        DateTimeSupplier supplier = () -> month.getTime().withDayOfMonth(month.getLastDay() - n);
        this.siblings.put(n > 0 ? "L-" + n : "L", supplier);
        this.lastDayFlag = 99;
        return this;
    }

    @Override
    public TheDay andLastWeekday() {
        this.ranges.add(new Range("LW"));
        return doAndLastWeekday();
    }

    private TheDay doAndLastWeekday() {
        DateTimeSupplier supplier = () -> month.getTime().withDayOfMonth(month.getLastWeekday());
        this.siblings.put("LW", supplier);
        this.lastDayFlag = 99;
        return this;
    }

    @Override
    public TheDay toDay(int dayOfMonth, int interval) {
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth);
        if (interval < 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        if (lastDayFlag != 99) {
            if (lastDayFlag >= dayOfMonth) {
                throw new IllegalArgumentException(lastDayFlag + ">=" + dayOfMonth);
            }
            for (int i = lastDayFlag + interval; i <= dayOfMonth; i += interval) {
                doAndDay(i);
            }
            this.ranges.get(ranges.size() - 1).setTo(dayOfMonth).setInterval(interval);
        }
        return this;
    }

    @Override
    public TheDay toLastDay(int interval) {
        return toDay(month.getLastDay(), interval);
    }

    @Override
    public LocalDateTime getTime() {
        return day;
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
    public Day next() {
        Map.Entry<String, DateTimeSupplier> entry =
                IteratorUtils.get(siblings.entrySet().iterator(), index++);
        day = entry.getValue().get();
        day = day.withYear(month.getYear()).withMonth(month.getMonth())
                .withDayOfMonth(getDayOfMonth(entry.getKey()));
        return this;
    }

    private int getDayOfMonth(String repr) {
        try {
            return Integer.parseInt(repr);
        } catch (RuntimeException e) {
            if ("L".equals(repr)) {
                return month.getLastDay();
            } else if ("LW".equals(repr)) {
                return month.getLastWeekday();
            } else {
                Pattern pattern = Pattern.compile("L\\-(\\d+)");
                Matcher matcher = pattern.matcher(repr);
                if (matcher.matches()) {
                    int n = Integer.parseInt(matcher.group(1));
                    return month.getLastDay() - n;
                }
                throw new IllegalArgumentException(repr);
            }
        }
    }

    @Override
    public CronExpression getParent() {
        return month;
    }

    @Override
    public String toCronString() {
        return ranges.stream().map(Range::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

}
