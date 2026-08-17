package com.github.cronsmith.test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * Shared helpers for the test suite.
 * <p>
 * A {@link com.github.cronsmith.cron.CronBuilder} refuses any year before its own start time, and
 * that start time defaults to <em>now</em> in UTC. Tests must therefore never hard-code a calendar
 * year: an expression written against 2025 stops being buildable on 1 January 2026. Everything here
 * is expressed relative to {@link #currentYear()} so the suite keeps passing as the years go by.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public abstract class CronTestSupport {

    /** The zone a default CronBuilder works in. */
    public static final ZoneId BUILDER_ZONE = ZoneId.of("UTC");

    /** The year a default CronBuilder starts from. */
    public static int currentYear() {
        return LocalDate.now(BUILDER_ZONE).getYear();
    }

    /** A year relative to the current one, e.g. {@code year(3)} is three years from now. */
    public static int year(int offset) {
        return currentYear() + offset;
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(BUILDER_ZONE).withNano(0);
    }

    /**
     * A start time far enough from any boundary that a test asserting on "the current year" cannot
     * be broken by the clock rolling over to the next year midway through the run.
     */
    public static LocalDateTime stableStartTime() {
        return LocalDate.of(currentYear(), 1, 1).atStartOfDay();
    }

}
