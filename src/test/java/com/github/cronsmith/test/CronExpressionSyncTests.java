package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * {@code sync(..)} moves an expression forward to a given point in time. It used to walk there one
 * occurrence at a time, so an expression whose start time lay years back - a snapshot restored from
 * storage, typically - needed tens of millions of steps. Each level now hands the catching up to
 * its parent first.
 * <p>
 * The tests below pin down both halves of that: the result must be exactly what stepping would have
 * produced, and it must be reached quickly.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronExpressionSyncTests {

    /** How the loop used to work: step until the expression is no longer behind the target. */
    @SuppressWarnings("unchecked")
    private static LocalDateTime syncByStepping(CronExpression cronExpression,
            LocalDateTime target) {
        java.util.Iterator<CronExpression> iterator =
                (java.util.Iterator<CronExpression>) cronExpression;
        while (cronExpression.getTime().isBefore(target) && iterator.hasNext()) {
            iterator.next();
        }
        return cronExpression.getTime();
    }

    private static CronBuilder builder(LocalDateTime startTime) {
        return new CronBuilder().setStartTime(startTime);
    }

    @Test
    public void testSyncMatchesStepByStepWalkingForEverySecond() {
        assertSameAsStepping(startTime -> builder(startTime).everySecond(7));
    }

    @Test
    public void testSyncMatchesStepByStepWalkingForEveryMinute() {
        assertSameAsStepping(startTime -> builder(startTime).everyMinute(11));
    }

    @Test
    public void testSyncMatchesStepByStepWalkingForEveryHour() {
        assertSameAsStepping(startTime -> builder(startTime).everyHour(5));
    }

    @Test
    public void testSyncMatchesStepByStepWalkingForEveryDay() {
        assertSameAsStepping(startTime -> builder(startTime).everyDay(3).at(6, 15));
    }

    @Test
    public void testSyncMatchesStepByStepWalkingForFixedTimes() {
        assertSameAsStepping(startTime -> builder(startTime).everyDay().at(9, 30, 45));
    }

    @Test
    public void testSyncMatchesStepByStepWalkingForValueLists() {
        assertSameAsStepping(startTime -> builder(startTime).everyMonth().day(1).andDay(10)
                .andDay(20).hour(8).andHour(16).minute(0).andMinute(30));
    }

    @Test
    public void testSyncMatchesStepByStepWalkingForLastDayOfMonth() {
        assertSameAsStepping(startTime -> builder(startTime).everyMonth().lastDay().at(23, 0));
    }

    /**
     * Builds the same expression twice and compares the fast synchronize against a plain walk to
     * the same target, over a range of start times and targets.
     */
    private static void assertSameAsStepping(
            java.util.function.Function<LocalDateTime, CronExpression> factory) {
        LocalDateTime[] starts = {CronTestSupport.stableStartTime(),
                CronTestSupport.stableStartTime().plusDays(45).plusHours(7).plusMinutes(23)};
        long[] offsetsInMinutes = {0, 1, 59, 60, 1440, 44_640, 525_600};
        for (LocalDateTime start : starts) {
            java.util.List<LocalDateTime> targets = new java.util.ArrayList<>();
            for (long offset : offsetsInMinutes) {
                targets.add(start.plusMinutes(offset));
            }
            // The last unit of a cycle is the delicate one: a parent sitting on the final day of a
            // month has no further step of its own to give, so landing exactly there has to work.
            targets.add(start.withDayOfMonth(start.toLocalDate().lengthOfMonth()));
            targets.add(start.plusMonths(9).with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59));
            for (LocalDateTime target : targets) {
                LocalDateTime fast = factory.apply(start).sync(target).getTime();
                LocalDateTime stepped = syncByStepping(factory.apply(start), target);
                assertEquals("start=" + start + " target=" + target, stepped, fast);
            }
        }
    }

    @Test
    public void testSyncLeavesAnExpressionAlreadyAheadUntouched() {
        LocalDateTime start = CronTestSupport.stableStartTime();
        CronExpression cronExpression = builder(start).everyMinute(5);
        LocalDateTime before = cronExpression.getTime();
        assertEquals(before, cronExpression.sync(start.minusYears(1)).getTime());
    }

    @Test
    public void testSyncIsIdempotent() {
        LocalDateTime start = CronTestSupport.stableStartTime();
        LocalDateTime target = start.plusDays(200).plusHours(13);
        CronExpression cronExpression = builder(start).everySecond(30);
        LocalDateTime once = cronExpression.sync(target).getTime();
        assertEquals(once, cronExpression.sync(target).getTime());
    }

    /**
     * The regression guard for the original complaint: a per-second expression started years ago
     * used to take seconds of CPU to answer. A generous ceiling keeps this from turning into a
     * flaky benchmark while still catching a return to linear stepping, which was several orders of
     * magnitude slower.
     */
    @Test(timeout = 30_000L)
    public void testCatchingUpOnAnOldStartTimeIsFast() {
        LocalDateTime longAgo = LocalDate.now(CronTestSupport.BUILDER_ZONE).minusYears(10)
                .withDayOfMonth(1).atStartOfDay();
        LocalDateTime now = CronTestSupport.now();

        long startedAt = System.currentTimeMillis();
        LocalDateTime next = builder(longAgo).everySecond(1).getNextFiredDateTime(now);
        long elapsed = System.currentTimeMillis() - startedAt;

        assertNotNull(next);
        assertTrue(next + " is not after " + now, next.isAfter(now));
        assertTrue("catching up took " + elapsed + "ms", elapsed < 5_000L);
    }

    @Test(timeout = 30_000L)
    public void testCatchingUpIsFastForAParsedExpressionToo() {
        long startedAt = System.currentTimeMillis();
        CronExpression cronExpression = CRON.parse("*/5 * * * * ?");
        LocalDateTime target = CronTestSupport.now().plusYears(3);
        LocalDateTime next = cronExpression.getNextFiredDateTime(target);
        long elapsed = System.currentTimeMillis() - startedAt;

        assertNotNull(next);
        assertTrue(next.isAfter(target));
        assertTrue("catching up took " + elapsed + "ms", elapsed < 5_000L);
    }

    @Test
    public void testGetNextFiredDateTimeNeverReturnsThePast() {
        LocalDateTime longAgo = CronTestSupport.stableStartTime().minusYears(3);
        for (CronExpression cronExpression : new CronExpression[] {builder(longAgo).everySecond(10),
                builder(longAgo).everyMinute(15), builder(longAgo).everyHour(2),
                builder(longAgo).everyDay().at(12, 0),
                builder(longAgo).everyMonth().lastDay().at(12, 0)}) {
            LocalDateTime now = CronTestSupport.now();
            LocalDateTime next = cronExpression.getNextFiredDateTime(now);
            assertNotNull(next);
            assertTrue(next + " is not after " + now, next.isAfter(now));
        }
    }

    @Test
    public void testSyncWithoutArgumentUsesTheBuilderStartTime() {
        LocalDateTime start = CronTestSupport.stableStartTime().plusMonths(3);
        CronExpression cronExpression = builder(start).everyMinute(5);
        assertTrue(!cronExpression.sync().getTime().isBefore(start));
    }

}
