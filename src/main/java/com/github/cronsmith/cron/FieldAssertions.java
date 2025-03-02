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

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * 
 * @Description: FieldAssertions
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public abstract class FieldAssertions {

    public static void checkYear(int year) {
        int thisYear = LocalDateTime.now().getYear();
        if (year < thisYear) {
            throw new IllegalArgumentException("Year '" + year + "' is past.");
        }
        if (year > Year.MAX_YEAR) {
            throw new IllegalArgumentException(
                    "Year '" + year + "' is greater than the Max Year " + Year.MAX_YEAR);
        }
    }

    public static void checkMonth(int month) {
        int start = java.time.Month.JANUARY.getValue();
        int end = java.time.Month.DECEMBER.getValue();
        if (month < start || month > end) {
            throw new IllegalArgumentException("Month's range is " + start + " to " + end);
        }
    }

    public static void checkDayOfYear(Year year, int day) {
        if (day < 1 || day > year.getLastDayOfYear()) {
            throw new IllegalArgumentException("Day's range is 1 to " + year.getLastDayOfYear());
        }
    }

    public static void checkWeekOfYear(Year year, int week) {
        if (week < 1 || week > year.getWeekCountOfYear()) {
            throw new IllegalArgumentException("Week's range is 1 to " + year.getWeekCountOfYear());
        }
    }

    public static void checkWeekOfMonth(Month month, int week) {
        if (week < 1 || week > month.getWeekCountOfMonth()) {
            throw new IllegalArgumentException(
                    "Week's range is 1 to " + month.getWeekCountOfMonth());
        }
    }

    public static void checkDayOfMonth(Month month, int day) {
        if (day < 1 || day > month.getLastDay()) {
            throw new IllegalArgumentException(
                    "Day's range of this month is 1 to " + month.getLastDay());
        }
    }

    public static void checkDayOfWeek(int dayOfWeek) {
        if (dayOfWeek < DayOfWeek.MONDAY.getValue() || dayOfWeek > DayOfWeek.SUNDAY.getValue()) {
            throw new IllegalArgumentException("WeekDay's range is " + DayOfWeek.MONDAY.getValue()
                    + " to " + DayOfWeek.SUNDAY.getValue());
        }
    }

    public static void checkHourOfDay(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour's range is 0 to 23.");
        }
    }

    public static void checkMinute(int minute) {
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Minute's range is 0 to 59.");
        }
    }

    public static void checkSecond(int second) {
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException("Second's range is 0 to 59.");
        }
    }

}
