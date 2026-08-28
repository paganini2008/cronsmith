package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * Behaviour across a daylight-saving switch, in both hemispheres.
 * <p>
 * A cron expression describes wall-clock time, so the schedule itself does not change when the
 * clocks do: "every day at 02:30" stays 02:30. What has to hold is that the times it produces stay
 * ordered and remain resolvable to a real instant, including on the day an hour is skipped (the
 * local time simply does not exist) and on the day an hour repeats (it exists twice).
 *
 * @Description: CronExpressionDaylightSavingTests
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
public class CronExpressionDaylightSavingTests {

    /** Zones that switch, north and south of the equator, plus one that never does. */
    private static final String[] SWITCHING_ZONES =
            {"Europe/Berlin", "Europe/London", "America/New_York", "America/Santiago",
                    "Australia/Sydney", "Pacific/Auckland"};

    private static CronExpression at(ZoneId zoneId, LocalDateTime startTime, int hour, int minute) {
        return new CronBuilder().setZoneId(zoneId).setStartTime(startTime).everyDay()
                .at(hour, minute);
    }

    private static List<LocalDateTime> fire(CronExpression cronExpression, int n) {
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.sync().consume(list::add, n);
        return list;
    }

    /** The day a zone moves its clocks forward, i.e. the day that is shorter than 24 hours. */
    private static LocalDate springForward(ZoneId zoneId, int year) {
        return findSwitch(zoneId, year, true);
    }

    /** The day a zone moves its clocks back, i.e. the day that is longer than 24 hours. */
    private static LocalDate fallBack(ZoneId zoneId, int year) {
        return findSwitch(zoneId, year, false);
    }

    private static LocalDate findSwitch(ZoneId zoneId, int year, boolean forward) {
        LocalDate date = LocalDate.of(year, 1, 1);
        for (int i = 0; i < 366; i++) {
            Duration length = Duration.between(date.atStartOfDay(zoneId),
                    date.plusDays(1).atStartOfDay(zoneId));
            if (forward && length.toHours() < 24) {
                return date;
            }
            if (!forward && length.toHours() > 24) {
                return date;
            }
            date = date.plusDays(1);
        }
        return null;
    }

    // ------------------------------------------------------------------ //
    // The schedule itself                                                //
    // ------------------------------------------------------------------ //

    @Test
    public void testEveryZoneReallyDoesSwitch() {
        int year = CronTestSupport.currentYear();
        for (String zone : SWITCHING_ZONES) {
            ZoneId zoneId = ZoneId.of(zone);
            assertNotNull(springForward(zoneId, year), zone + " never springs forward");
            assertNotNull(fallBack(zoneId, year), zone + " never falls back");
        }
    }

    @Test
    public void testWallClockTimeIsKeptAcrossASpringForward() {
        int year = CronTestSupport.currentYear();
        for (String zone : SWITCHING_ZONES) {
            ZoneId zoneId = ZoneId.of(zone);
            LocalDate skipped = springForward(zoneId, year);
            CronExpression cronExpression = at(zoneId, skipped.minusDays(2).atStartOfDay(), 3, 30);
            for (LocalDateTime ldt : fire(cronExpression, 5)) {
                assertEquals(LocalTime.of(3, 30), ldt.toLocalTime(), zone + " " + ldt);
                assertNotNull(ldt.atZone(zoneId).toInstant(), zone);
            }
        }
    }

    @Test
    public void testWallClockTimeIsKeptAcrossAFallBack() {
        int year = CronTestSupport.currentYear();
        for (String zone : SWITCHING_ZONES) {
            ZoneId zoneId = ZoneId.of(zone);
            LocalDate repeated = fallBack(zoneId, year);
            CronExpression cronExpression = at(zoneId, repeated.minusDays(2).atStartOfDay(), 1, 30);
            for (LocalDateTime ldt : fire(cronExpression, 5)) {
                assertEquals(LocalTime.of(1, 30), ldt.toLocalTime(), zone + " " + ldt);
                assertNotNull(ldt.atZone(zoneId).toInstant(), zone);
            }
        }
    }

    /**
     * A time inside the skipped hour never occurs on that one day. The schedule still lists it -
     * it is a wall-clock schedule - and resolving it to an instant is what java.time shifts
     * forward, which is the same thing every scheduler does with such a time.
     */
    @Test
    public void testATimeInsideTheSkippedHourStillResolves() {
        int year = CronTestSupport.currentYear();
        ZoneId berlin = ZoneId.of("Europe/Berlin");
        LocalDate skipped = springForward(berlin, year);
        CronExpression cronExpression = at(berlin, skipped.minusDays(1).atStartOfDay(), 2, 30);

        List<LocalDateTime> list = fire(cronExpression, 4);
        assertStrictlyIncreasing(list);
        for (LocalDateTime ldt : list) {
            assertEquals(LocalTime.of(2, 30), ldt.toLocalTime());
        }
        LocalDateTime onTheSkippedDay = skipped.atTime(2, 30);
        ZonedDateTime resolved = onTheSkippedDay.atZone(berlin);
        assertNotNull(resolved.toInstant());
        assertFalse(resolved.toLocalDateTime().equals(onTheSkippedDay), "02:30 should not exist on " + skipped);
    }

