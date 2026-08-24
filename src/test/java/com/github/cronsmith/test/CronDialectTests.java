package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.DayOfWeek;
import org.junit.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.CronDialect;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * Printing an expression for another scheduler: Quartz, Spring Scheduling, AWS EventBridge and
 * Unix crontab.
 * <p>
 * The interesting cases are the ones a target cannot express - AWS and crontab have no seconds
 * field, crontab has none of the Quartz extensions, Spring has no year - which must be reported
 * rather than turned into an expression that fires at the wrong time.
 *
 * @Description: CronDialectTests
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
public class CronDialectTests {

    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    private static int year() {
        return CronTestSupport.currentYear();
    }

    // ------------------------------------------------------------------ //
    // Schedules every flavour can express                                //
    // ------------------------------------------------------------------ //

    @Test
    public void testDailySchedule() {
        CronExpression daily = builder().everyDay().at(9, 30);
        assertEquals("0 30 9 * * ?", CRON.toCronString(daily, CronDialect.CRONSMITH));
        assertEquals("0 30 9 * * ?", CRON.toQuartzString(daily));
        assertEquals("0 30 9 * * ?", CRON.toSpringString(daily));
        assertEquals("30 9 * * ? *", CRON.toAwsString(daily));
        assertEquals("30 9 * * *", CRON.toUnixString(daily));
    }

    @Test
    public void testWeekdaySchedule() {
        CronExpression weekdays = builder().everyMonth().everyWeek().everyWeekday().at(9, 0);
        assertEquals("0 0 9 ? * MON-FRI", CRON.toQuartzString(weekdays));
        assertEquals("0 0 9 ? * MON-FRI", CRON.toSpringString(weekdays));
        assertEquals("0 9 ? * MON-FRI *", CRON.toAwsString(weekdays));
        assertEquals("0 9 * * MON-FRI", CRON.toUnixString(weekdays));
    }

    @Test
    public void testEveryFifteenMinutes() {
        CronExpression quarterly = builder().everyMinute(15);
        assertEquals("0 */15 * * * ?", CRON.toQuartzString(quarterly));
        assertEquals("0 */15 * * * ?", CRON.toSpringString(quarterly));
        assertEquals("*/15 * * * ? *", CRON.toAwsString(quarterly));
        assertEquals("*/15 * * * *", CRON.toUnixString(quarterly));
    }

    @Test
    public void testDayOfMonthList() {
        CronExpression monthly = builder().everyMonth().day(1).andDay(15).at(2, 0);
        assertEquals("0 0 2 1,15 * ?", CRON.toQuartzString(monthly));
        assertEquals("0 2 1,15 * ? *", CRON.toAwsString(monthly));
        assertEquals("0 2 1,15 * *", CRON.toUnixString(monthly));
    }

    // ------------------------------------------------------------------ //
    // Schedules only some flavours can express                           //
    // ------------------------------------------------------------------ //

    @Test
    public void testSecondsAreQuartzAndSpringOnly() {
        CronExpression everyFifteenSeconds = builder().everySecond(15);
        assertEquals("*/15 * * * * ?", CRON.toQuartzString(everyFifteenSeconds));
        assertEquals("*/15 * * * * ?", CRON.toSpringString(everyFifteenSeconds));
        assertRefused(CronDialect.AWS, everyFifteenSeconds, "seconds");
        assertRefused(CronDialect.UNIX, everyFifteenSeconds, "seconds");
    }

    @Test
    public void testLastDayOfMonthIsNotACrontabFeature() {
        CronExpression lastDay = builder().everyMonth().lastDay().at(23, 59);
        assertEquals("0 59 23 L * ?", CRON.toQuartzString(lastDay));
        assertEquals("0 59 23 L * ?", CRON.toSpringString(lastDay));
        assertEquals("59 23 L * ? *", CRON.toAwsString(lastDay));
        assertRefused(CronDialect.UNIX, lastDay, "'L' or 'W'");
    }

