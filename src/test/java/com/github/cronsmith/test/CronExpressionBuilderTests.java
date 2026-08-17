package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * @Description: CronExpressionBuilderTests
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CronExpressionBuilderTests {

    private static final int Y = CronTestSupport.currentYear();

    /**
     * A builder pinned to the first day of the current year. Everything below is asserted against
     * {@link CronTestSupport#currentYear()} rather than a literal year, and pinning the start time
     * additionally keeps the assertions stable when a run straddles midnight on New Year's Eve.
     */
    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    @Test
    public void testA() {
        CronExpression cronExpression = builder().everySecond(5);
        assertEquals("*/5 * * * * ?", cronExpression.toString());
    }

    @Test
    public void testB() {
        CronExpression cronExpression = builder().everyMinute(5).second(5).andSecond(10)
                .toSecond(30).andSecond(32).toSecond(59, 2);
        assertEquals("5,10-30,32/2 */5 * * * ?", cronExpression.toString());
    }

    @Test
    public void testC() {
        CronExpression cronExpression = builder().everyMonth().day(10).andDay(15).andDay(16)
                .andLastDay().everyHour(2).everyMinute(5);
        assertEquals("0 */5 */2 10,15,16,L * ?", cronExpression.toString());
    }

    @Test
    public void testD() {
        CronExpression cronExpression = builder().everyMonth(3).day(10).andLastWeekday().hour(12)
                .minute(1).toMinute(15, 1);
        assertEquals("0 1-15 12 10,LW */3 ?", cronExpression.toString());
    }

    @Test
    public void testE() {
        CronExpression cronExpression =
                builder().everyMonth().everyWeek().Mon().toFri().at(15, 10);
        assertEquals("0 10 15 ? * MON-FRI", cronExpression.toString());
    }

    @Test
    public void testF() {
        CronExpression cronExpression = builder().everyMonth().dayOfWeek(3, 6).everyHour(2);
        assertEquals("0 0 */2 ? * SAT#3", cronExpression.toString());
    }

    @Test
    public void testG() {
        CronExpression cronExpression =
                builder().year().Mar().toSept().everyWeek().everyWeekday().at(9, 10);
        assertEquals("0 10 9 ? MAR-SEP MON-FRI " + Y, cronExpression.toString());
    }

    @Test
    public void testH() {
        CronExpression cronExpression =
                builder().year().toYear(Y + 3).everyMonth(2).lastDay().hour(12);
        assertEquals("0 0 12 L */2 ? " + Y + "-" + (Y + 3), cronExpression.toString());
    }

    @Test
    public void testI() {
        CronExpression cronExpression = builder().everyYear().June().andJuly().andAug()
                .latestWeekday(15).hour(10).toHour(18, 2);
        assertEquals("0 0 10-18/2 15W JUN,JUL,AUG ?", cronExpression.toString());
    }

    @Test
    public void testJ() {
        CronExpression cronExpression = builder().everyYear(2).Mar().andApr().andMay().toDec()
                .everyWeek(2).Tues().toFri().hour(10).andHour(12).toHour(22).everyMinute()
                .second(10).andSecond(20).andSecond(30);
        // Every other week is an ordinal restriction, and cron has no range form for '#', so
        // each weekday of the range is listed against each week it applies to.
        assertEquals("10,20,30 * 10,12-22 ? MAR,APR,MAY-DEC "
                + "TUE#1,TUE#3,TUE#5,WED#1,WED#3,WED#5,THU#1,THU#3,THU#5,FRI#1,FRI#3,FRI#5 " + Y
                + "/2", cronExpression.toString());
    }

    @Test
    public void testK() {
        CronExpression cronExpression = builder().year().toYear(Y + 5).andYear(Y + 10).toEnd(2)
                .everyMonth(2, 2).dayOfWeek(2, DayOfWeek.TUESDAY).and(3, DayOfWeek.WEDNESDAY)
                .andLastFri().hour(2).andHour(3).andHour(4).toHour(17, 2).minute(0).toMinute(12, 3)
                .andMinute(15).toMinute(40, 2).andMinute(46).andMinute(48).andMinute(50)
                .everySecond(5);
        assertEquals("*/5 0-12/3,15-40/2,46,48,50 2,3,4-17/2 ? FEB-DEC/2 TUE#2,WED#3,FRIL " + Y
                + "-" + (Y + 5) + "," + (Y + 10) + "/2", cronExpression.toString());
    }

    @Test
    public void testL() {
        CronExpression cronExpression = builder().everyYear(Y, 4).everyMonth(5, 1).day(10)
                .andDay(15).andDay(20).andLastDay(2).hour(10).toHour(15, 1).at(10, 0).andSecond(15)
                .andSecond(30).andSecond(45);
        assertEquals("0,15,30,45 10 10-15 10,15,20,L-2 MAY-DEC ? " + Y + "/4",
                cronExpression.toString());
    }

    @Test
    public void testM() {
        CronExpression cronExpression = builder().year().andYear(Y + 1).andYear(Y + 5).toEnd(2)
                .Mar().andJuly().andSept().dayOfWeek(1, DayOfWeek.SATURDAY)
                .and(2, DayOfWeek.THURSDAY).andLast(DayOfWeek.FRIDAY).hour(0).toHour(12, 3)
                .everyMinute(10).second(0).andSecond(15).andSecond(30).andSecond(45).toSecond(59);
        assertEquals("0,15,30,45/1 */10 0-12/3 ? MAR,JUL,SEP SAT#1,THU#2,FRIL " + Y + ","
                + (Y + 1) + "," + (Y + 5) + "/2", cronExpression.toString());
    }

    @Test
    public void testO() {
        LocalDate future = LocalDate.now(CronTestSupport.BUILDER_ZONE).plusMonths(2).withDayOfMonth(1);
        CronExpression cronExpression = CRON.atFuture(future);
        assertEquals("0 0 0 1 " + monthAbbr(future) + " ? " + future.getYear(),
                cronExpression.toString());
    }

    @Test
    public void testP() {
        LocalDateTime future = LocalDate.now(CronTestSupport.BUILDER_ZONE).plusMonths(2)
                .withDayOfMonth(1).atTime(12, 15, 0);
        CronExpression cronExpression = CRON.atFuture(future);
        assertEquals("0 15 12 1 " + monthAbbr(future.toLocalDate()) + " ? " + future.getYear(),
                cronExpression.toString());
    }

    @Test
    public void testQ() {
        CronExpression cronExpression = CRON.setInterval(LocalTime.of(23, 45, 30));
        assertEquals("30 45 23 * * ?", cronExpression.toString());
    }

    @Test
    public void testR() {
        CronExpression cronExpression = CRON.setInterval(5, TimeUnit.MINUTES);
        cronExpression.sync().consume(ldt -> {
        }, 10);
        assertEquals("0 */5 * * * ?", cronExpression.toString());
    }

    // ------------------------------------------------------------------ //
    // Entry points of CronBuilder                                        //
    // ------------------------------------------------------------------ //

    @Test
    public void testEverySecondFrom() {
        assertEquals("10/15 * * * * ?", builder().everySecond(10, 15).toString());
    }

    @Test
    public void testEveryMinuteDefaults() {
        assertEquals("0 * * * * ?", builder().everyMinute().toString());
        assertEquals("0 */7 * * * ?", builder().everyMinute(7).toString());
    }

    @Test
    public void testEveryHourAndDay() {
        assertEquals("0 0 */2 * * ?", builder().everyHour(2).toString());
        assertEquals("0 0 0 */2 * ?", builder().everyDay(2).toString());
    }

    @Test
    public void testEveryMonthAndYear() {
        assertEquals("0 0 0 1 */3 ?", builder().everyMonth(3).day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN ?", builder().everyYear().Jan().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN ? " + Y + "/2",
                builder().everyYear(2).Jan().day(1).at(0, 0).toString());
    }

    @Test
    public void testTheYearEntryPoints() {
        assertEquals("0 0 0 1 JAN ? " + Y, builder().year().Jan().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN ? " + (Y + 1),
                builder().year(Y + 1).Jan().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN ? " + Y, builder().month().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN ? " + Y, builder().day().at(0, 0).toString());
        assertEquals("0 0 0 1 JAN ? " + Y, builder().hour().toString());
        assertEquals("0 0 0 1 JAN ? " + Y, builder().minute().toString());
        assertEquals("0 2 1 1 JAN ? " + Y, builder().at(1, 2).toString());
        assertEquals("3 2 1 1 JAN ? " + Y, builder().at(1, 2, 3).toString());
        assertEquals("0 0 6 1 JAN ? " + Y, builder().hour(6).toString());
        assertEquals("0 30 0 1 JAN ? " + Y, builder().minute(30).toString());
    }

    @Test
    public void testAbsoluteEntryPoints() {
        assertEquals("0 0 0 1 JUN ? " + Y, builder().month(Y, 6).day(1).at(0, 0).toString());
        assertEquals("0 0 0 20 JUN ? " + Y, builder().day(Y, 6, 20).at(0, 0).toString());
        assertNotNull(builder().week(Y, 6, 2).Mon().at(0, 0).toString());
    }

    // ------------------------------------------------------------------ //
    // Month / day-of-week vocabulary                                     //
    // ------------------------------------------------------------------ //

    @Test
    public void testAllMonthAbbreviations() {
        CronExpression cronExpression = builder().year().Jan().andFeb().andMar().andApr().andMay()
                .andJune().andJuly().andAug().andSept().andOct().andNov().andDec().day(1).at(0, 0);
        assertEquals("0 0 0 1 JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC ? " + Y,
                cronExpression.toString());
    }

    @Test
    public void testMonthRangeShortcuts() {
        assertEquals("0 0 0 1 MAR-DEC ? " + Y,
                builder().year().Mar().toDec().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-MAR ? " + Y,
                builder().year().Jan().toMar().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-APR ? " + Y,
                builder().year().Jan().toApr().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-MAY ? " + Y,
                builder().year().Jan().toMay().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-JUN ? " + Y,
                builder().year().Jan().toJune().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-JUL ? " + Y,
                builder().year().Jan().toJuly().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-AUG ? " + Y,
                builder().year().Jan().toAug().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-OCT ? " + Y,
                builder().year().Jan().toOct().day(1).at(0, 0).toString());
        assertEquals("0 0 0 1 JAN-NOV ? " + Y,
                builder().year().Jan().toNov().day(1).at(0, 0).toString());
    }

    @Test
    public void testMonthAsNumber() {
        CronExpression cronExpression = new CronBuilder().setStartTime(CronTestSupport.stableStartTime())
                .setUseMonthAsNumber(true).year().Mar().toDec().day(1).at(0, 0);
        assertEquals("0 0 0 1 3-12 ? " + Y, cronExpression.toString());
    }

    @Test
    public void testAllDayOfWeekAbbreviations() {
        CronExpression cronExpression = builder().everyMonth().everyWeek().Sun().andMon().andTues()
                .andWed().andThur().andFri().andSat().at(9, 0);
        assertEquals("0 0 9 ? * SUN,MON,TUE,WED,THU,FRI,SAT", cronExpression.toString());
    }

    @Test
    public void testDayOfWeekRangeShortcuts() {
        assertEquals("0 0 9 ? * MON-WED", builder().everyMonth().everyWeek().Mon().toWed().at(9, 0).toString());
        assertEquals("0 0 9 ? * MON-THU", builder().everyMonth().everyWeek().Mon().toThur().at(9, 0).toString());
        assertEquals("0 0 9 ? * MON-SAT", builder().everyMonth().everyWeek().Mon().toSat().at(9, 0).toString());
        assertEquals("0 0 9 ? * MON-SUN", builder().everyMonth().everyWeek().Mon().toSun().at(9, 0).toString());
        assertEquals("0 0 9 ? * MON-FRI",
                builder().everyMonth().everyWeek().Mon().toDay(DayOfWeek.FRIDAY).at(9, 0).toString());
        assertEquals("0 0 9 ? * MON-FRI/2",
                builder().everyMonth().everyWeek().Mon().toDay(DayOfWeek.FRIDAY, 2).at(9, 0).toString());
    }

    @Test
    public void testDayOfWeekAsNumber() {
        CronExpression cronExpression = new CronBuilder().setStartTime(CronTestSupport.stableStartTime())
                .setUseDayOfWeekAsNumber(true).everyMonth().everyWeek().Mon().toFri().at(9, 0);
        // Numbers in the day-of-week field follow cron numbering, where Monday is 2.
        assertEquals("0 0 9 ? * 2-6", cronExpression.toString());
    }

    @Test
    public void testDayOfWeekInMonthVariants() {
        assertEquals("0 0 9 ? * TUE#2",
                builder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY).at(9, 0).toString());
        assertEquals("0 0 9 ? * MON#1,TUE#2,WED#3,THU#4",
                builder().everyMonth().dayOfWeek(1, DayOfWeek.MONDAY).and(2, DayOfWeek.TUESDAY)
                        .and(3, DayOfWeek.WEDNESDAY).and(4, DayOfWeek.THURSDAY).at(9, 0).toString());
        assertEquals("0 0 9 ? * MON#1,MONL,TUEL,WEDL,THUL,FRIL,SATL,SUNL",
                builder().everyMonth().dayOfWeek(1, DayOfWeek.MONDAY).andLastMon().andLastTues()
                        .andLastWed().andLastThur().andLastFri().andLastSat().andLastSun().at(9, 0)
                        .toString());
    }

    // ------------------------------------------------------------------ //
    // L / W day-of-month vocabulary                                      //
    // ------------------------------------------------------------------ //

    @Test
    public void testLastDayVariants() {
        assertEquals("0 0 0 L * ?", builder().everyMonth().lastDay().at(0, 0).toString());
        assertEquals("0 0 0 L-2 * ?", builder().everyMonth().lastDay(2).at(0, 0).toString());
        assertEquals("0 0 0 LW * ?", builder().everyMonth().lastWeekday().at(0, 0).toString());
        assertEquals("0 0 0 15W * ?", builder().everyMonth().latestWeekday(15).at(0, 0).toString());
    }

    @Test
    public void testDayRangesTowardsEndOfMonth() {
        assertEquals("0 0 0 1/5 * ?", builder().everyMonth().day(1).toLastDay(5).at(0, 0).toString());
        assertEquals("0 0 0 1-LW/5 * ?",
                builder().everyMonth().day(1).toLastWeekday(5).at(0, 0).toString());
        assertEquals("0 0 0 1-20W/5 * ?",
                builder().everyMonth().day(1).toLatestWeekday(20, 5).at(0, 0).toString());
        assertEquals("0 0 0 1,LW * ?",
                builder().everyMonth().day(1).andLastWeekday().at(0, 0).toString());
        assertEquals("0 0 0 1,15W * ?",
                builder().everyMonth().day(1).andLatestWeekday(15).at(0, 0).toString());
        assertEquals("0 0 0 1-10 * ?", builder().everyMonth().day(1).toDay(10).at(0, 0).toString());
        assertEquals("0 0 0 1-10/3 * ?",
                builder().everyMonth().day(1).toDay(10, 3).at(0, 0).toString());
        assertEquals("0 0 0 1,L * ?", builder().everyMonth().day(1).andLastDay().at(0, 0).toString());
        assertEquals("0 0 0 1,L-3 * ?",
                builder().everyMonth().day(1).andLastDay(3).at(0, 0).toString());
    }

    @Test
    public void testLastWeekOfMonth() {
        assertEquals("0 0 0 ? * MONL",
                builder().everyMonth().lastWeek().Mon().at(0, 0).toString());
        assertEquals("0 0 9 ? * FRIL",
                builder().everyMonth().lastDayOfWeek(java.time.DayOfWeek.FRIDAY.getValue()).at(9, 0)
                        .toString());
    }

    // ------------------------------------------------------------------ //
    // Hour / minute / second vocabulary                                  //
    // ------------------------------------------------------------------ //

    @Test
    public void testHourMinuteSecondCombinations() {
        assertEquals("0 0 1,3,5 * * ?",
                builder().everyDay().hour(1).andHour(3).andHour(5).minute(0).toString());
        assertEquals("0 0 1-11/2 * * ?",
                builder().everyDay().hour(1).toHour(11, 2).minute(0).toString());
        assertEquals("0 1-11/2 0 * * ?",
                builder().everyDay().hour(0).minute(1).toMinute(11, 2).toString());
        assertEquals("1-11/2 0 0 * * ?",
                builder().everyDay().at(0, 0).second(1).toSecond(11, 2).toString());
        assertEquals("30 15 12 * * ?", builder().everyDay().at(12, 15, 30).toString());
        assertEquals("0 15 12 * * ?", builder().everyDay().hour(12).at(15, 0).toString());
    }

    @Test
    public void testEveryUnitBelowAFixedOne() {
        assertEquals("*/10 * 12 * * ?", builder().everyDay().hour(12).everyMinute().everySecond(10).toString());
        assertEquals("0 */10 12 * * ?", builder().everyDay().hour(12).everyMinute(10).toString());
        assertEquals("0 0 */6 * * ?", builder().everyDay().everyHour(6).toString());
    }

    // ------------------------------------------------------------------ //
    // Invalid arguments                                                  //
    // ------------------------------------------------------------------ //

    @Test(expected = IllegalArgumentException.class)
    public void testIntervalMustBePositive() {
        builder().everySecond(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinuteIntervalMustBePositive() {
        builder().everyMinute(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHourIntervalMustBePositive() {
        builder().everyHour(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDayIntervalMustBePositive() {
        builder().everyDay(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMonthIntervalMustBePositive() {
        builder().everyMonth(1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testYearIntervalMustBePositive() {
        builder().everyYear(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testYearBeforeStartTimeIsRejected() {
        builder().year(Y - 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testYearAboveMaximumIsRejected() {
        builder().year(2100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEveryYearBeforeStartTimeIsRejected() {
        builder().everyYear(Y - 1, 1).Jan().day(1).at(0, 0).toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDescendingYearRangeIsRejected() {
        builder().year(Y + 5).toYear(Y + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDescendingMonthRangeIsRejected() {
        builder().year().Sept().toMar();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDescendingHourRangeIsRejected() {
        builder().everyDay().hour(20).toHour(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDescendingMinuteRangeIsRejected() {
        builder().everyDay().at(0, 30).getBuilder().everyDay().hour(0).minute(30).toMinute(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDescendingSecondRangeIsRejected() {
        builder().everyDay().at(0, 0).second(30).toSecond(10);
    }

    @Test(expected = java.time.DateTimeException.class)
    public void testHourOutOfRangeIsRejected() {
        builder().everyDay().hour(24);
    }

    @Test(expected = java.time.DateTimeException.class)
    public void testMinuteOutOfRangeIsRejected() {
        builder().everyDay().hour(0).minute(60);
    }

    @Test(expected = java.time.DateTimeException.class)
    public void testSecondOutOfRangeIsRejected() {
        builder().everyDay().at(0, 0).second(60);
    }

    @Test(expected = java.time.DateTimeException.class)
    public void testMonthOutOfRangeIsRejected() {
        builder().year().month(13);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPastDateIsRejectedByAtFuture() {
        CRON.atFuture(LocalDate.now(CronTestSupport.BUILDER_ZONE).minusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTodayIsRejectedByAtFuture() {
        CRON.atFuture(LocalDate.now(CronTestSupport.BUILDER_ZONE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPastDateTimeIsRejectedByAtFuture() {
        CRON.atFuture(LocalDateTime.now(CronTestSupport.BUILDER_ZONE).minusHours(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeInitialDelayIsRejected() {
        CRON.setInterval(-1, 5, TimeUnit.MINUTES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroIntervalIsRejected() {
        CRON.setInterval(0, 0, TimeUnit.MINUTES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTimeUnitIsRejected() {
        CRON.setInterval(0, 5, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedTimeUnitIsRejected() {
        CRON.setInterval(5, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSetIntervalForEverySupportedTimeUnit() {
        assertEquals("*/5 * * * * ?", CRON.setInterval(5, TimeUnit.SECONDS).toString());
        assertEquals("0 */5 * * * ?", CRON.setInterval(5, TimeUnit.MINUTES).toString());
        assertEquals("0 0 */5 * * ?", CRON.setInterval(5, TimeUnit.HOURS).toString());
        assertEquals("0 0 0 */5 * ?", CRON.setInterval(5, TimeUnit.DAYS).toString());
    }

    @Test
    public void testSetIntervalWithInitialDelay() {
        CronExpression cronExpression = CRON.setInterval(60_000L, 5, TimeUnit.MILLISECONDS.SECONDS);
        assertTrue(cronExpression.toString().startsWith("*/5 "));
    }

    private static String monthAbbr(LocalDate date) {
        return date.getMonth().name().substring(0, 3);
    }

}
