package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

/**
 * A fixed-period schedule: fire at an anchor, then every {@code period} thereafter — regardless of
 * calendar boundaries. Unlike a cron field (which steps within its own range, so it cannot express
 * "every 90 minutes"), this steps by a raw duration, so any interval works, e.g. {@code PT1H30M}.
 *
 * <p>
 * It stands on its own rather than joining the second-to-year expression tree, implementing only
 * what the {@link CronExpression} machinery needs: a parent that carries the start time, the current
 * position, {@link #sync(LocalDateTime)} to jump onto the grid, and the iterator that walks it.
 *
 * @Description: PeriodicCronExpression
 * @Author: Fred Feng
 * @Date: 25/08/2026
 * @Version 1.0.0
 */
public class PeriodicCronExpression implements CronExpression, Iterator<CronExpression> {

    private static final long serialVersionUID = -6427883901276544820L;

    private final CronBuilder parent;
    private final long periodMillis;
    private LocalDateTime current;
    private boolean primed;

    public PeriodicCronExpression(long periodMillis, LocalDateTime anchor) {
        if (periodMillis <= 0) {
            throw new IllegalArgumentException("Period must be positive: " + periodMillis);
        }
        if (anchor == null) {
            throw new IllegalArgumentException("Anchor is required");
        }
        this.periodMillis = periodMillis;
        this.parent = new CronBuilder().setStartTime(anchor);
        this.current = anchor;
        this.primed = true;
    }

    @Override
    public CronExpression getParent() {
        return parent;
    }

    @Override
    public LocalDateTime getTime() {
        return current;
    }

    /** Jump to the earliest grid point (anchor + k*period) at or after {@code target}. */
    @Override
    public CronExpression sync(LocalDateTime target) {
        LocalDateTime anchor = parent.getStartTime();
        long delta = anchor.until(target, ChronoUnit.MILLIS);
        long k = delta <= 0 ? 0L : (delta + periodMillis - 1) / periodMillis;
        current = anchor.plus(k * periodMillis, ChronoUnit.MILLIS);
        primed = true;
        return this;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public CronExpression next() {
        if (primed) {
            primed = false;
        } else {
            current = current.plus(periodMillis, ChronoUnit.MILLIS);
        }
        return this;
    }

    public long getPeriodMillis() {
        return periodMillis;
    }

    @Override
    public boolean supportCronString() {
        return false;
    }

    @Override
    public String toString() {
        return "every " + periodMillis + "ms";
    }

}
