
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;

/**
 * 
 * Entry class to create a CronExpression object. Start your work from here.
 * 
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public final class CronBuilder implements CronExpression, Serializable {

    private static final long serialVersionUID = -400537561864611508L;

    public CronBuilder() {
        this.zoneId = ZoneId.systemDefault();
        this.startTime = LocalDateTime.now(zoneId).withNano(0);
        this.useMonthAsNumber = false;
        this.useDayOfWeekAsNumber = false;
    }

    private ZoneId zoneId;
    private LocalDateTime startTime;
    private boolean useMonthAsNumber;
    private boolean useDayOfWeekAsNumber;

    public CronBuilder setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public CronBuilder setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public boolean isUseMonthAsNumber() {
        return useMonthAsNumber;
    }

    public CronBuilder setUseMonthAsNumber(boolean useMonthAsNumber) {
        this.useMonthAsNumber = useMonthAsNumber;
        return this;
    }

    public boolean isUseDayOfWeekAsNumber() {
        return useDayOfWeekAsNumber;
    }

    public CronBuilder setUseDayOfWeekAsNumber(boolean useDayOfWeekAsNumber) {
        this.useDayOfWeekAsNumber = useDayOfWeekAsNumber;
        return this;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    public LocalDateTime getTime() {
        return startTime;
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public CronExpression getParent() {
        return null;
    }

    public Year everyYear() {
        return everyYear(1);
    }

    public Year everyYear(int interval) {
        return everyYear(getTime().getYear(), interval);
    }

    public Year everyYear(int fromYear, int interval) {
        return everyYear(fromYear, Year.MAX_YEAR, interval);
    }

    public Year everyYear(int fromYear, int toYear, int interval) {
        return everyYear(e -> fromYear, e -> toYear, interval);
    }

    public Year everyYear(IntFunction<CronBuilder> from, IntFunction<CronBuilder> to,
            int interval) {
        return new EveryYear(this, from, to, interval);
    }

    public Month everyMonth() {
        return everyMonth(1);
    }

    public Month everyMonth(int interval) {
        return everyMonth(1, interval);
    }

    public Month everyMonth(int fromMonth, int interval) {
        return everyMonth(fromMonth, 12, interval);
    }

    public Month everyMonth(int fromMonth, int toMonth, int interval) {
        return everyMonth(y -> fromMonth, y -> toMonth, interval);
    }

    public Month everyMonth(IntFunction<Year> from, IntFunction<Year> to, int interval) {
        return everyYear().everyMonth(from, to, interval);
    }

    public Week everyWeek() {
        return everyWeek(1);
    }

    public Week everyWeek(int interval) {
        return everyWeek(1, interval);
    }

    public Week everyWeek(int fromWeekOfMonth, int interval) {
        return everyMonth().everyWeek(m -> fromWeekOfMonth, m -> {
            return m.getWeekCountOfMonth();
        }, interval);
    }

    public Week everyWeek(int fromWeekOfMonth, int toWeekOfMonth, int interval) {
        return everyWeek(m -> fromWeekOfMonth, m -> toWeekOfMonth, interval);
    }

    public Week everyWeek(IntFunction<Month> from, IntFunction<Month> to, int interval) {
        return everyMonth().everyWeek(from, to, interval);
    }

    public Day everyDay() {
        return everyDay(1);
    }

    public Day everyDay(int interval) {
        return everyDay(1, interval);
    }

    public Day everyDay(int fromDayOfMonth, int interval) {
        return everyDay(m -> fromDayOfMonth, m -> {
            return m.getLastDay();
        }, interval);
    }

    public Day everyDay(int fromDayOfMonth, int toDayOfMonth, int interval) {
        return everyDay(m -> fromDayOfMonth, m -> toDayOfMonth, interval);
    }

    public Day everyDay(IntFunction<Month> from, IntFunction<Month> to, int interval) {
        return everyMonth().everyDay(from, to, interval);
    }

    public Hour everyHour() {
        return everyHour(1);
    }

    public Hour everyHour(int interval) {
        return everyHour(0, 23, interval);
    }

    public Hour everyHour(int fromHour, int toHour, int interval) {
        return everyHour(d -> fromHour, d -> toHour, interval);
    }

    public Hour everyHour(IntFunction<Day> from, IntFunction<Day> to, int interval) {
        return everyDay().everyHour(from, to, interval);
    }

    public Minute everyMinute() {
        return everyMinute(1);
    }

    public Minute everyMinute(int interval) {
        return everyMinute(0, 59, interval);
    }

    public Minute everyMinute(int fromMinute, int toMinute, int interval) {
        return everyMinute(h -> fromMinute, h -> toMinute, interval);
    }

    public Minute everyMinute(IntFunction<Hour> from, IntFunction<Hour> to, int interval) {
        return everyHour().everyMinute(from, to, interval);
    }

    public Second everySecond() {
        return everySecond(1);
    }

    public Second everySecond(int interval) {
        return everySecond(0, 59, interval);
    }

    public Second everySecond(int fromSecond, int toSecond, int interval) {
        return everySecond(m -> fromSecond, m -> toSecond, interval);
    }

    public Second everySecond(IntFunction<Minute> from, IntFunction<Minute> to, int interval) {
        return everyMinute().everySecond(from, to, interval);
    }

    public TheYear year() {
        return year(getTime().getYear());
    }

    public TheYear year(int year) {
        return new ThisYear(this, year);
    }

    public TheMonth month() {
        return year().month(getTime().getMonthValue());
    }

    public TheMonth month(int year, int month) {
        return year(year).month(month);
    }

    public TheWeek week() {
        return month().week(getTime().get(WeekFields.ISO.weekOfMonth()));
    }

    public TheWeek week(int year, int month, int weekOfMonth) {
        return month(year, month).week(weekOfMonth);
    }

    public TheDay day() {
        return month().day(getTime().getDayOfMonth());
    }

    public TheDay day(int year, int month, int dayOfMonth) {
        return month(year, month).day(dayOfMonth);
    }

    public TheHour hour() {
        return day().hour(getTime().getHour());
    }

    public TheHour hour(int hourOfDay) {
        return day().hour(hourOfDay);
    }

    public TheMinute minute() {
        return hour().minute(getTime().getMinute());
    }

    public TheMinute minute(int minute) {
        return hour().minute(minute);
    }

    public TheMinute at(int hourOfDay, int minute) {
        return day().hour(hourOfDay).minute(minute);
    }

    public TheSecond at(int hourOfDay, int minute, int second) {
        return day().hour(hourOfDay).minute(minute).second(second);
    }
}
