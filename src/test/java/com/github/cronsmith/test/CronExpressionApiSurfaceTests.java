package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDay;
import com.github.cronsmith.cron.TheDayOfWeek;
import com.github.cronsmith.cron.TheHour;
import com.github.cronsmith.cron.TheMinute;
import com.github.cronsmith.cron.TheMonth;
import com.github.cronsmith.cron.TheSecond;
import com.github.cronsmith.cron.TheWeek;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.scheduler.CronScheduledEvent;
import com.github.cronsmith.scheduler.CronScheduledEvent.EventType;
import com.github.cronsmith.scheduler.CronSchedulerListener;
import com.github.cronsmith.scheduler.DebugCronSchedulerListener;
import com.github.cronsmith.utils.ExecutorUtils;

/**
 *
 * A sweep over the corners of the public API that the scenario tests do not reach: the one-argument
 * shortcuts on the fluent interfaces, the accessors of every expression flavour, and the listener
 * and executor helpers.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronExpressionApiSurfaceTests {

    private static CronBuilder builder() {
        return new CronBuilder().setStartTime(CronTestSupport.stableStartTime());
    }

    private static List<LocalDateTime> fire(CronExpression cronExpression, int n) {
        List<LocalDateTime> list = new ArrayList<>();
        cronExpression.consume(list::add, n);
        return list;
    }

    // ------------------------------------------------------------------ //
    // One-argument shortcuts on the fluent interfaces                    //
    // ------------------------------------------------------------------ //

    @Test
    public void testSingleArgumentRangeShortcuts() {
        TheSecond second = builder().everyDay().at(0, 0).second(0).toSecond(30);
        assertEquals("0-30 0 0 * * ?", second.toString());

        TheMinute minute = builder().everyDay().hour(0).minute(0).toMinute(30);
        assertEquals("0 0-30 0 * * ?", minute.toString());

        TheHour hour = builder().everyDay().hour(0).toHour(12);
        assertEquals("0 0 0-12 * * ?", hour.at(0, 0).toString());

        TheDay day = builder().everyMonth().day(1).toDay(10);
        assertEquals("0 0 0 1-10 * ?", day.at(0, 0).toString());

        TheMonth month = builder().year().Jan().toMonth(6);
        assertEquals("0 0 0 1 JAN-JUN ? " + CronTestSupport.currentYear(),
                month.day(1).at(0, 0).toString());

        TheDayOfWeek dayOfWeek = builder().everyMonth().everyWeek().Mon().toDay(5);
        assertEquals("0 0 0 ? * MON-FRI", dayOfWeek.at(0, 0).toString());

        TheWeek week = builder().everyMonth().week(1).toWeek(3);
        assertNotNull(week.Mon().at(0, 0).toString());
    }

    @Test
    public void testDayShortcutsForTheEndOfTheMonth() {
        // An interval of 1 needs no '/' suffix.
        assertEquals("0 0 0 1 * ?",
                builder().everyMonth().day(1).toLastDay().at(0, 0).toString());
        assertEquals("0 0 0 1-LW * ?",
                builder().everyMonth().day(1).toLastWeekday(1).at(0, 0).toString());
        assertEquals("0 0 0 1,L * ?",
                builder().everyMonth().day(1).andLastDay().at(0, 0).toString());
    }

    @Test
    public void testWeekShortcuts() {
        assertNotNull(builder().everyMonth().week(1).toLastWeek().Mon().at(0, 0).toString());
        assertNotNull(builder().everyMonth().lastDayOfWeek(DayOfWeek.FRIDAY.getValue()));
        // A week of the year has no cron field of its own, so it refuses to be printed.
        assertNotNull(fire(builder().year().week(1).toLastWeek().Mon().at(0, 0), 3));
    }

    /**
     * The week nodes have a cron form of their own even though the printed expression is assembled
     * from the day-of-week below them; asking for it directly must not blow up.
     */
    @Test
    public void testWeekNodesCanRenderTheirOwnField() {
        assertNotNull(builder().everyMonth().week(1).toCronString());
        assertNotNull(builder().everyMonth().week(1).andWeek(3).toCronString());
        assertNotNull(builder().everyMonth().week(1).toWeek(3).toCronString());
        assertNotNull(builder().everyMonth().week(1).toLastWeek().toCronString());
        assertNotNull(builder().everyMonth().week(1).toLastWeek(2).toCronString());
        assertNotNull(builder().everyMonth().week(1).andLastWeek().toCronString());
        assertNotNull(builder().everyMonth().lastWeek().toCronString());
        assertNotNull(builder().year().lastWeek().toCronString());
        assertNotNull(builder().everyMonth().everyWeek(2).toCronString());
    }

    @Test
    public void testWeekdayShortcutsPickTheRightDay() {
        assertEquals("0 0 9 ? * MON", builder().everyMonth().everyWeek().Mon().at(9, 0).toString());
        assertEquals("0 0 9 ? * TUE", builder().everyMonth().everyWeek().Tues().at(9, 0).toString());
        assertEquals("0 0 9 ? * WED", builder().everyMonth().everyWeek().Wed().at(9, 0).toString());
        assertEquals("0 0 9 ? * THU", builder().everyMonth().everyWeek().Thur().at(9, 0).toString());
        assertEquals("0 0 9 ? * FRI", builder().everyMonth().everyWeek().Fri().at(9, 0).toString());
        assertEquals("0 0 9 ? * SAT", builder().everyMonth().everyWeek().Sat().at(9, 0).toString());
        assertEquals("0 0 9 ? * SUN", builder().everyMonth().everyWeek().Sun().at(9, 0).toString());
        assertEquals("0 0 9 ? * MON-FRI",
                builder().everyMonth().everyWeek().everyWeekday().at(9, 0).toString());
    }

    @Test
    public void testDayOfWeekRangesWithinAFixedWeek() {
        // A weekday range nested inside a numbered week goes through its own tag rendering.
        assertNotNull(builder().everyMonth().week(2).Mon().toFri().at(9, 0).toString());
        assertNotNull(builder().everyMonth().lastWeek().Mon().toFri().at(9, 0).toString());
        assertNotNull(builder().everyMonth().everyWeek(2).Mon().toFri().at(9, 0).toString());
    }

    // ------------------------------------------------------------------ //
    // Accessors                                                          //
    // ------------------------------------------------------------------ //

    @Test
    public void testDayAccessors() {
        Day day = builder().everyMonth().day(15);
        assertEquals(CronTestSupport.currentYear(), day.getYear());
        assertEquals(15, day.getDay());
        assertTrue(day.getMonth() >= 1 && day.getMonth() <= 12);
        assertTrue(day.getDayOfWeek() >= 1 && day.getDayOfWeek() <= 7);
        assertTrue(day.getDayOfYear() >= 1 && day.getDayOfYear() <= 366);
    }

    @Test
    public void testEveryDayOfWeekAccessors() {
        Day day = builder().everyMonth().everyWeek().everyDay(2);
        assertTrue(day.getYear() >= CronTestSupport.currentYear() - 1);
        assertTrue(day.getMonth() >= 1 && day.getMonth() <= 12);
        assertTrue(day.getDay() >= 1 && day.getDay() <= 31);
        assertTrue(day.getDayOfWeek() >= 1 && day.getDayOfWeek() <= 7);
        assertTrue(day.getDayOfYear() >= 1 && day.getDayOfYear() <= 366);
        assertNotNull(day.getTime());
        assertNotNull(day.getParent());
        assertNotNull(day.toCronString());
        assertEquals(4, fire(day.at(12, 0), 4).size());
    }

    @Test
    public void testEveryWeekAccessors() {
        Week week = builder().everyMonth().everyWeek(2);
        assertTrue(week.getYear() >= CronTestSupport.currentYear() - 1);
        assertTrue(week.getMonth() >= 1 && week.getMonth() <= 12);
        assertTrue(week.getWeek() >= 1 && week.getWeek() <= 6);
        assertTrue(week.getWeekOfYear() >= 1 && week.getWeekOfYear() <= 53);
        assertNotNull(week.getTime());
        assertNotNull(week.getParent());
        assertEquals(4, fire(week.Mon().at(12, 0), 4).size());
    }

    @Test
    public void testLastWeekAccessors() {
        Week monthWeek = builder().everyMonth().lastWeek();
        assertTrue(monthWeek.getWeek() >= 1);
        assertTrue(monthWeek.getWeekOfYear() >= 1);
        assertEquals(CronTestSupport.currentYear(), monthWeek.getYear());
        assertNotNull(monthWeek.getParent());

        Week yearWeek = builder().year().lastWeek();
        assertEquals(CronTestSupport.currentYear(), yearWeek.getYear());
        assertEquals(12, yearWeek.getMonth());
        assertTrue(yearWeek.getWeekOfYear() >= 52);
        assertNotNull(yearWeek.getParent());
        assertEquals("L", yearWeek.toCronString());
        assertNotNull(yearWeek.getTime());
        assertNotNull(yearWeek.sync(CronTestSupport.stableStartTime()));
        assertEquals(1, fire(yearWeek.everyDay(2).at(12, 0), 10).size() > 0 ? 1 : 0);
    }

    @Test
    public void testMonthAccessorsAcrossFlavours() {
        Month everyMonth = builder().everyMonth(2);
        assertTrue(everyMonth.getMonth() >= 1 && everyMonth.getMonth() <= 12);
        assertEquals(CronTestSupport.currentYear(), everyMonth.getYear());
        assertTrue(everyMonth.getLastDay() >= 28);
        assertTrue(everyMonth.getLastDay(1) >= 27);
        assertTrue(everyMonth.getLastWeekday() >= 1);
        assertTrue(everyMonth.getLatestWeekday(15) >= 1);
        assertTrue(everyMonth.getWeekCountOfMonth() >= 4);
        assertNotNull(everyMonth.getTime());
        assertNotNull(everyMonth.getParent());
    }

    @Test
    public void testLatestWeekdayFlavour() {
        TheDay latestWeekday = builder().everyMonth().latestWeekday(15);
        assertTrue(latestWeekday.getDay() >= 13 && latestWeekday.getDay() <= 17);
        assertEquals(CronTestSupport.currentYear(), latestWeekday.getYear());
        assertTrue(latestWeekday.getDayOfWeek() >= 1 && latestWeekday.getDayOfWeek() <= 5);
        assertTrue(latestWeekday.getDayOfYear() >= 1);
        assertNotNull(latestWeekday.getParent());
        assertEquals("15W", latestWeekday.toCronString());
        assertEquals("15W,20W", builder().everyMonth().latestWeekday(15).andLatestWeekday(20)
                .toCronString());
        assertEquals("15W,L", builder().everyMonth().latestWeekday(15).andLastDay().toCronString());
        assertEquals("15W,LW",
                builder().everyMonth().latestWeekday(15).andLastWeekday().toCronString());
        assertEquals("15W,20", builder().everyMonth().latestWeekday(15).andDay(20).toCronString());
    }

    @Test
    public void testDayOfYearAccessors() {
        Day dayOfYear = builder().year().day(100);
        assertEquals(CronTestSupport.currentYear(), dayOfYear.getYear());
        assertEquals(100, dayOfYear.getDayOfYear());
        assertTrue(dayOfYear.getMonth() >= 1 && dayOfYear.getMonth() <= 12);
        assertTrue(dayOfYear.getDay() >= 1 && dayOfYear.getDay() <= 31);
        assertTrue(dayOfYear.getDayOfWeek() >= 1 && dayOfYear.getDayOfWeek() <= 7);
        assertNotNull(dayOfYear.getParent());
        assertNotNull(dayOfYear.getTime());
        assertNotNull(dayOfYear.sync(CronTestSupport.stableStartTime()));
    }

    @Test
    public void testEveryHourAndMinuteAndSecondAccessors() {
        CronExpression second = builder().everySecond(5);
        assertNotNull(second.getParent());
        assertNotNull(second.getTime());
        assertNotNull(second.getZoneId());
        assertNotNull(second.getBuilder());

        com.github.cronsmith.cron.Second everySecond = builder().everySecond(5);
        assertEquals(CronTestSupport.currentYear(), everySecond.getYear());
        assertTrue(everySecond.getMonth() >= 1);
        assertTrue(everySecond.getDay() >= 1);
        assertTrue(everySecond.getHour() >= 0);
        assertTrue(everySecond.getMinute() >= 0);
        assertTrue(everySecond.getSecond() >= 0);

        com.github.cronsmith.cron.Minute everyMinute = builder().everyMinute(5);
        assertEquals(CronTestSupport.currentYear(), everyMinute.getYear());
        assertTrue(everyMinute.getMonth() >= 1);
        assertTrue(everyMinute.getDay() >= 1);
        assertTrue(everyMinute.getHour() >= 0);
        assertTrue(everyMinute.getMinute() >= 0);

        com.github.cronsmith.cron.Hour everyHour = builder().everyHour(5);
        assertEquals(CronTestSupport.currentYear(), everyHour.getYear());
        assertTrue(everyHour.getMonth() >= 1);
        assertTrue(everyHour.getDay() >= 1);
        assertTrue(everyHour.getHour() >= 0);
    }

    // ------------------------------------------------------------------ //
    // Scheduler helpers                                                  //
    // ------------------------------------------------------------------ //

    @Test
    public void testDebugListenerHandlesEveryEventType() {
        DebugCronSchedulerListener listener = new DebugCronSchedulerListener();
        Runnable task = () -> {
        };
        listener.onTaskScheduled(new CronScheduledEvent(this, task, EventType.SCHEDULED));
        listener.onTaskPaused(new CronScheduledEvent(this, task, EventType.PAUSED));
        listener.onTaskResumed(new CronScheduledEvent(this, task, EventType.RESUMED));
        listener.onTaskCancelled(new CronScheduledEvent(this, task, EventType.CANCELLED));
        listener.onTaskRemoved(new CronScheduledEvent(this, task, EventType.REMOVED));
        listener.onTaskFinished(new CronScheduledEvent(this, task, EventType.FINISHED));

        CronScheduledEvent failed = new CronScheduledEvent(this, task, EventType.FAILED);
        failed.setReason(new IllegalStateException("expected in the test output"));
        listener.onTaskFailed(failed);

        CronScheduledEvent finishedWithReason =
                new CronScheduledEvent(this, task, EventType.FINISHED);
        finishedWithReason.setReason(new IllegalStateException("expected in the test output"));
        listener.onTaskFinished(finishedWithReason);

        // A failure event without a reason must not turn into a NullPointerException.
        listener.onTaskFailed(new CronScheduledEvent(this, task, EventType.FAILED));
    }

    @Test
    public void testListenerDefaultsAreNoOps() {
        CronSchedulerListener listener = new CronSchedulerListener() {
        };
        Runnable task = () -> {
        };
        for (EventType eventType : EventType.values()) {
            CronScheduledEvent event = new CronScheduledEvent(this, task, eventType);
            listener.onTaskScheduled(event);
            listener.onTaskPaused(event);
            listener.onTaskResumed(event);
            listener.onTaskCancelled(event);
            listener.onTaskRemoved(event);
            listener.onTaskFailed(event);
            listener.onTaskFinished(event);
        }
    }

    /**
     * An executor that reports itself as still running after {@code shutdown()} sends
     * {@code gracefulShutdown} down its forceful path, which hands the work to a separate thread.
     */
    @Test
    public void testGracefulShutdownFallsBackToShutdownNow() throws Exception {
        StubbornExecutorService executor = new StubbornExecutorService();
        ExecutorUtils.gracefulShutdown(executor, 50L);
        for (int i = 0; i < 100 && !executor.shutdownNowCalled; i++) {
            Thread.sleep(20);
        }
        assertTrue("shutdownNow was never reached", executor.shutdownNowCalled);
        assertFalse(ExecutorUtils.isShutdown(executor));
    }

    /** Reports itself as never shut down, whatever it is asked to do. */
    private static class StubbornExecutorService implements ExecutorService {

        volatile boolean shutdownNowCalled;

        @Override
        public void shutdown() {}

        @Override
        public List<Runnable> shutdownNow() {
            shutdownNowCalled = true;
            return new ArrayList<>();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<?> submit(Runnable task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
                TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout,
                TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

}
