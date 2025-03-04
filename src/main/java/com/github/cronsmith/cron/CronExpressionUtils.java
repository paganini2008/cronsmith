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

import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

/**
 * 
 * @Description: CronExpressionUtils
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public abstract class CronExpressionUtils {

    private static final Era epoch = Era.getInstance();

    public static TheYear year() {
        return epoch.year();
    }

    public static TheYear year(int year) {
        return epoch.year(year);
    }

    public static TheMonth month() {
        return year().month(epoch.getTime().getMonthValue());
    }

    public static TheMonth month(int year, int month) {
        return year(year).month(month);
    }

    public static TheWeek week() {
        return month().week(epoch.getTime().get(WeekFields.ISO.weekOfMonth()));
    }

    public static TheWeek week(int year, int month, int weekOfMonth) {
        return month(year, month).week(weekOfMonth);
    }

    public static TheDay day() {
        return month().day(epoch.getTime().getDayOfMonth());
    }

    public static TheDay day(int year, int month, int dayOfMonth) {
        return month(year, month).day(dayOfMonth);
    }

    public static TheHour hour() {
        return day().hour(epoch.getTime().getHour());
    }

    public static TheHour hour(int hourOfDay) {
        return day().hour(hourOfDay);
    }

    public static TheMinute minute() {
        return hour().minute(epoch.getTime().getMinute());
    }

    public static TheMinute minute(int minute) {
        return hour().minute(minute);
    }

    public static TheMinute at(int hourOfDay, int minute) {
        return day().hour(hourOfDay).minute(minute);
    }

    public static TheSecond at(int hourOfDay, int minute, int second) {
        return day().hour(hourOfDay).minute(minute).second(second);
    }

    public static Year everyYear() {
        return epoch.everyYear();
    }

    public static Year everyYear(int interval) {
        return epoch.everyYear(interval);
    }

    public static Year everyYear(int fromYear, int toYear, int interval) {
        return epoch.everyYear(fromYear, toYear, interval);
    }

    public static Month everyMonth() {
        return everyMonth(1);
    }

    public static Month everyMonth(int interval) {
        return everyMonth(1, 12, interval);
    }

    public static Month everyMonth(int fromMonth, int toMonth, int interval) {
        return everyYear().everyMonth(fromMonth, toMonth, interval);
    }

    public static Week everyWeek() {
        return everyWeek(1);
    }

    public static Week everyWeek(int interval) {
        return everyMonth().everyWeek(m -> 1, m -> {
            return m.getWeekCountOfMonth();
        }, interval);
    }

    public static Week everyWeek(int fromWeek, int toWeek, int interval) {
        return everyMonth().everyWeek(fromWeek, toWeek, interval);
    }

    public static Day everyDay() {
        return everyDay(1);
    }

    public static Day everyDay(int interval) {
        return everyMonth().everyDay(m -> 1, m -> {
            return m.getLastDay();
        }, interval);
    }

    public static Day everyDay(int fromDay, int toDay, int interval) {
        return everyMonth().everyDay(fromDay, toDay, interval);
    }

    public static Hour everyHour() {
        return everyHour(1);
    }

    public static Hour everyHour(int interval) {
        return everyHour(0, 23, interval);
    }

    public static Hour everyHour(int fromHour, int toHour, int interval) {
        return everyDay().everyHour(fromHour, toHour, interval);
    }

    public static Minute everyMinute() {
        return everyMinute(1);
    }

    public static Minute everyMinute(int interval) {
        return everyMinute(0, 59, interval);
    }

    public static Minute everyMinute(int fromMinute, int toMinute, int interval) {
        return everyHour().everyMinute(fromMinute, toMinute, interval);
    }

    public static Second everySecond() {
        return everySecond(1);
    }

    public static Second everySecond(int interval) {
        return everySecond(0, 59, interval);
    }

    public static Second everySecond(int fromSecond, int toSecond, int interval) {
        return everyMinute().everySecond(fromSecond, toSecond, interval);
    }

    public static void main(String[] args) {
        everySecond(5, 30, 7).forEach(ldt -> {
            System.out.println(ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }, 100);
        System.out.println(everySecond(5, 30, 7));
    }

}
