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
package com.github.cronsmith;

import java.time.DayOfWeek;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Epoch;
import com.github.cronsmith.cron.EveryYear;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDayOfWeek;
import com.github.cronsmith.cron.TheDayOfWeekInMonth;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;
import com.github.cronsmith.parser.CronOption;
import com.github.cronsmith.parser.DayOfWeekOption;
import com.github.cronsmith.parser.DayOption;
import com.github.cronsmith.parser.HourOption;
import com.github.cronsmith.parser.MalformedCronException;
import com.github.cronsmith.parser.MinuteOption;
import com.github.cronsmith.parser.MonthOption;
import com.github.cronsmith.parser.SecondOption;
import com.github.cronsmith.parser.YearOption;

/**
 * 
 * @Description: CRON
 * @Author: Fred Feng
 * @Date: 02/03/2025
 * @Version 1.0.0
 */
public abstract class CRON {

    public static TemporalField NON_ISO_WEEKS_FIELD =
            WeekFields.of(DayOfWeek.MONDAY, 7).dayOfWeek();

    public static String toCronString(CronExpression cronExpression) {
        CronExpression copy = cronExpression.copy();
        if (copy instanceof Year) {
            copy = ((Year) copy).Jan().day(1).at(0, 0, 0);
        } else if (copy instanceof Month) {
            copy = ((Month) copy).day(1).at(0, 0, 0);
        } else if (copy instanceof Week) {
            copy = ((Week) copy).Mon().at(0, 0, 0);
        } else if (copy instanceof Day) {
            copy = ((Day) copy).at(0, 0, 0);
        } else if (copy instanceof Hour) {
            copy = ((Hour) copy).at(0, 0);
        } else if (copy instanceof Minute) {
            copy = ((Minute) copy).second(0);
        }

        final StringBuilder cron = new StringBuilder();
        CronExpression second = copy;
        CronExpression minute = second.getParent();
        CronExpression hour = minute.getParent();
        cron.append(second.toCronString()).append(" ").append(minute.toCronString()).append(" ")
                .append(hour.toCronString()).append(" ");

        CronExpression day = hour.getParent();
        boolean hasDayOfWeek = false;
        if (day instanceof TheDayOfWeek || day instanceof TheDayOfWeekInMonth) {
            hasDayOfWeek = true;
        }
        if (hasDayOfWeek) {
            cron.append("?").append(" ");
        } else {
            cron.append(day.toCronString()).append(" ");
        }

        CronExpression month;
        if (hasDayOfWeek && day.getParent() instanceof Week) {
            month = day.getParent().getParent();
        } else {
            month = day.getParent();
        }

        cron.append(month.toCronString()).append(" ");

        if (hasDayOfWeek) {
            cron.append(day.toCronString());
        } else {
            cron.append("?");
        }

        CronExpression year = month.getParent();
        if (!(year instanceof EveryYear)) {
            cron.append(" ").append(year.toCronString());
        }
        return cron.toString();
    }

    public static CronExpression parse(String cronString) {
        List<String> clauses = new ArrayList<String>();
        if (clauses.size() == 6) {
            clauses.add("*");
        }
        if (clauses.size() != 7) {
            throw new MalformedCronException(cronString);
        }
        Collections.reverse(clauses);
        Collections.swap(clauses, 1, 2);
        List<CronOption> parsers = new ArrayList<CronOption>(7);
        String value = clauses.get(0);
        parsers.add(new YearOption(value));

        value = clauses.get(1);
        parsers.add(new MonthOption(value));

        boolean hasDay = false;
        value = clauses.get(2);
        if (!value.equals("?")) {
            parsers.add(new DayOfWeekOption(value));
            hasDay = true;
        }

        value = clauses.get(3);
        if (!value.equals("?")) {
            if (hasDay) {
                throw new MalformedCronException(cronString);
            }
            parsers.add(new DayOption(value));
        }

        value = clauses.get(4);
        parsers.add(new HourOption(value));

        value = clauses.get(5);
        parsers.add(new MinuteOption(value));

        value = clauses.get(6);
        parsers.add(new SecondOption(value));

        CronExpression cronExpression = Epoch.getInstance();
        try {
            for (CronOption clause : parsers) {
                cronExpression = clause.join(cronExpression);
            }
            return cronExpression;
        } catch (RuntimeException e) {
            if (e instanceof MalformedCronException) {
                throw e;
            }
            throw new MalformedCronException(cronString, e);
        }
    }

}
