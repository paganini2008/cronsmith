package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.CronDialect;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * Every expression printed in README.md, asserted here so the documentation cannot drift away from
 * what the library actually produces.
 *
 * @Description: ReadmeExamplesTests
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
public class ReadmeExamplesTests {

    /** The README pins this start time wherever it prints concrete date-times. */
    private static CronBuilder pinned() {
        return new CronBuilder().setStartTime(LocalDate.of(2027, 1, 1).atStartOfDay());
    }

    private static List<String> fire(CronExpression cronExpression, int n) {
        List<String> list = new ArrayList<>();
        cronExpression.consume(ldt -> list.add(ldt.toString()), n);
        return list;
    }

    @Test
    public void testHeadlineExample() {
        CronExpression cron =
                new CronBuilder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0);
        assertEquals("0 0 18 ? * FRIL", cron.toString());
        assertEquals("0 18 ? * FRIL *", CRON.toAwsString(cron));
        assertNotNull(cron.getNextFiredDateTime());
    }

    @Test
    public void testBuilderExamples() {
        assertEquals("*/5 * * * * ?", new CronBuilder().everySecond(5).toString());
        assertEquals("5,10-30,32/2 */5 * * * ?", new CronBuilder().everyMinute(5).second(5)
                .andSecond(10).toSecond(30).andSecond(32).toSecond(59, 2).toString());
        assertEquals("0 */5 */2 10,15,16,L * ?", new CronBuilder().everyMonth().day(10).andDay(15)
                .andDay(16).andLastDay().everyHour(2).everyMinute(5).toString());
        assertEquals("0 1-15 12 10,LW */3 ?", new CronBuilder().everyMonth(3).day(10)
                .andLastWeekday().hour(12).minute(1).toMinute(15, 1).toString());
        assertEquals("0 10 15 ? * MON-FRI",
                new CronBuilder().everyMonth().everyWeek().Mon().toFri().at(15, 10).toString());
        assertEquals("0 0 */2 ? * SAT#3", new CronBuilder().everyMonth()
                .dayOfWeek(3, DayOfWeek.SATURDAY).everyHour(2).toString());
        assertEquals("0 0 18 ? * FRIL", new CronBuilder().everyMonth()
                .lastDayOfWeek(DayOfWeek.FRIDAY.getValue()).at(18, 0).toString());
        assertEquals("0 30 23 L-3 * ?",
                new CronBuilder().everyMonth().lastDay(3).at(23, 30).toString());
        assertEquals("0 0 9 15W * ?",
                new CronBuilder().everyMonth().latestWeekday(15).at(9, 0).toString());
        assertEquals("0 10 9 ? MAR-SEP MON-FRI " + CronTestSupport.currentYear(),
                new CronBuilder().year().Mar().toSept().everyWeek().everyWeekday().at(9, 10)
                        .toString());
    }

    @Test
    public void testIterationExample() {
        assertEquals(
                java.util.Arrays.asList("2027-01-15T09:00", "2027-02-15T09:00", "2027-03-15T09:00",
                        "2027-04-15T09:00", "2027-05-14T09:00"),
                fire(pinned().everyMonth().latestWeekday(15).at(9, 0), 5));
    }

    @Test
    public void testParsingExamples() {
        assertEquals("0 0 12 ? * FRIL", CRON.parse("0 0 12 ? * FRIL").toString());
        assertEquals("0 0 12 ? * TUE#2", CRON.parse("0 0 12 ? * TUE#2").toString());
        assertEquals("0 0 12 LW * ?", CRON.parse("0 0 12 LW * ?").toString());
        assertEquals("0 15 10 ? * MON-FRI 2027-2030",
                CRON.parse("0 15 10 ? * MON-FRI 2027-2030").toString());

        assertEquals("0 */5 * * * ?", CRON.parse("*/5 * * * *").toString());
        assertEquals("0 0 9 ? * MON-FRI", CRON.parse("0 9 * * 1-5").toString());
        assertEquals("0 0 12 ? * SUN", CRON.parse("0 0 12 ? * 1").toString());
    }

    @Test
    public void testDialectExamples() {
        CronExpression daily = new CronBuilder().everyDay().at(9, 30);
        assertEquals("0 30 9 * * ?", CRON.toQuartzString(daily));
        assertEquals("0 30 9 * * ?", CRON.toSpringString(daily));
        assertEquals("30 9 * * ? *", CRON.toAwsString(daily));
        assertEquals("30 9 * * *", CRON.toUnixString(daily));
        assertEquals("30 9 * * ? *", CRON.toCronString(daily, CronDialect.AWS));
    }

    @Test
    public void testBeyondStandardCronExamples() {
        assertEquals(
                java.util.Arrays.asList("2027-07-27T12:00", "2027-11-26T12:00", "2027-11-27T12:00",
                        "2027-11-28T12:00", "2027-11-29T12:00", "2027-11-30T12:00"),
                fire(pinned().setZoneId(ZoneId.of("UTC")).year(2027).day(208).andDay(330)
                        .toLastDay().at(12, 0), 6));

        List<String> weeks = fire(pinned().setZoneId(ZoneId.of("UTC")).everyYear().week(40)
                .andWeek(45).Mon().toFri().at(12, 0), 12);
        assertEquals("2027-10-04T12:00", weeks.get(0));
        assertEquals("2027-10-08T12:00", weeks.get(4));
        assertEquals("2027-11-08T12:00", weeks.get(5));
        assertEquals("2028-10-02T12:00", weeks.get(10));

        assertEquals("0 0 9 ? * TUE#2,WED#3,FRIL",
                new CronBuilder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY)
                        .and(3, DayOfWeek.WEDNESDAY).andLastFri().at(9, 0).toString());
        assertEquals("0 0 8 ? * MON#1,MONL",
                new CronBuilder().everyMonth().week(1).andLastWeek().Mon().at(8, 0).toString());
        assertEquals("0 0 0 10,15,25W,L * ?", new CronBuilder().everyMonth().day(10).andDay(15)
                .andLatestWeekday(25).andLastDay().at(0, 0).toString());
    }

    @Test
    public void testBestPracticeExamples() {
        assertEquals(
                java.util.Arrays.asList("2027-01-29T12:00", "2027-04-30T12:00", "2027-07-30T12:00",
                        "2027-10-29T12:00", "2027-12-31T12:00"),
                fire(pinned().everyMonth().dayOfWeek(5, DayOfWeek.FRIDAY).at(12, 0), 5));

        CronExpression cron = new CronBuilder().everyDay().at(9, 0);
        assertEquals(cron.toString(),
                CronExpression.deserialize(cron.serialize()).toString());
    }

    @Test
    public void testWorkedExamples() {
        assertEquals("0 30 23 LW * ?",
                new CronBuilder().everyMonth().lastWeekday().at(23, 30).toString());
        assertEquals("0 0 6 15W,LW * ?", new CronBuilder().everyMonth().latestWeekday(15)
                .andLastWeekday().at(6, 0).toString());
        assertEquals("0 0 17 ? * TUE#2,FRIL", new CronBuilder().everyMonth()
                .dayOfWeek(2, DayOfWeek.TUESDAY).andLastFri().at(17, 0).toString());
        assertEquals("0 */15 9-18 ? * MON-FRI", new CronBuilder().everyMonth().everyWeek().Mon()
                .toFri().hour(9).toHour(18).everyMinute(15).toString());
        assertEquals("0 0 2 1 */3 ?",
                new CronBuilder().everyMonth(3).day(1).at(2, 0).toString());
        assertEquals("0 0 10 ? JUN,JUL,AUG SAT,SUN 2027-2029",
                pinned().year(2027).toYear(2029).June().andJuly().andAug().everyWeek().Sat()
                        .andSun().at(10, 0).toString());
        assertEquals("0 15 12 1 DEC ? 2027",
                CRON.atFuture(LocalDateTime.of(2027, 12, 1, 12, 15, 0)).toString());
        assertEquals("0 */5 * * * ?", CRON.setInterval(5, TimeUnit.MINUTES).toString());
        assertEquals("30 45 23 * * ?", CRON.setInterval(LocalTime.of(23, 45, 30)).toString());

        CronExpression migrated = CRON.parse("15 10 * * MON-FRI");
        assertEquals("0 15 10 ? * MON-FRI", migrated.toString());
        assertEquals("0 15 10 ? * MON-FRI", CRON.toQuartzString(migrated));
        assertEquals("15 10 ? * MON-FRI *", CRON.toAwsString(migrated));
    }

    @Test
    public void testDialectComparisonTable() {
        assertRow(new CronBuilder().everyDay().at(9, 30), "0 30 9 * * ?", "0 30 9 * * ?",
                "30 9 * * ? *", "30 9 * * *");
        assertRow(new CronBuilder().everyMonth().everyWeek().everyWeekday().at(9, 0),
                "0 0 9 ? * MON-FRI", "0 0 9 ? * MON-FRI", "0 9 ? * MON-FRI *", "0 9 * * MON-FRI");
        assertRow(new CronBuilder().everyMinute(15), "0 */15 * * * ?", "0 */15 * * * ?",
                "*/15 * * * ? *", "*/15 * * * *");
        assertRow(new CronBuilder().everyMonth().lastDay().at(23, 59), "0 59 23 L * ?",
                "0 59 23 L * ?", "59 23 L * ? *", null);
        assertRow(new CronBuilder().everyMonth().dayOfWeek(2, DayOfWeek.TUESDAY).at(10, 0),
                "0 0 10 ? * TUE#2", "0 0 10 ? * TUE#2", "0 10 ? * TUE#2 *", null);
        assertRow(new CronBuilder().everySecond(15), "*/15 * * * * ?", "*/15 * * * * ?", null,
                null);
        assertRow(new CronBuilder().everyMonth().lastDay(3).at(0, 0), "0 0 0 L-3 * ?", null, null,
                null);
    }

    /** {@code null} means the flavour cannot express the schedule, as the README table shows. */
    private static void assertRow(CronExpression cronExpression, String quartz, String spring,
            String aws, String unix) {
        assertEquals(quartz, render(cronExpression, CronDialect.QUARTZ));
        assertEquals(spring, render(cronExpression, CronDialect.SPRING));
        assertEquals(aws, render(cronExpression, CronDialect.AWS));
        assertEquals(unix, render(cronExpression, CronDialect.UNIX));
    }

    private static String render(CronExpression cronExpression, CronDialect dialect) {
        try {
            return CRON.toCronString(cronExpression, dialect);
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }

}
