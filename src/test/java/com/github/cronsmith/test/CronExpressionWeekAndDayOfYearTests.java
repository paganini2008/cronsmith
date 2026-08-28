package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * The week-scoped and year-scoped parts of the builder: weeks of a month, weeks of a year, days of
 * a year and the ranges over them.
 * <p>
 * Several of these have no cron representation, so they are checked by the date-times they produce
 * rather than by a printed expression.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronExpressionWeekAndDayOfYearTests {

    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    private static int year() {
        return CronTestSupport.currentYear();
    }

    private static List<LocalDateTime> fire(CronExpression cronExpression, int n) {
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.consume(list::add, n);
        return list;
    }

    private static void assertStrictlyIncreasing(List<LocalDateTime> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i).isAfter(list.get(i - 1)), "not increasing: " + list.get(i - 1) + " -> " + list.get(i));
        }
    }

    // ------------------------------------------------------------------ //
    // Days inside a week                                                 //
    // ------------------------------------------------------------------ //

    @Test
    public void testEveryNthDayOfAWeek() {
        List<LocalDateTime> list = fire(builder().everyMonth().everyWeek().everyDay(2).at(12, 0), 8);
        assertStrictlyIncreasing(list);
        assertEquals("0 0 12 ? * MON-SUN/2",
                builder().everyMonth().everyWeek().everyDay(2).at(12, 0).toString());
    }

    @Test
    public void testEveryNthDayOfEveryNthWeek() {
        List<LocalDateTime> list =
                fire(builder().everyMonth().everyWeek(2).everyDay(3).at(12, 0), 8);
        assertStrictlyIncreasing(list);
    }

    @Test
    public void testEveryNthDayOfAFixedWeek() {
        List<LocalDateTime> list = fire(builder().everyMonth().week(2).everyDay(2).at(12, 0), 8);
        assertStrictlyIncreasing(list);
    }

    @Test
    public void testEveryNthDayOfTheLastWeek() {
        CronExpression cronExpression = builder().everyMonth().lastWeek().everyDay(2).at(12, 0);
        assertEquals("0 0 12 ? * MONL,WEDL,FRIL,SUNL", cronExpression.toString());
        assertStrictlyIncreasing(fire(builder().everyMonth().lastWeek().everyDay(2).at(12, 0), 8));
    }

    @Test
    public void testEveryNthDayOfAWeekOfTheYear() {
        assertStrictlyIncreasing(fire(builder().year().week(5).everyDay(2).at(12, 0), 3));
    }

    // ------------------------------------------------------------------ //
    // Weeks of a month                                                   //
    // ------------------------------------------------------------------ //

    @Test
    public void testWeekOfMonthRangesProduceMondays() {
        assertMondays(builder().everyMonth().week(2).toWeek(4).Mon().at(12, 0), 8);
        assertMondays(builder().everyMonth().week(2).toWeek(4, 2).Mon().at(12, 0), 6);
        assertMondays(builder().everyMonth().week(2).andWeek(4).Mon().at(12, 0), 8);
        assertMondays(builder().everyMonth().week(2).toLastWeek().Mon().at(12, 0), 8);
        assertMondays(builder().everyMonth().week(2).toLastWeek(2).Mon().at(12, 0), 6);
        assertMondays(builder().everyMonth().week(2).andLastWeek().Mon().at(12, 0), 6);
    }

    @Test
    public void testWeekOfMonthAccessors() {
        com.github.cronsmith.cron.Week week = builder().everyMonth().week(2);
        assertEquals(year(), week.getYear());
        assertTrue(week.getMonth() >= 1 && week.getMonth() <= 12);
        assertTrue(week.getWeek() >= 1 && week.getWeek() <= 6);
        assertTrue(week.getWeekOfYear() >= 1 && week.getWeekOfYear() <= 53);
    }

    @Test
    public void testDescendingWeekOfMonthRangeIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().everyMonth().week(4).toWeek(2);
    
        });
    }

    @Test
    public void testDescendingWeekOfMonthListIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().everyMonth().week(4).andWeek(2);
    
        });
    }

    @Test
    public void testWeekOfMonthIntervalMustBePositive() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().everyMonth().week(1).toWeek(3, 0);
    
        });
    }

    // ------------------------------------------------------------------ //
    // Weeks of a year                                                    //
    // ------------------------------------------------------------------ //

    @Test
    public void testWeekOfYearRangesProduceMondays() {
        assertMondays(builder().year().week(5).toWeek(20, 3).Mon().at(12, 0), 5);
        assertMondays(builder().year().week(5).andWeek(20).Mon().at(12, 0), 2);
        assertMondays(builder().year().week(5).andLastWeek().Mon().at(12, 0), 2);
        assertMondays(builder().year().week(5).toLastWeek(2).Mon().at(12, 0), 6);
        assertMondays(builder().year().lastWeek().Mon().at(12, 0), 1);
    }

    @Test
    public void testWeekOfYearAccessors() {
        com.github.cronsmith.cron.Week week = builder().year().week(10);
        assertEquals(year(), week.getYear());
        assertTrue(week.getWeekOfYear() >= 1 && week.getWeekOfYear() <= 53);
        assertTrue(week.getWeek() >= 1 && week.getWeek() <= 6);
        assertTrue(week.getMonth() >= 1 && week.getMonth() <= 12);
    }

    @Test
    public void testDescendingWeekOfYearRangeIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().year().week(20).toWeek(10);
    
        });
    }

    @Test
    public void testDescendingWeekOfYearListIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().year().week(20).andWeek(10);
    
        });
    }

    // ------------------------------------------------------------------ //
    // Days of a year                                                     //
    // ------------------------------------------------------------------ //

    @Test
    public void testDayOfYearList() {
        List<LocalDateTime> list = fire(builder().year().day(100).andDay(200).at(12, 0), 4);
        assertEquals(2, list.size());
        assertEquals(100, list.get(0).getDayOfYear());
        assertEquals(200, list.get(1).getDayOfYear());
    }

    @Test
    public void testDayOfYearRange() {
        List<LocalDateTime> list = fire(builder().year().day(100).toDay(120, 5).at(12, 0), 10);
        assertStrictlyIncreasing(list);
        for (LocalDateTime ldt : list) {
            assertTrue(ldt.getDayOfYear() >= 100 && ldt.getDayOfYear() <= 120, ldt.toString());
            assertEquals(0, (ldt.getDayOfYear() - 100) % 5);
        }
    }

    @Test
    public void testDayOfYearLastDayVariants() {
        int lastDay = builder().year().getLastDayOfYear();
        assertEquals(lastDay,
                lastOf(fire(builder().year().day(100).andLastDay().at(12, 0), 4)).getDayOfYear());
        assertEquals(lastDay - 3,
                lastOf(fire(builder().year().day(100).andLastDay(3).at(12, 0), 4)).getDayOfYear());
        assertStrictlyIncreasing(fire(builder().year().day(100).toLastDay(50).at(12, 0), 8));
    }

    @Test
    public void testDayOfYearWeekdayVariants() {
        LocalDateTime lastWeekday =
                lastOf(fire(builder().year().day(100).andLastWeekday().at(12, 0), 4));
        assertNotWeekend(lastWeekday);
        LocalDateTime latestWeekday =
                lastOf(fire(builder().year().day(100).andLatestWeekday(200).at(12, 0), 4));
        assertNotWeekend(latestWeekday);
        assertStrictlyIncreasing(fire(builder().year().day(100).toLastWeekday(50).at(12, 0), 6));
        assertStrictlyIncreasing(
                fire(builder().year().day(100).toLatestWeekday(200, 20).at(12, 0), 6));
    }

    @Test
    public void testLastDayOfYear() {
        List<LocalDateTime> list = fire(builder().year().lastDay().at(12, 0), 2);
        assertEquals(1, list.size());
        assertEquals(12, list.get(0).getMonthValue());
        assertEquals(31, list.get(0).getDayOfMonth());
    }

    @Test
    public void testDescendingDayOfYearRangeIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().year().day(200).toDay(100);
    
        });
    }

    @Test
    public void testDescendingDayOfYearListIsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().year().day(200).andDay(100);
    
        });
    }

    // ------------------------------------------------------------------ //
    // Multi-year schedules                                                //
    // ------------------------------------------------------------------ //

    @Test
    public void testEveryNthYearWithYearScopedFields() {
        List<LocalDateTime> lastDays = fire(builder().everyYear(2).lastDay().at(12, 0), 3);
        assertStrictlyIncreasing(lastDays);
        for (LocalDateTime ldt : lastDays) {
            assertEquals(12, ldt.getMonthValue());
            assertEquals(31, ldt.getDayOfMonth());
        }

        assertEquals("0 0 12 ? DEC MONL " + year() + "/2",
                builder().everyYear(2).lastWeek().Mon().at(12, 0).toString());
        assertStrictlyIncreasing(fire(builder().everyYear(2).week(5).Mon().at(12, 0), 3));
        assertStrictlyIncreasing(fire(builder().everyYear(2).day(100).at(12, 0), 3));
    }

    @Test
    public void testEveryYearAccessors() {
        com.github.cronsmith.cron.Year everyYear = builder().everyYear(2);
        assertTrue(everyYear.getYear() >= year());
        assertTrue(everyYear.getWeekCountOfYear() >= 52);
        assertTrue(everyYear.getLastDayOfYear() >= 365);
        assertEquals(everyYear.getLastDayOfYear() - 5, everyYear.getLastDayOfYear(5));
        assertTrue(everyYear.getLatestWeekdayOfYear(200) > 0);
        assertNotNull(everyYear.getTime());
        assertEquals(java.time.Year.isLeap(everyYear.getYear()), everyYear.isLeapYear());
    }

    @Test
    public void testYearAboveTheMaximumIsRejectedByEveryYear() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        builder().everyYear(3000, 1).Jan().day(1).at(0, 0).toString();
    
        });
    }

    private static void assertMondays(CronExpression cronExpression, int n) {
        List<LocalDateTime> list = fire(cronExpression, n);
        assertTrue(!list.isEmpty(), "nothing fired");
        assertStrictlyIncreasing(list);
        for (LocalDateTime ldt : list) {
            assertEquals(DayOfWeek.MONDAY, ldt.getDayOfWeek(), ldt.toString());
            assertEquals(12, ldt.getHour());
        }
    }

    private static void assertNotWeekend(LocalDateTime ldt) {
        assertTrue(ldt.getDayOfWeek() != DayOfWeek.SATURDAY
                && ldt.getDayOfWeek() != DayOfWeek.SUNDAY, ldt + " is a weekend");
    }

    private static LocalDateTime lastOf(List<LocalDateTime> list) {
        return list.get(list.size() - 1);
    }

}
