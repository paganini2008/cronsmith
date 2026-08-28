package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * Covers building a fixed-interval schedule from an ISO-8601 duration, both the {@link Duration}
 * form and the string form, and the durations that cannot be a fixed cron step.
 *
 * <p>
 * The interval builders anchor their start time to "now", so the tests do not assert an absolute
 * fire time. They assert two things that are deterministic regardless of when the test runs: that a
 * duration maps to the same expression as the equivalent {@code setInterval(int, TimeUnit)}, and
 * that consecutive fire times are exactly the duration apart.
 *
 * @Description: CronDurationTests
 * @Author: Fred Feng
 * @Date: 24/08/2026
 * @Version 1.0.0
 */
public class CronDurationTests {

    /**
     * The gap between the next two fire times of an interval expression, which must equal the
     * configured interval.
     */
    private static Duration step(CronExpression expression) {
        LocalDateTime t1 = expression.getNextFiredDateTime(LocalDateTime.now());
        LocalDateTime t2 = expression.getNextFiredDateTime(t1);
        return Duration.between(t1, t2);
    }

    // ---- a duration maps to the same expression as the explicit unit form ---- //

    @Test
    public void testSecondsMapToEverySecond() {
        assertEquals(CRON.setInterval(30, TimeUnit.SECONDS).toString(),
                CRON.setInterval(Duration.ofSeconds(30)).toString());
    }

    @Test
    public void testMinutesMapToEveryMinute() {
        assertEquals(CRON.setInterval(5, TimeUnit.MINUTES).toString(),
                CRON.setInterval(Duration.ofMinutes(5)).toString());
    }

    @Test
    public void testHoursMapToEveryHour() {
        assertEquals(CRON.setInterval(2, TimeUnit.HOURS).toString(),
                CRON.setInterval(Duration.ofHours(2)).toString());
    }

    @Test
    public void testDaysMapToEveryDay() {
        assertEquals(CRON.setInterval(1, TimeUnit.DAYS).toString(),
                CRON.setInterval(Duration.ofDays(1)).toString());
    }

    @Test
    public void testCoarsestUnitIsChosen() {
        // 120 minutes is exactly two hours, and 24 hours exactly one day: the coarser unit wins, so
        // both reduce to the hour/day expression rather than a minute/hour one.
        assertEquals(CRON.setInterval(2, TimeUnit.HOURS).toString(),
                CRON.setInterval(Duration.ofMinutes(120)).toString());
        assertEquals(CRON.setInterval(1, TimeUnit.DAYS).toString(),
                CRON.setInterval(Duration.ofHours(24)).toString());
    }

    // ---- consecutive fire times are exactly the interval apart ---- //

    @Test
    public void testStepEverySeconds() {
        assertEquals(Duration.ofSeconds(30), step(CRON.setInterval(Duration.ofSeconds(30))));
    }

    @Test
    public void testStepEveryMinutes() {
        assertEquals(Duration.ofMinutes(5), step(CRON.setInterval(Duration.ofMinutes(5))));
    }

    @Test
    public void testStepEveryHours() {
        assertEquals(Duration.ofHours(2), step(CRON.setInterval(Duration.ofHours(2))));
    }

    @Test
    public void testStepEveryTwoDays() {
        assertEquals(Duration.ofDays(2), step(CRON.setInterval(Duration.ofDays(2))));
    }

    @Test
    public void testStepFiftyNineSeconds() {
        assertEquals(Duration.ofSeconds(59), step(CRON.setInterval(Duration.ofSeconds(59))));
    }

    // ---- ISO-8601 string form ---- //

    @Test
    public void testStringSeconds() {
        assertEquals(CRON.setInterval(30, TimeUnit.SECONDS).toString(),
                CRON.setInterval("PT30S").toString());
    }

    @Test
    public void testStringMinutes() {
        assertEquals(Duration.ofMinutes(15), step(CRON.setInterval("PT15M")));
    }

    @Test
    public void testStringHours() {
        assertEquals(Duration.ofHours(6), step(CRON.setInterval("PT6H")));
    }

    @Test
    public void testStringDay() {
        assertEquals(Duration.ofDays(1), step(CRON.setInterval("P1D")));
    }

    @Test
    public void testStringWholeHoursGivenInSeconds() {
        // PT3600S is one hour; parsed, reduced to the hour unit, and stepping by an hour.
        assertEquals(CRON.setInterval(1, TimeUnit.HOURS).toString(),
                CRON.setInterval("PT3600S").toString());
    }

    @Test
    public void testStringIsTrimmed() {
        assertEquals(Duration.ofMinutes(5), step(CRON.setInterval("  PT5M  ")));
    }

    // ---- rejected: durations that are not a fixed cron step ---- //

    @Test
    public void testCompoundHourMinuteRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval(Duration.ofMinutes(90)); // 1h30m: not a whole hour and over 59 minutes
    
        });
    }

    @Test
    public void testCompoundStringRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval("PT1H30M");
    
        });
    }

    @Test
    public void testNinetySecondsRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval(Duration.ofSeconds(90)); // over 59 seconds, not a whole minute
    
        });
    }

    @Test
    public void testTwentyFiveHoursRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval(Duration.ofHours(25)); // not whole days, over 23 hours
    
        });
    }

    @Test
    public void testSubSecondRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval(Duration.ofMillis(500));
    
        });
    }

    @Test
    public void testZeroRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval(Duration.ZERO);
    
        });
    }

    @Test
    public void testNegativeRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval(Duration.ofSeconds(-10));
    
        });
    }

    @Test
    public void testNullDurationRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval((Duration) null);
    
        });
    }

    @Test
    public void testNullStringRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
        CRON.setInterval((String) null);
    
        });
    }

    @Test
    public void testInvalidStringRejected() {
        org.junit.jupiter.api.Assertions.assertThrows(DateTimeParseException.class, () -> {
        CRON.setInterval("not-a-duration");
    
        });
    }

}
