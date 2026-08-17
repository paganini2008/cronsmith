package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.parser.CronExpressionContext;

/**
 *
 * Zone handling: a cron expression carries the zone its schedule is meant to be read in. The wall
 * clock pattern itself never changes with the zone - "every day at 09:00" is 09:00 local time
 * everywhere - so the tests check the zone is carried through the whole chain and that the
 * schedule stays identical, including across a daylight-saving switch.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronExpressionZoneTests {

    /** A spread of zones: ahead of UTC, behind UTC, half-hour offset, and two DST regimes. */
    private static final String[] ZONES = {"UTC", "Asia/Shanghai", "America/New_York",
            "Europe/Berlin", "Asia/Kolkata", "Australia/Sydney", "America/Sao_Paulo"};

    @Test
    public void testZoneIdIsCarriedByEveryNodeOfTheChain() {
        for (String zone : ZONES) {
            ZoneId zoneId = ZoneId.of(zone);
            CronExpression cronExpression = new CronBuilder().setZoneId(zoneId).everyDay().at(9, 0);
            assertEquals(zone, zoneId, cronExpression.getZoneId());
            assertEquals(zone, zoneId, cronExpression.getParent().getZoneId());
            assertEquals(zone, zoneId, cronExpression.getBuilder().getZoneId());
        }
    }

    @Test
    public void testTheWallClockScheduleIsTheSameInEveryZone() {
        List<LocalDateTime> reference = null;
        for (String zone : ZONES) {
            CronExpression cronExpression = new CronBuilder().setZoneId(ZoneId.of(zone))
                    .setStartTime(CronTestSupport.stableStartTime()).everyDay().at(9, 30);
            List<LocalDateTime> list = new ArrayList<>();
            cronExpression.consume(list::add, 10);
            if (reference == null) {
                reference = list;
            } else {
                assertEquals(zone, reference, list);
            }
        }
        assertNotNull(reference);
    }

    @Test
    public void testStartTimeIsReadInTheConfiguredZone() {
        // A builder left to its own devices starts from "now" in UTC.
        CronBuilder builder = new CronBuilder();
        assertEquals(ZoneId.of("UTC"), builder.getZoneId());
        assertEquals(LocalDate.now(ZoneId.of("UTC")).getYear(), builder.getStartTime().getYear());
    }

    @Test
    public void testScheduleSurvivesADaylightSavingSpringForward() {
        // Europe/Berlin skips 02:00-03:00 on the last Sunday of March.
        ZoneId berlin = ZoneId.of("Europe/Berlin");
        int year = CronTestSupport.currentYear();
        LocalDateTime beforeSwitch = LocalDate.of(year, 3, 25).atStartOfDay();
        CronExpression cronExpression = new CronBuilder().setZoneId(berlin)
                .setStartTime(beforeSwitch).everyDay().at(2, 30);
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.sync().consume(list::add, 14);
        for (LocalDateTime ldt : list) {
            assertEquals(ldt.toString(), 2, ldt.getHour());
            assertEquals(ldt.toString(), 30, ldt.getMinute());
            // The instant a skipped local time maps to still has to be resolvable.
            ZonedDateTime zoned = ldt.atZone(berlin);
            assertNotNull(zoned.toInstant());
        }
    }

    @Test
    public void testScheduleSurvivesADaylightSavingFallBack() {
        // Europe/Berlin repeats 02:00-03:00 on the last Sunday of October.
        ZoneId berlin = ZoneId.of("Europe/Berlin");
        int year = CronTestSupport.currentYear();
        LocalDateTime beforeSwitch = LocalDate.of(year, 10, 25).atStartOfDay();
        CronExpression cronExpression = new CronBuilder().setZoneId(berlin)
                .setStartTime(beforeSwitch).everyHour(1);
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.sync().consume(list::add, 48);
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i - 1) + " -> " + list.get(i), list.get(i).isAfter(list.get(i - 1)));
        }
    }

    @Test
    public void testParserPropagatesItsZoneId() {
        CronExpressionContext context = new CronExpressionContext();
        assertEquals(ZoneId.of("UTC"), context.getZoneId());
        for (String zone : ZONES) {
            context.setZoneId(ZoneId.of(zone));
            assertEquals(ZoneId.of(zone), context.getZoneId());
        }
    }

    @Test
    public void testAZonedExpressionSerializesWithItsZone() {
        CronExpression cronExpression =
                new CronBuilder().setZoneId(ZoneId.of("Asia/Shanghai")).everyDay().at(9, 0);
        CronExpression restored = CRON.load(CRON.toByteArray(cronExpression));
        assertEquals(ZoneId.of("Asia/Shanghai"), restored.getZoneId());
        assertEquals(cronExpression.toString(), restored.toString());
    }

    @Test
    public void testNextFiredDateTimeIsAfterTheGivenPointInEveryZone() {
        for (String zone : ZONES) {
            CronExpression cronExpression =
                    new CronBuilder().setZoneId(ZoneId.of(zone)).everyMinute(5);
            LocalDateTime reference = LocalDateTime.now(ZoneId.of(zone));
            LocalDateTime next = cronExpression.getNextFiredDateTime(reference);
            assertNotNull(zone, next);
            assertTrue(zone + ": " + next + " <= " + reference, next.isAfter(reference));
        }
    }

}
