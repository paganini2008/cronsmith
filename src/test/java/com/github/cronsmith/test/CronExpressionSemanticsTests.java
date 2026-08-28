package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * The rules a cron expression is expected to follow everywhere else: what a slashed field means,
 * what {@code #} and {@code L} pick out of a month, how weekdays are numbered, and that asking for
 * the next run never skips one.
 *
 * @Description: CronExpressionSemanticsTests
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
public class CronExpressionSemanticsTests {

    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    private static List<LocalDateTime> fire(CronExpression cronExpression, int n) {
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.consume(list::add, n);
        return list;
    }

    // ------------------------------------------------------------------ //
    // A slashed field starts at its own starting point                   //
    // ------------------------------------------------------------------ //

    @Test
    public void testSlashedSecondsStartAtZero() {
        List<LocalDateTime> list = fire(CRON.parse("*/15 * * * * ?"), 4);
        assertEquals(0, list.get(0).getSecond());
        assertEquals(15, list.get(1).getSecond());
        assertEquals(30, list.get(2).getSecond());
        assertEquals(45, list.get(3).getSecond());
    }

    @Test
    public void testSlashedMinutesStartAtZero() {
        List<LocalDateTime> list = fire(CRON.parse("0 */15 * * * ?"), 4);
        assertEquals(0, list.get(0).getMinute());
        assertEquals(15, list.get(1).getMinute());
        assertEquals(30, list.get(2).getMinute());
        assertEquals(45, list.get(3).getMinute());
    }

    @Test
    public void testSlashedHoursStartAtZero() {
        List<LocalDateTime> list = fire(CRON.parse("0 0 */6 * * ?"), 4);
        assertEquals(0, list.get(0).getHour());
        assertEquals(6, list.get(1).getHour());
        assertEquals(12, list.get(2).getHour());
        assertEquals(18, list.get(3).getHour());
    }

    @Test
    public void testSlashedDayOfMonthStartsAtTheFirst() {
        List<LocalDateTime> list = fire(CRON.parse("0 0 0 */5 * ?"), 4);
        assertEquals(1, list.get(0).getDayOfMonth());
        assertEquals(6, list.get(1).getDayOfMonth());
        assertEquals(11, list.get(2).getDayOfMonth());
        assertEquals(16, list.get(3).getDayOfMonth());
    }

    @Test
    public void testSlashedMonthStartsAtJanuary() {
        List<LocalDateTime> list = fire(CRON.parse("0 0 0 1 */3 ?"), 4);
        assertEquals(1, list.get(0).getMonthValue());
        assertEquals(4, list.get(1).getMonthValue());
        assertEquals(7, list.get(2).getMonthValue());
        assertEquals(10, list.get(3).getMonthValue());
    }

    @Test
    public void testSlashedFieldWithAnExplicitStart() {
        List<LocalDateTime> list = fire(CRON.parse("0 10/15 * * * ?"), 4);
        assertEquals(10, list.get(0).getMinute());
        assertEquals(25, list.get(1).getMinute());
        assertEquals(40, list.get(2).getMinute());
        assertEquals(55, list.get(3).getMinute());
    }

    // ------------------------------------------------------------------ //
    // '#' is the nth weekday of the month, 'L' the last one              //
    // ------------------------------------------------------------------ //

    @Test
    public void testHashPicksTheNthWeekdayOfTheMonth() {
        for (int ordinal = 1; ordinal <= 4; ordinal++) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                String cron = "0 0 12 ? * " + name(dayOfWeek) + "#" + ordinal;
                for (LocalDateTime ldt : fire(CRON.parse(cron), 6)) {
                    assertEquals(dayOfWeek, ldt.getDayOfWeek(), cron + " -> " + ldt);
                    assertEquals(ldt.toLocalDate(), nth(ldt, ordinal, dayOfWeek), cron + " -> " + ldt);
                }
            }
        }
    }

    @Test
    public void testFifthOccurrenceSkipsMonthsThatHaveNone() {
        String cron = "0 0 12 ? * FRI#5";
        for (LocalDateTime ldt : fire(CRON.parse(cron), 6)) {
            assertEquals(DayOfWeek.FRIDAY, ldt.getDayOfWeek());
            assertEquals(ldt.toLocalDate(), nth(ldt, 5, DayOfWeek.FRIDAY), ldt.toString());
            assertTrue(ldt.getDayOfMonth() >= 29, ldt + " is not a fifth Friday");
        }
    }

    @Test
    public void testLastPicksTheLastWeekdayOfTheMonth() {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            String cron = "0 0 12 ? * " + name(dayOfWeek) + "L";
            for (LocalDateTime ldt : fire(CRON.parse(cron), 6)) {
                assertEquals(dayOfWeek, ldt.getDayOfWeek(), cron + " -> " + ldt);
                assertEquals(ldt.toLocalDate(), ldt.toLocalDate()
                        .with(TemporalAdjusters.lastInMonth(dayOfWeek)), cron + " -> " + ldt);
            }
        }
    }

    @Test
    public void testWeekOfMonthBuilderAgreesWithTheHashTag() {
        for (int ordinal = 1; ordinal <= 4; ordinal++) {
            CronExpression built = builder().everyMonth().week(ordinal).Wed().at(12, 0);
            assertEquals("0 0 12 ? * WED#" + ordinal, built.toString());
            assertEquals(fire(CRON.parse("0 0 12 ? * WED#" + ordinal), 5),
                    fire(builder().everyMonth().week(ordinal).Wed().at(12, 0), 5));
        }
    }

    @Test
    public void testLastWeekBuilderAgreesWithTheLastTag() {
        CronExpression built = builder().everyMonth().lastWeek().Wed().at(12, 0);
        assertEquals("0 0 12 ? * WEDL", built.toString());
        assertEquals(fire(CRON.parse("0 0 12 ? * WEDL"), 5),
                fire(builder().everyMonth().lastWeek().Wed().at(12, 0), 5));
    }

    // ------------------------------------------------------------------ //
    // Weekday numbering                                                  //
    // ------------------------------------------------------------------ //

    @Test
    public void testNumericWeekdaysFollowCronNumbering() {
        // SUN=1 .. SAT=7, the numbering Quartz and AWS EventBridge use.
        DayOfWeek[] expected = {DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
        for (int number = 1; number <= 7; number++) {
            String cron = "0 0 12 ? * " + number;
            for (LocalDateTime ldt : fire(CRON.parse(cron), 3)) {
                assertEquals(expected[number - 1], ldt.getDayOfWeek(), cron + " -> " + ldt);
            }
        }
    }

    @Test
    public void testNumericWeekdayRangeMatchesTheNamedOne() {
        assertEquals(fire(CRON.parse("0 0 12 ? * MON-FRI"), 10),
                fire(CRON.parse("0 0 12 ? * 2-6"), 10));
    }

    // ------------------------------------------------------------------ //
    // Asking for the next run must not skip one                          //
    // ------------------------------------------------------------------ //

    /**
     * The regression this guards: a fixed-time expression used to answer with tomorrow when asked
     * earlier the same day, quietly losing one run every time a schedule was (re)started.
     */
    @Test
    public void testNextFiredDateTimeDoesNotSkipTodaysRun() {
        LocalDateTime morning = CronTestSupport.stableStartTime().plusMonths(2).withHour(10);
        assertEquals(morning.withHour(12).withMinute(0),
                daily(morning, 12, 0).getNextFiredDateTime(morning));
        assertEquals(morning.withHour(10).withMinute(30),
                daily(morning, 10, 30).getNextFiredDateTime(morning));
        assertEquals(morning.plusDays(1).withHour(9).withMinute(0),
                daily(morning, 9, 0).getNextFiredDateTime(morning));
    }

    @Test
    public void testNextFiredDateTimeDoesNotSkipTheCurrentMonthsRun() {
        LocalDateTime reference = CronTestSupport.stableStartTime().plusMonths(2).withHour(10);
        CronExpression monthly = new CronBuilder().setStartTime(reference).everyMonth()
                .day(reference.getDayOfMonth()).at(12, 0);
        assertEquals(reference.withHour(12).withMinute(0),
                monthly.getNextFiredDateTime(reference));
    }

    @Test
    public void testNextFiredDateTimeHonoursTheFirstOfSeveralHours() {
        LocalDateTime reference = CronTestSupport.stableStartTime().plusMonths(2).withHour(10);
        CronExpression twiceADay = new CronBuilder().setStartTime(reference).everyDay().hour(11)
                .andHour(16).minute(0);
        assertEquals(reference.withHour(11).withMinute(0),
                twiceADay.getNextFiredDateTime(reference));
    }

    @Test
    public void testSyncedExpressionStartsAtWhereItWasSynced() {
        LocalDateTime reference = CronTestSupport.stableStartTime().plusMonths(2).withHour(10);
        CronExpression daily = daily(reference, 12, 0);
        List<LocalDateTime> list = new ArrayList<>();
        daily.sync(reference).consume(list::add, 3);
        assertEquals(reference.withHour(12).withMinute(0), list.get(0));
        assertEquals(reference.plusDays(1).withHour(12).withMinute(0), list.get(1));
    }

    // ------------------------------------------------------------------ //
    // Unix crontab lines                                                 //
    // ------------------------------------------------------------------ //

    @Test
    public void testFiveFieldLineIsReadAsCrontab() {
        assertEquals("0 */5 * * * ?", CRON.parse("*/5 * * * *").toString());
        assertEquals("0 30 2 1 * ?", CRON.parse("30 2 1 * *").toString());
        assertEquals("0 0 9 ? * MON-FRI", CRON.parse("0 9 * * 1-5").toString());
        assertEquals("0 15 10 ? * MON-FRI", CRON.parse("15 10 * * MON-FRI").toString());
    }

    @Test
    public void testCrontabNumbersSundayAsZeroOrSeven() {
        for (String cron : new String[] {"0 0 * * 0", "0 0 * * 7", "0 0 * * SUN"}) {
            assertEquals("0 0 0 ? * SUN", CRON.parse(cron).toString(), cron);
            for (LocalDateTime ldt : fire(CRON.parse(cron), 3)) {
                assertEquals(DayOfWeek.SUNDAY, ldt.getDayOfWeek(), cron);
            }
        }
    }

    @Test
    public void testCrontabWeekdayNumbersStartAtMonday() {
        // Crontab's 1 is Monday, whereas the same number means Sunday in a Quartz expression.
        for (LocalDateTime ldt : fire(CRON.parse("0 0 * * 1"), 3)) {
            assertEquals(DayOfWeek.MONDAY, ldt.getDayOfWeek());
        }
        for (LocalDateTime ldt : fire(CRON.parse("0 0 12 ? * 1"), 3)) {
            assertEquals(DayOfWeek.SUNDAY, ldt.getDayOfWeek());
        }
    }

    @Test
    public void testCrontabLineFiresAtTheExpectedTimes() {
        List<LocalDateTime> list = fire(CRON.parse("30 2 * * *"), 3);
        for (LocalDateTime ldt : list) {
            assertEquals(2, ldt.getHour());
            assertEquals(30, ldt.getMinute());
            assertEquals(0, ldt.getSecond());
        }
        assertNotNull(list.get(0));
    }

    @Test
    public void testCrontabRestrictingBothDayFieldsIsReported() {
        org.junit.jupiter.api.Assertions.assertThrows(com.github.cronsmith.parser.CronParserException.class, () -> {
        // Crontab fires on either field, which this library has no way of expressing.
        CRON.parse("0 0 1 * MON");
    
        });
    }

    private static CronExpression daily(LocalDateTime startTime, int hour, int minute) {
        return new CronBuilder().setStartTime(startTime).everyDay().at(hour, minute);
    }

    private static LocalDate nth(LocalDateTime within, int ordinal, DayOfWeek dayOfWeek) {
        return within.toLocalDate().withDayOfMonth(1)
                .with(TemporalAdjusters.dayOfWeekInMonth(ordinal, dayOfWeek));
    }

    private static String name(DayOfWeek dayOfWeek) {
        return dayOfWeek.name().substring(0, 3);
    }

}