    /**
     * A time inside the repeated hour occurs twice; {@code atZone} picks the earlier of the two,
     * which is the offset still in force before the clocks go back.
     */
    @Test
    public void testATimeInsideTheRepeatedHourResolvesToTheEarlierOffset() {
        int year = CronTestSupport.currentYear();
        ZoneId berlin = ZoneId.of("Europe/Berlin");
        LocalDate repeated = fallBack(berlin, year);
        LocalDateTime ambiguous = repeated.atTime(2, 30);

        List<ZoneOffset> offsets = berlin.getRules().getValidOffsets(ambiguous);
        assertEquals(2, offsets.size(), "expected 02:30 to happen twice on " + repeated);
        assertEquals(offsets.get(0), ambiguous.atZone(berlin).getOffset());

        CronExpression cronExpression = at(berlin, repeated.minusDays(1).atStartOfDay(), 2, 30);
        assertStrictlyIncreasing(fire(cronExpression, 4));
    }

    // ------------------------------------------------------------------ //
    // Real elapsed time between two firings                              //
    // ------------------------------------------------------------------ //

    /**
     * Two consecutive daily runs are 23, 24 or 25 hours of real time apart depending on the
     * switch, even though the wall clock always shows the same time.
     */
    @Test
    public void testRealElapsedTimeShrinksAndGrowsAroundASwitch() {
        int year = CronTestSupport.currentYear();
        for (String zone : SWITCHING_ZONES) {
            ZoneId zoneId = ZoneId.of(zone);
            assertDailyGap(zoneId, springForward(zoneId, year), 23);
            assertDailyGap(zoneId, fallBack(zoneId, year), 25);
        }
    }

    private static void assertDailyGap(ZoneId zoneId, LocalDate switchDay, long expectedHours) {
        CronExpression cronExpression = at(zoneId, switchDay.minusDays(1).atStartOfDay(), 12, 0);
        List<LocalDateTime> list = fire(cronExpression, 3);
        assertStrictlyIncreasing(list);
        boolean sawTheSwitch = false;
        for (int i = 1; i < list.size(); i++) {
            long hours = Duration.between(list.get(i - 1).atZone(zoneId), list.get(i).atZone(zoneId))
                    .toHours();
            assertTrue(hours >= 23 && hours <= 25, zoneId + ": " + hours + "h between two daily runs");
            sawTheSwitch |= hours == expectedHours;
        }
        assertTrue(sawTheSwitch, zoneId + " never showed a " + expectedHours + "h gap around " + switchDay);
    }

    // ------------------------------------------------------------------ //
    // Hourly schedules, where the switch is most visible                 //
    // ------------------------------------------------------------------ //

    @Test
    public void testHourlyScheduleStaysOrderedAcrossBothSwitches() {
        int year = CronTestSupport.currentYear();
        for (String zone : SWITCHING_ZONES) {
            ZoneId zoneId = ZoneId.of(zone);
            for (LocalDate switchDay : new LocalDate[] {springForward(zoneId, year),
                    fallBack(zoneId, year)}) {
                CronExpression cronExpression = new CronBuilder().setZoneId(zoneId)
                        .setStartTime(switchDay.minusDays(1).atStartOfDay()).everyHour(1);
                List<LocalDateTime> list = fire(cronExpression, 72);
                assertStrictlyIncreasing(list);
                for (LocalDateTime ldt : list) {
                    assertEquals(0, ldt.getMinute(), zone + " " + ldt);
                    assertNotNull(ldt.atZone(zoneId).toInstant(), zone);
                }
            }
        }
    }

    @Test
    public void testZoneWithoutDaylightSavingIsUnaffected() {
        // Asia/Shanghai has had no switch since 1991, so every day is exactly 24 hours.
        ZoneId shanghai = ZoneId.of("Asia/Shanghai");
        int year = CronTestSupport.currentYear();
        assertEquals(null, springForward(shanghai, year));
        assertEquals(null, fallBack(shanghai, year));

        CronExpression cronExpression =
                at(shanghai, LocalDate.of(year, 3, 1).atStartOfDay(), 2, 30);
        List<LocalDateTime> list = fire(cronExpression, 10);
        for (int i = 1; i < list.size(); i++) {
            assertEquals(24,
                    Duration.between(list.get(i - 1).atZone(shanghai), list.get(i).atZone(shanghai))
                            .toHours());
        }
    }

    private static void assertStrictlyIncreasing(List<LocalDateTime> list) {
        assertFalse(list.isEmpty(), "nothing fired");
        for (int i = 1; i < list.size(); i++) {
            assertTrue(list.get(i).isAfter(list.get(i - 1)), "not increasing: " + list.get(i - 1) + " -> " + list.get(i));
        }
    }

}
