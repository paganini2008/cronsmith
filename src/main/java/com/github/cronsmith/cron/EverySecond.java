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

/**
 * 
 * @Description: EverySecond
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class EverySecond implements Second, Serializable {

    private static final long serialVersionUID = -2606684197757806223L;
    private Minute minute;
    private LocalDateTime second;
    private final Function<Minute, Integer> from;
    private final Function<Minute, Integer> to;
    private final int interval;
    private boolean self;
    private boolean forward;

    EverySecond(Minute minute, Function<Minute, Integer> from, Function<Minute, Integer> to,
            int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Invalid interval: " + interval);
        }
        this.minute = minute;
        this.from = from;
        this.to = to;

        this.second = minute.getTime().withSecond(getFromSecond());
        this.interval = interval;
        this.self = true;
        this.forward = true;
    }

    private int getFromSecond() {
        int fromSecond = from.apply(minute);
        FieldAssertions.checkSecond(fromSecond);
        return fromSecond;
    }

    private int getToSecond() {
        int toSecond = to.apply(minute);
        FieldAssertions.checkSecond(toSecond);
        return toSecond;
    }

    @Override
    public boolean hasNext() {
        boolean next = self || second.getSecond() + interval <= getToSecond();
        if (!next) {
            if (minute.hasNext()) {
                minute = minute.next();
                second = second.withYear(minute.getYear()).withMonth(minute.getMonth())
                        .withDayOfMonth(minute.getDay()).withHour(minute.getHour())
                        .withMinute(minute.getMinute()).withSecond(getFromSecond());
                forward = false;
                next = true;
            }
        }
        return next;
    }

    @Override
    public Second next() {
        if (self) {
            self = false;
        } else {
            if (forward) {
                second = second.plusSeconds(interval);
            } else {
                forward = true;
            }
        }
        return this;
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
    public LocalDateTime getTime() {
        return second;
    }

    @Override
    public CronExpression getParent() {
        return minute;
    }

    @Override
    public String toCronString() {
        int fromSecond = getFromSecond();
        int toSecond = getToSecond();
        String str;
        if (fromSecond == 0 && toSecond == 59) {
            str = "*";
        } else {
            str = fromSecond + "-" + toSecond;
        }
        return interval > 1 ? str + "/" + interval : str;
    }

    @Override
    public String toString() {
        return CRON.toCronString(this);
    }

    public static void main(String[] args) {
        // Year everyYear = CronExpressionBuilder.everyYear(2);
        // Month everyMonth = everyYear.everyMonth(5, 10, 2);
        // Day everyDay = everyMonth.everyDay(1, 15, 3);
        // Hour everyHour = everyDay.everyHour(4);
        // Second second = everyHour.at(12, 0);


        // System.out.println(second);
    }

}
