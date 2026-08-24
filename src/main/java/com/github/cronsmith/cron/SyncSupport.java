package com.github.cronsmith.cron;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 *
 * Helpers shared by the {@code sync(LocalDateTime)} implementations.
 * <p>
 * Synchronizing used to walk the iterator one tick at a time all the way from the start time to the
 * target, so a per-second expression whose start time lies a few years back needed tens of millions
 * of iterations - most visibly when a cron expression is restored from a snapshot taken long ago.
 * Every implementation now first asks its parent to catch up in one hop, which leaves at most one
 * unit worth of stepping (never more than 60 seconds, 60 minutes, 24 hours, ...) to do locally.
 *
 * @Description: SyncSupport
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
abstract class SyncSupport {

    /** Whether {@code current} still lies in an earlier minute than {@code target}. */
    static boolean behindMinute(LocalDateTime current, LocalDateTime target) {
        return current.truncatedTo(ChronoUnit.MINUTES)
                .isBefore(target.truncatedTo(ChronoUnit.MINUTES));
    }

    /** Whether {@code current} still lies in an earlier hour than {@code target}. */
    static boolean behindHour(LocalDateTime current, LocalDateTime target) {
        return current.truncatedTo(ChronoUnit.HOURS).isBefore(target.truncatedTo(ChronoUnit.HOURS));
    }

    /** Whether {@code current} still lies on an earlier day than {@code target}. */
    static boolean behindDay(LocalDateTime current, LocalDateTime target) {
        return current.toLocalDate().isBefore(target.toLocalDate());
    }

    /** Whether {@code current} still lies in an earlier month than {@code target}. */
    static boolean behindMonth(LocalDateTime current, LocalDateTime target) {
        return YearMonth.from(current).isBefore(YearMonth.from(target));
    }

    /*
     * A parent is always asked to catch up with the *beginning* of the unit the target falls in,
     * never with the target itself: a month asked to reach the 17th would run past the whole month
     * and land on the 1st of the next one, because it compares whole dates.
     */

    static LocalDateTime startOfMinute(LocalDateTime target) {
        return target.truncatedTo(ChronoUnit.MINUTES);
    }

    static LocalDateTime startOfHour(LocalDateTime target) {
        return target.truncatedTo(ChronoUnit.HOURS);
    }

    static LocalDateTime startOfDay(LocalDateTime target) {
        return target.toLocalDate().atStartOfDay();
    }

    static LocalDateTime startOfMonth(LocalDateTime target) {
        return target.toLocalDate().withDayOfMonth(1).atStartOfDay();
    }

    /**
     * Tells the parent that the value it just landed on is being taken over by its child, so the
     * parent steps on rather than handing the same value out once more.
     */
    static void takeOver(CronExpression parent) {
        if (parent instanceof PendingValueHolder) {
            ((PendingValueHolder) parent).takePendingValue();
        }
    }

}
