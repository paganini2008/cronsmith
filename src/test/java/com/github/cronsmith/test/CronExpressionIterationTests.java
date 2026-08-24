package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * These tests look at the date-times an expression actually produces rather than at the string it
 * prints. Expectations are stated as properties ("fires on the last day of its month") instead of
 * literal dates, so they hold in any year the suite happens to run in.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronExpressionIterationTests {

    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    /** Collects the next {@code n} occurrences without disturbing the expression. */
    private static List<LocalDateTime> fire(CronExpression cronExpression, int n) {
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.consume(list::add, n);
        return list;
    }

    private static void assertStrictlyIncreasing(List<LocalDateTime> list) {
        for (int i = 1; i < list.size(); i++) {
            assertTrue("not increasing: " + list.get(i - 1) + " -> " + list.get(i),
                    list.get(i).isAfter(list.get(i - 1)));
        }
    }

    // ------------------------------------------------------------------ //
    // Interval based expressions                                         //
    // ------------------------------------------------------------------ //

    @Test
    public void testEverySecondStepsBySeconds() {
        List<LocalDateTime> list = fire(builder().everySecond(15), 8);
        assertEquals(8, list.size());
        assertStrictlyIncreasing(list);
        for (int i = 1; i < list.size(); i++) {
            assertEquals(15, java.time.Duration.between(list.get(i - 1), list.get(i)).getSeconds());
        }
    }

    @Test
    public void testEveryMinuteStepsByMinutes() {
        List<LocalDateTime> list = fire(builder().everyMinute(30), 6);
        assertStrictlyIncreasing(list);
        for (int i = 1; i < list.size(); i++) {
            assertEquals(30, java.time.Duration.between(list.get(i - 1), list.get(i)).toMinutes());
        }
    }

    @Test
    public void testEveryHourStepsByHours() {
        List<LocalDateTime> list = fire(builder().everyHour(6), 8);
        assertStrictlyIncreasing(list);
        for (int i = 1; i < list.size(); i++) {
            assertEquals(6, java.time.Duration.between(list.get(i - 1), list.get(i)).toHours());
        }
    }

    @Test
    public void testEveryDayStepsWithinEachMonth() {
        List<LocalDateTime> list = fire(builder().everyDay(10).at(12, 0), 6);
        assertStrictlyIncreasing(list);
        for (LocalDateTime ldt : list) {
            assertEquals(12, ldt.getHour());
            assertEquals(0, ldt.getMinute());
        }
    }

    /**
     * An interval starts firing at its starting point, the way a slashed field does in any other
     * cron implementation, so {@code everyMonth(2)} covers January, March, May and so on.
     */
    @Test
    public void testEveryMonthKeepsTheSameDayOfMonth() {
        List<LocalDateTime> list = fire(builder().everyMonth(2).day(15).at(9, 30), 6);
        assertStrictlyIncreasing(list);
        for (LocalDateTime ldt : list) {
            assertEquals(15, ldt.getDayOfMonth());
            assertEquals(ldt.toString(), 1, ldt.getMonthValue() % 2);
        }
    }

    // ------------------------------------------------------------------ //
    // L / W day-of-month semantics                                       //
    // ------------------------------------------------------------------ //

    @Test
    public void testLastDayFiresOnTheLastDayOfEachMonth() {
        for (LocalDateTime ldt : fire(builder().everyMonth().lastDay().at(12, 0), 14)) {
            assertEquals(ldt.toString(), YearMonth.from(ldt).lengthOfMonth(), ldt.getDayOfMonth());
        }
    }

    @Test
    public void testLastDayWithOffset() {
        for (LocalDateTime ldt : fire(builder().everyMonth().lastDay(2).at(12, 0), 12)) {
            assertEquals(ldt.toString(), YearMonth.from(ldt).lengthOfMonth() - 2,
                    ldt.getDayOfMonth());
        }
    }

    @Test
    public void testLastWeekdayIsTheLastWorkingDayOfTheMonth() {
        for (LocalDateTime ldt : fire(builder().everyMonth().lastWeekday().at(12, 0), 14)) {
            assertFalse(ldt + " is a weekend", isWeekend(ldt.toLocalDate()));
            LocalDate next = ldt.toLocalDate().plusDays(1);
            while (next.getMonthValue() == ldt.getMonthValue()) {
                assertTrue(next + " is a later weekday", isWeekend(next));
                next = next.plusDays(1);
            }
        }
    }

    @Test
    public void testLatestWeekdayStaysInsideItsMonth() {
        for (LocalDateTime ldt : fire(builder().everyMonth().latestWeekday(15).at(12, 0), 14)) {
            assertFalse(ldt + " is a weekend", isWeekend(ldt.toLocalDate()));
            assertTrue(ldt + " drifted too far", Math.abs(ldt.getDayOfMonth() - 15) <= 2);
        }
    }

    @Test
    public void testDayListIsWalkedInOrder() {
        List<LocalDateTime> list =
                fire(builder().everyMonth().day(1).andDay(10).andDay(20).andLastDay().at(12, 0), 12);
        assertStrictlyIncreasing(list);
        for (LocalDateTime ldt : list) {
            int day = ldt.getDayOfMonth();
            assertTrue(ldt.toString(), day == 1 || day == 10 || day == 20
                    || day == YearMonth.from(ldt).lengthOfMonth());
        }
    }

    // ------------------------------------------------------------------ //
    // Calendar corner cases                                              //
    // ------------------------------------------------------------------ //

    @Test
    public void testFebruaryLengthIsRespected() {
        for (LocalDateTime ldt : fire(builder().everyMonth().lastDay().at(0, 0), 26)) {
            if (ldt.getMonthValue() == 2) {
                assertEquals(ldt.toString(), ldt.toLocalDate().isLeapYear() ? 29 : 28,
                        ldt.getDayOfMonth());
            }
        }
    }

    @Test
    public void testLeapDayIsReachableFromALeapYear() {
        int leapYear = nextLeapYear(CronTestSupport.currentYear());
        CronExpression cronExpression = new CronBuilder()
                .setStartTime(LocalDate.of(leapYear, 1, 1).atStartOfDay()).year(leapYear).Feb()
                .day(29).at(12, 0);
        List<LocalDateTime> list = fire(cronExpression, 1);
        assertEquals(LocalDate.of(leapYear, 2, 29).atTime(12, 0), list.get(0));
    }

    @Test
    public void testYearRangeStopsAtItsUpperBound() {
        int y = CronTestSupport.currentYear();
        List<LocalDateTime> list =
                fire(builder().year().toYear(y + 2).Jan().day(1).at(0, 0), 10);
        assertStrictlyIncreasing(list);
        for (LocalDateTime ldt : list) {
            assertTrue(ldt.toString(), ldt.getYear() >= y && ldt.getYear() <= y + 2);
        }
        assertEquals(3, list.size());
    }

    @Test
    public void testSingleOccurrenceExpressionEndsAfterOneFiring() {
        int y = CronTestSupport.currentYear();
        assertEquals(1, fire(builder().year(y).Jan().day(1).at(0, 0, 0), 10).size());
    }

    // ------------------------------------------------------------------ //
    // Day-of-week semantics                                              //
    // ------------------------------------------------------------------ //

    @Test
    public void testWeekdayRangeOnlyFiresOnWorkingDays() {
        for (LocalDateTime ldt : fire(builder().everyMonth().everyWeek().everyWeekday().at(9, 0), 20)) {
            assertFalse(ldt + " is a weekend", isWeekend(ldt.toLocalDate()));
        }
    }

    @Test
    public void testExplicitWeekdayListOnlyFiresOnThoseDays() {
        for (LocalDateTime ldt : fire(
                builder().everyMonth().everyWeek().Mon().andWed().andFri().at(9, 0), 20)) {
            DayOfWeek dow = ldt.getDayOfWeek();
            assertTrue(ldt.toString(), dow == DayOfWeek.MONDAY || dow == DayOfWeek.WEDNESDAY
                    || dow == DayOfWeek.FRIDAY);
        }
    }

    @Test
    public void testDayOfWeekInMonthAlwaysFiresOnThatWeekday() {
        for (LocalDateTime ldt : fire(
                builder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY).at(12, 0), 12)) {
            assertEquals(ldt.toString(), DayOfWeek.TUESDAY, ldt.getDayOfWeek());
        }
    }

    @Test
    public void testParsedWeekdayExpressionsFireOnTheRightWeekday() {
        for (LocalDateTime ldt : fire(CRON.parse("0 0 12 ? * SAT,SUN"), 10)) {
            assertTrue(ldt.toString(), isWeekend(ldt.toLocalDate()));
        }
    }

    // ------------------------------------------------------------------ //
    // list(..) / consume(..) contracts                                   //
    // ------------------------------------------------------------------ //

    @Test
    public void testConsumeWithNegativeCountIsBoundedByTheSchedule() {
        int y = CronTestSupport.currentYear();
        List<LocalDateTime> list = new ArrayList<>();
        builder().year(y).Jan().day(1).at(0, 0, 0).consume(list::add, -1);
        assertEquals(1, list.size());
    }

    @Test
    public void testListReturnsNothingForAWindowInThePast() {
        LocalDateTime start = CronTestSupport.stableStartTime();
        List<LocalDateTime> list = builder().everyMonth().day(15).at(12, 0)
                .list(start.minusYears(2), start.minusYears(1));
        assertTrue(list.toString(), list.isEmpty());
    }

    @Test
    public void testGetTimeAccessorsAgreeWithTheFiredValue() {
        CronExpression cronExpression = builder().everyMonth().day(15).at(9, 30, 45);
        LocalDateTime ldt = cronExpression.getTime();
        assertNotNull(ldt);
        assertEquals(15, ldt.getDayOfMonth());
        assertEquals(9, ldt.getHour());
        assertEquals(30, ldt.getMinute());
        assertEquals(45, ldt.getSecond());
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static int nextLeapYear(int from) {
        int year = from;
        while (!java.time.Year.isLeap(year)) {
            year++;
        }
        return year;
    }

}