    @Test
    public void testNthWeekdayIsNotACrontabFeature() {
        CronExpression secondTuesday =
                builder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY).at(10, 0);
        assertEquals("0 0 10 ? * TUE#2", CRON.toQuartzString(secondTuesday));
        assertEquals("0 0 10 ? * TUE#2", CRON.toSpringString(secondTuesday));
        assertEquals("0 10 ? * TUE#2 *", CRON.toAwsString(secondTuesday));
        assertRefused(CronDialect.UNIX, secondTuesday, "'L' or '#'");
    }

    @Test
    public void testLastWeekdayOfMonth() {
        CronExpression lastFriday =
                builder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);
        assertEquals("0 0 18 ? * FRIL", CRON.toQuartzString(lastFriday));
        assertEquals("0 18 ? * FRIL *", CRON.toAwsString(lastFriday));
        assertRefused(CronDialect.UNIX, lastFriday, "'L' or '#'");
    }

    @Test
    public void testOffsetFromTheLastDayIsQuartzOnly() {
        CronExpression thirdFromLast = builder().everyMonth().lastDay(3).at(0, 0);
        assertEquals("0 0 0 L-3 * ?", CRON.toQuartzString(thirdFromLast));
        assertRefused(CronDialect.SPRING, thirdFromLast, "L-");
        assertRefused(CronDialect.AWS, thirdFromLast, "L-");
        assertRefused(CronDialect.UNIX, thirdFromLast, "'L' or 'W'");
    }

    @Test
    public void testYearRestrictionIsQuartzAndAwsOnly() {
        CronExpression bounded = builder().year().toYear(year() + 2).Jan().day(1).at(0, 0);
        assertEquals("0 0 0 1 JAN ? " + year() + "-" + (year() + 2), CRON.toQuartzString(bounded));
        assertEquals("0 0 1 JAN ? " + year() + "-" + (year() + 2), CRON.toAwsString(bounded));
        assertRefused(CronDialect.SPRING, bounded, "year");
        assertRefused(CronDialect.UNIX, bounded, "year");
    }

    // ------------------------------------------------------------------ //
    // The rendering contract itself                                      //
    // ------------------------------------------------------------------ //

    @Test
    public void testFieldCounts() {
        CronExpression daily = builder().everyDay().at(9, 30);
        assertEquals(6, CRON.toQuartzString(daily).split(" ").length);
        assertEquals(6, CRON.toSpringString(daily).split(" ").length);
        assertEquals(6, CRON.toAwsString(daily).split(" ").length);
        assertEquals(5, CRON.toUnixString(daily).split(" ").length);
    }

    @Test
    public void testExactlyOneDayFieldIsIgnoredForQuartzAndAws() {
        // Quartz and EventBridge both insist that one of the two day fields is '?'.
        for (CronExpression cronExpression : new CronExpression[] {builder().everyDay().at(9, 30),
                builder().everyMonth().everyWeek().Mon().at(9, 30),
                builder().everyMonth().lastDay().at(9, 30)}) {
            for (String cron : new String[] {CRON.toQuartzString(cronExpression),
                    CRON.toAwsString(cronExpression)}) {
                String[] fields = cron.split(" ");
                int offset = fields.length == 6 && !cron.startsWith("0 ") ? -1 : 0;
                String dayOfMonth = fields[3 + offset];
                String dayOfWeek = fields[5 + offset];
                assertEquals(cron, 1,
                        ("?".equals(dayOfMonth) ? 1 : 0) + ("?".equals(dayOfWeek) ? 1 : 0));
            }
        }
    }

    @Test
    public void testEveryDialectRendersEveryPlainSchedule() {
        CronExpression plain = builder().everyDay().at(0, 0);
        for (CronDialect dialect : CronDialect.values()) {
            assertTrue(dialect.name(), CRON.toCronString(plain, dialect).length() > 0);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenderingRejectsSomethingThatIsNotACronsmithExpression() {
        CronDialect.QUARTZ.render("not a cron expression");
    }

    // ------------------------------------------------------------------ //
    // The field array every dialect is rendered from                     //
    // ------------------------------------------------------------------ //

    @Test
    public void testCronFieldsAreRenderedInCanonicalOrder() {
        String[] fields = CRON.toCronFields(builder().everyMonth().everyWeek().Mon().at(9, 30));
        assertEquals(7, fields.length);
        assertEquals("0", fields[0]);
        assertEquals("30", fields[1]);
        assertEquals("9", fields[2]);
        assertEquals("?", fields[3]);
        assertEquals("*", fields[4]);
        assertEquals("MON", fields[5]);
        assertNull("no year restriction", fields[6]);
    }

    @Test
    public void testCronFieldsCarryTheYearWhenThereIsOne() {
        String[] fields =
                CRON.toCronFields(builder().year().toYear(year() + 2).Jan().day(1).at(0, 0));
        assertEquals(year() + "-" + (year() + 2), fields[6]);
        assertEquals("1", fields[3]);
        assertEquals("?", fields[5]);
    }

    /** The fields are what the flavours are built from, so both routes have to agree. */
    @Test
    public void testRenderingFromFieldsMatchesRenderingFromTheString() {
        for (CronExpression cronExpression : new CronExpression[] {builder().everyDay().at(9, 30),
                builder().everyMonth().everyWeek().everyWeekday().at(9, 0),
                builder().everyMonth().lastDay().at(23, 59),
                builder().year().toYear(year() + 2).Jan().day(1).at(0, 0)}) {
            String canonical = cronExpression.toString();
            for (CronDialect dialect : CronDialect.values()) {
                String fromFields;
                try {
                    fromFields = CRON.toCronString(cronExpression, dialect);
                } catch (UnsupportedOperationException e) {
                    try {
                        dialect.render(canonical);
                        fail(dialect + " accepted " + canonical + " through the string route");
                    } catch (UnsupportedOperationException expected) {
                        continue;
                    }
                    continue;
                }
                assertEquals(dialect + " " + canonical, dialect.render(canonical), fromFields);
            }
        }
    }

    @Test
    public void testCronFieldsAreAFreshArrayEveryTime() {
        CronExpression cronExpression = builder().everyDay().at(9, 30);
        String[] first = CRON.toCronFields(cronExpression);
        first[0] = "tampered";
        assertEquals("0", CRON.toCronFields(cronExpression)[0]);
        assertEquals("0 30 9 * * ?", cronExpression.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCronFieldsRefuseASchedulePlainCronCannotHold() {
        CRON.toCronFields(builder().year().day(200).at(12, 0));
    }

    private static void assertRefused(CronDialect dialect, CronExpression cronExpression,
            String expectedInMessage) {
        try {
            CRON.toCronString(cronExpression, dialect);
            fail(dialect + " should not be able to express " + cronExpression);
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(expectedInMessage));
        }
    }

}
