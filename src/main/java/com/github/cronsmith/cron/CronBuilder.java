
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDate;
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
        this.zoneId = ZoneId.of("UTC");
        this.startTime = LocalDateTime.now(zoneId).withNano(0);
        this.useMonthAsNumber = false;
        this.useDayOfWeekAsNumber = false;
    }

    private ZoneId zoneId;
    private LocalDateTime startTime;
    private boolean useMonthAsNumber;
    private boolean useDayOfWeekAsNumber;
    private boolean useNaturalScale;

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

    public boolean isUseNaturalScale() {
        return useNaturalScale;
    }

    public CronBuilder setUseNaturalScale(boolean useNaturalScale) {
        this.useNaturalScale = useNaturalScale;
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
        return LocalDate.of(startTime.getYear(), 1, 1).atStartOfDay();
    }

    @Override
    public CronExpression sync(LocalDateTime target) {
        return this;
    }

    @Override
    public CronExpression getParent() {
        return null;
    }

    @Override
    public boolean supportCronString() {
        return false;
    }

    public Year everyYear() {
        return everyYear(1);
    }

    public Year everyYear(int interval) {
        return everyYear(getTime().getYear(), interval);
    }

    public Year everyYear(int fromYear, int interval) {
        return everyYear(e -> fromYear, interval);
    }

    public Year everyYear(IntFunction<CronBuilder> from, int interval) {
        return new EveryYear(this, from, interval);
    }

    public Month everyMonth() {
        return everyMonth(1);
    }

    public Month everyMonth(int interval) {
        return everyMonth(1, interval);
    }

    public Month everyMonth(int fromMonth, int interval) {
        return everyMonth(y -> fromMonth, interval);
    }

    public Month everyMonth(IntFunction<Year> from, int interval) {
        return everyYear().everyMonth(from, interval);
    }

    public Week everyWeek() {
        return everyWeek(1);
    }

    public Week everyWeek(int interval) {
        return everyWeek(1, interval);
    }

    public Week everyWeek(int fromWeekOfMonth, int interval) {
        return everyWeek(m -> fromWeekOfMonth, interval);
    }

    public Week everyWeek(IntFunction<Month> from, int interval) {
        return everyMonth().everyWeek(from, interval);
    }

    public Day everyDay() {
        return everyDay(1);
    }

    public Day everyDay(int interval) {
        return everyDay(1, interval);
    }

    public Day everyDay(int fromDayOfMonth, int interval) {
        return everyDay(m -> fromDayOfMonth, interval);
    }

    public Day everyDay(IntFunction<Month> from, int interval) {
        return everyMonth().everyDay(from, interval);
    }

    public Hour everyHour() {
        return everyHour(1);
    }

    public Hour everyHour(int interval) {
        return everyHour(0, interval);
    }

    public Hour everyHour(int fromHour, int interval) {
        return everyHour(d -> fromHour, interval);
    }

    public Hour everyHour(IntFunction<Day> from, int interval) {
        return everyDay().everyHour(from, interval);
    }

    public Minute everyMinute() {
        return everyMinute(1);
    }

    public Minute everyMinute(int interval) {
        return everyMinute(0, interval);
    }

    public Minute everyMinute(int fromMinute, int interval) {
        return everyMinute(h -> fromMinute, interval);
    }

    public Minute everyMinute(IntFunction<Hour> from, int interval) {
        return everyHour().everyMinute(from, interval);
    }

    public Second everySecond() {
        return everySecond(1);
    }

    public Second everySecond(int interval) {
        return everySecond(0, interval);
    }

    public Second everySecond(int fromSecond, int interval) {
        return everySecond(m -> fromSecond, interval);
    }

    public Second everySecond(IntFunction<Minute> from, int interval) {
        return everyMinute().everySecond(from, interval);
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
