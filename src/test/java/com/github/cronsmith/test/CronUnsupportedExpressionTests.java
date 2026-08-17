package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.Year;

/**
 *
 * Not every schedule this library can express fits into cron syntax. A day of the year or a week of
 * the year has no cron field to live in, and the builder itself is not a schedule at all. Such an
 * expression still iterates perfectly well - it just refuses to print itself as cron, which is what
 * {@code supportCronString()} announces.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronUnsupportedExpressionTests {

    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    private static int year() {
        return CronTestSupport.currentYear();
    }

    // ------------------------------------------------------------------ //
    // Declared as unsupported                                            //
    // ------------------------------------------------------------------ //

    @Test
    public void testBuilderIsNotACronExpression() {
        CronBuilder builder = builder();
        assertFalse(builder.supportCronString());
        try {
            builder.toCronString();
            fail("expected the builder to refuse to render itself");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        assertNull(builder.getParent());
    }

    @Test
    public void testDayOfYearCannotBeRendered() {
        Day day = builder().year().day(200);
        assertFalse(day.supportCronString());
        assertUnrenderable(day.at(12, 0));
    }

    @Test
    public void testLastDayOfYearCannotBeRendered() {
        assertUnrenderable(builder().year().lastDay().at(12, 0));
    }

    @Test
    public void testWeekOfYearCannotBeRendered() {
        assertFalse(builder().year().week(10).supportCronString());
        assertUnrenderable(builder().year().week(10).Mon().at(12, 0));
        assertUnrenderable(builder().year().week(10).toWeek(20, 2).Mon().at(12, 0));
    }

    /** The expression must still be usable even though it has no cron representation. */
    @Test
    public void testUnrenderableExpressionsStillIterate() {
        CronExpression dayOfYear = builder().year().day(200).at(12, 0);
        List<LocalDateTime> list = new ArrayList<>();
        dayOfYear.consume(list::add, 1);
        assertEquals(1, list.size());
        assertEquals(200, list.get(0).getDayOfYear());
        assertEquals(year(), list.get(0).getYear());

        CronExpression weekOfYear = builder().year().week(10).Mon().at(12, 0);
        List<LocalDateTime> weeks = new ArrayList<>();
        weekOfYear.consume(weeks::add, 1);
        assertEquals(1, weeks.size());
        assertEquals(java.time.DayOfWeek.MONDAY, weeks.get(0).getDayOfWeek());
    }

    @Test
    public void testUnrenderableExpressionsStillSerialize() {
        CronExpression dayOfYear = builder().year().day(200).at(12, 0);
        CronExpression restored = CronExpression.deserialize(dayOfYear.serialize());
        assertNotNull(restored);
        assertEquals(dayOfYear.getTime(), restored.getTime());
    }

    @Test
    public void testLastWeekOfYearRendersThroughDecember() {
        // The last week of the year does have a cron shape: 1L inside December.
        String cron = builder().year().lastWeek().Mon().at(12, 0).toString();
        assertEquals("0 0 12 ? DEC MONL " + year(), cron);
    }

    // ------------------------------------------------------------------ //
    // Operations a given expression flavour does not offer               //
    // ------------------------------------------------------------------ //

    @Test
    public void testLatestWeekdayRefusesRangeOperations() {
        Month month = builder().everyMonth();
        assertUnsupported(() -> month.latestWeekday(15).toLastDay(1));
        assertUnsupported(() -> builder().everyMonth().latestWeekday(15).toLastWeekday(1));
        assertUnsupported(() -> builder().everyMonth().latestWeekday(15).toLatestWeekday(20, 1));
        assertUnsupported(() -> builder().everyMonth().latestWeekday(15).toDay(20, 1));
    }

    @Test
    public void testYearIsTheTopOfTheChain() {
        Year year = builder().year();
        assertNotNull(year.getParent());
        assertNull(year.getParent().getParent());
        assertEquals(year(), year.getYear());
        assertEquals(year(), year.getTime().getYear());
    }

    @Test
    public void testLeapYearFlag() {
        assertEquals(java.time.Year.isLeap(year()), builder().year().isLeapYear());
    }

    @Test
    public void testYearHelpers() {
        Year year = builder().year();
        assertTrue(year.getWeekCountOfYear() >= 52);
        assertEquals(java.time.Year.isLeap(year()) ? 366 : 365, year.getLastDayOfYear());
        assertEquals(java.time.Year.isLeap(year()) ? 364 : 363, year.getLastDayOfYear(2));
        int lastWeekday = year.getLastWeekdayOfYear();
        assertTrue(lastWeekday > 0 && lastWeekday <= year.getLastDayOfYear());
    }

    @Test
    public void testMonthHelpers() {
        Month month = builder().month(year(), 2);
        assertEquals(2, month.getMonth());
        assertEquals(year(), month.getYear());
        assertEquals(java.time.Year.isLeap(year()) ? 29 : 28, month.getLastDay());
        assertEquals(java.time.Year.isLeap(year()) ? 27 : 26, month.getLastDay(2));
        assertTrue(month.getLastWeekday() > 0);
        assertTrue(month.getLatestWeekday(15) > 0);
        assertTrue(month.getWeekCountOfMonth() >= 4);
    }

    private static void assertUnrenderable(CronExpression cronExpression) {
        try {
            cronExpression.toString();
            fail("expected " + cronExpression.getClass().getSimpleName()
                    + " to refuse to render itself as cron");
        } catch (UnsupportedOperationException e) {
            assertTrue(String.valueOf(e.getMessage()),
                    e.getMessage() == null || e.getMessage().contains("cron"));
        }
    }

    private static void assertUnsupported(Runnable call) {
        try {
            call.run();
            fail("expected an UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

}
