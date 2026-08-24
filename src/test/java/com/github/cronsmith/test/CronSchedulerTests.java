package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.scheduler.CronFuture;
import com.github.cronsmith.scheduler.CronScheduledEvent;
import com.github.cronsmith.scheduler.CronScheduledEvent.EventType;
import com.github.cronsmith.scheduler.CronScheduler;
import com.github.cronsmith.scheduler.CronSchedulerImpl;
import com.github.cronsmith.scheduler.CronSchedulerListener;
import com.github.cronsmith.scheduler.DefaultPeriodicalExecutor;
import com.github.cronsmith.scheduler.PeriodicalExecutor;
import com.github.cronsmith.utils.ExecutorUtils;
import com.github.cronsmith.utils.StringUtils;
/**
 *
 * Tests around running real tasks off a cron expression: repetition counts, the task life cycle
 * events, pausing/resuming/removing and cancellation.
 * <p>
 * Every wait is bounded - the original suite awaited a latch of {@code Integer.MAX_VALUE} counts,
 * which could never be reached and simply hung the build.
 *
 * @Description: CronSchedulerTests
 * @Author: Fred Feng
 * @Date: 10/03/2025
 * @Version 1.0.0
 */
public class CronSchedulerTests {

    /** Nothing here should need anywhere near this long; it only keeps a failure from hanging. */
    private static final int TIMEOUT_SECONDS = 60;

    private ScheduledExecutorService scheduledExecutorService;

    @Before
    public void start() {
        scheduledExecutorService =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @After
    public void release() {
        scheduledExecutorService.shutdownNow();
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue("timed out waiting for the task to run",
                latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    @Test
    public void testScheduler1() throws InterruptedException {
        int n = 3;
        final CountDownLatch latch = new CountDownLatch(n);
        final AtomicInteger counter = new AtomicInteger();
        CronFuture future = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService)
                .setDebuged(false).runTask(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                }, n);
        await(latch);
        future.cancel(true);
        assertTrue(future.isDone());
        assertEquals(n, counter.get());
    }

    @Test
    public void testScheduler2() throws InterruptedException {
        int n = 2;
        final CountDownLatch latch = new CountDownLatch(n);
        final AtomicInteger counter = new AtomicInteger();
        CronFuture future = new CronBuilder().everySecond(3).scheduler(scheduledExecutorService)
                .setDebuged(false).runTask(() -> {
                    counter.incrementAndGet();
                    latch.countDown();
                }, n);
        await(latch);
        future.cancel(true);
        assertTrue(future.isDone());
        assertEquals(n, counter.get());
    }

    @Test
    public void testDebugListenerPrintsWithoutFailing() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        CronFuture future = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService)
                .setDebuged(true).runTask(latch::countDown, 1);
        await(latch);
        future.cancel(true);
    }

    @Test
    public void testLifeCycleEventsAreDelivered() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        final List<EventType> events = new CopyOnWriteArrayList<>();
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        scheduler.subscribe(new RecordingListener(events));
        Runnable task = latch::countDown;
        CronFuture future = scheduler.runTask(task, 2);
        await(latch);
        // The very last run removes the task, which is what closes the cancellation loop.
        for (int i = 0; i < 50 && !events.contains(EventType.REMOVED); i++) {
            Thread.sleep(100);
        }
        future.cancel(true);
        assertTrue(events.toString(), events.contains(EventType.SCHEDULED));
        assertTrue(events.toString(), events.contains(EventType.FINISHED));
        assertTrue(events.toString(), events.contains(EventType.REMOVED));
    }

    @Test
    public void testFailingTaskReportsTheReason() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final List<CronScheduledEvent> failures = new CopyOnWriteArrayList<>();
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        scheduler.subscribe(new CronSchedulerListener() {
            @Override
            public void onTaskFailed(CronScheduledEvent event) {
                failures.add(event);
                latch.countDown();
            }
        });
        CronFuture future = scheduler.runTask(() -> {
            throw new IllegalStateException("boom");
        }, 2);
        await(latch);
        future.cancel(true);
        assertFalse(failures.isEmpty());
        assertEquals("boom", failures.get(0).getReason().getMessage());
        assertEquals(EventType.FAILED, failures.get(0).getEventType());
        assertNotNull(failures.get(0).toString());
    }

    @Test
    public void testPauseAndResume() throws InterruptedException {
        final CountDownLatch scheduled = new CountDownLatch(1);
        final List<EventType> events = new CopyOnWriteArrayList<>();
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        scheduler.subscribe(new RecordingListener(events));
        Runnable task = scheduled::countDown;
        CronFuture future = scheduler.runTask(task, 100);
        await(scheduled);

        assertTrue(scheduler.checkExisted(task));
        assertEquals(1, scheduler.countOfTasks());

        scheduler.pauseTask(task);
        assertFalse(future.isScheduled());
        scheduler.resumeTask(task);
        assertTrue(future.isScheduled());

        scheduler.removeTask(task);
        assertFalse(scheduler.checkExisted(task));
        assertEquals(0, scheduler.countOfTasks());
        assertTrue(events.toString(), events.contains(EventType.PAUSED));
        assertTrue(events.toString(), events.contains(EventType.RESUMED));
        assertTrue(events.toString(), events.contains(EventType.REMOVED));
    }

    /**
     * A schedule whose start time lies in the past starts at its next due run; the occurrences that
     * have already gone by are not replayed.
     */
    @Test
    public void testAPastStartTimeIsNotReplayed() throws InterruptedException {
        LocalDateTime longAgo = LocalDate.now().minusYears(5).withDayOfMonth(1).atStartOfDay();
        final List<LocalDateTime> ranAt = new CopyOnWriteArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        CronExpression cronExpression = new CronBuilder().setStartTime(longAgo).everySecond(2);
        LocalDateTime before = LocalDateTime.now(cronExpression.getZoneId());
        CronFuture future = cronExpression.scheduler(scheduledExecutorService).runTask(() -> {
            ranAt.add(LocalDateTime.now(cronExpression.getZoneId()));
            latch.countDown();
        }, 1);
        await(latch);
        future.cancel(true);

        assertEquals("the backlog must not be replayed", 1, ranAt.size());
        assertTrue(ranAt.get(0) + " is not at or after " + before,
                !ranAt.get(0).isBefore(before.minusSeconds(1)));
    }

    /**
     * Getting there must not cost one iteration per elapsed second either, so submitting the task
     * has to return promptly however far back the start time lies.
     */
    @Test(timeout = 30_000L)
    public void testSchedulingAPastStartTimeIsPrompt() {
        LocalDateTime longAgo = LocalDate.now().minusYears(5).withDayOfMonth(1).atStartOfDay();
        long startedAt = System.currentTimeMillis();
        CronFuture future = new CronBuilder().setStartTime(longAgo).everySecond(1)
                .scheduler(scheduledExecutorService).runTask(() -> {
                }, 1);
        long elapsed = System.currentTimeMillis() - startedAt;
        if (future != null) {
            future.cancel(true);
        }
        assertTrue("submitting took " + elapsed + "ms", elapsed < 5_000L);
    }

    @Test
    public void testUnknownTaskIsIgnoredByTheLifeCycleCalls() {
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        Runnable unknown = () -> {
        };
        scheduler.pauseTask(unknown);
        scheduler.resumeTask(unknown);
        scheduler.removeTask(unknown);
        assertFalse(scheduler.checkExisted(unknown));
        assertEquals(0, scheduler.countOfTasks());
    }

    @Test(expected = IllegalStateException.class)
    public void testSchedulingTheSameTaskTwiceIsRejected() {
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        Runnable task = () -> {
        };
        scheduler.runTask(task, 10);
        scheduler.runTask(task, 10);
    }

    @Test
    public void testRunTaskUntilAGivenPointInTime() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        CronFuture future = scheduler.runTask(latch::countDown, LocalDateTime.now().plusSeconds(2));
        await(latch);
        future.cancel(true);
        assertNotNull(future);
    }

    @Test
    public void testRunTaskForEver() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        CronFuture future = scheduler.runTaskForEver(latch::countDown);
        await(latch);
        assertTrue(scheduler.countOfTasks() > 0);
        future.cancel(true);
        assertTrue(future.isCancelled());
    }

    @Test
    public void testSubscribeIsIdempotentAndUnsubscribeWorks() {
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        CronSchedulerListener listener = new CronSchedulerListener() {
        };
        assertNotNull(scheduler.subscribe(listener));
        assertNotNull(scheduler.subscribe(listener));
        scheduler.unsubscribe(listener);
        // Null listeners must simply be ignored rather than blow up.
        scheduler.subscribe(null);
        scheduler.unsubscribe(null);
        assertNotNull(scheduler.setDebuged(true));
        assertNotNull(scheduler.setDebuged(false));
    }

    @Test
    public void testCronFutureDelegatesToTheUnderlyingFuture() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        CronScheduler scheduler = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService);
        CronFuture future = scheduler.runTask(latch::countDown, 1);
        assertFalse(future.isCancelled());
        await(latch);
        for (int i = 0; i < 50 && future.getNextFiredDateTime() == null; i++) {
            Thread.sleep(100);
        }
        future.cancel(true);
        assertTrue(future.isDone());
    }

    @Test
    public void testCustomPeriodicalExecutorIsUsed() throws InterruptedException {
        final AtomicInteger scheduleCalls = new AtomicInteger();
        PeriodicalExecutor executor = new PeriodicalExecutor() {
            private final PeriodicalExecutor delegate =
                    new DefaultPeriodicalExecutor(scheduledExecutorService);

            @Override
            public Future<?> schedule(Runnable task, long delay, TimeUnit timeUnit) {
                scheduleCalls.incrementAndGet();
                return delegate.schedule(task, delay, timeUnit);
            }
        };
        final CountDownLatch latch = new CountDownLatch(1);
        CronExpression cronExpression = new CronBuilder().everySecond(1);
        CronFuture future = new CronSchedulerImpl(cronExpression, executor).runTask(latch::countDown, 1);
        await(latch);
        future.cancel(true);
        assertTrue(scheduleCalls.get() > 0);
    }

    @Test
    public void testExecutorUtils() {
        ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
        assertFalse(ExecutorUtils.isShutdown(es));
        assertFalse(ExecutorUtils.isShutdown(Runnable::run));
        ExecutorUtils.gracefulShutdown(Runnable::run, 100L);
        ExecutorUtils.gracefulShutdown(es, 100L);
        assertTrue(ExecutorUtils.isShutdown(es));
        // Shutting an already terminated pool down again must be a no-op.
        ExecutorUtils.gracefulShutdown(es, 100L);
    }

    @Test
    public void testStringUtils() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank(" x "));
        assertTrue(StringUtils.isNotBlank(" x "));
    }

    @Test
    public void testScheduledEventCarriesItsPayload() {
        Runnable task = () -> {
        };
        CronScheduledEvent event = new CronScheduledEvent(this, task, EventType.FINISHED);
        assertEquals(task, event.getTask());
        assertEquals(EventType.FINISHED, event.getEventType());
        assertTrue(event.getTimestamp() > 0);
        assertTrue(event.toString().contains("Ended: true"));

        LocalDateTime next = LocalDateTime.now().plusMinutes(1);
        event.setNextFiredDateTime(next);
        assertEquals(next, event.getNextFiredDateTime());
        assertTrue(event.toString().contains("NextFired: "));
        assertEquals(Collections.emptyList(), Collections.emptyList());
    }

    /** Records every life cycle callback so a test can assert on the sequence afterwards. */
    private static class RecordingListener implements CronSchedulerListener {

        private final List<EventType> events;

        RecordingListener(List<EventType> events) {
            this.events = events;
        }

        @Override
        public void onTaskScheduled(CronScheduledEvent event) {
            events.add(EventType.SCHEDULED);
        }

        @Override
        public void onTaskFinished(CronScheduledEvent event) {
            events.add(EventType.FINISHED);
        }

        @Override
        public void onTaskPaused(CronScheduledEvent event) {
            events.add(EventType.PAUSED);
        }

        @Override
        public void onTaskResumed(CronScheduledEvent event) {
            events.add(EventType.RESUMED);
        }

        @Override
        public void onTaskCancelled(CronScheduledEvent event) {
            events.add(EventType.CANCELLED);
        }

        @Override
        public void onTaskRemoved(CronScheduledEvent event) {
            events.add(EventType.REMOVED);
        }

        @Override
        public void onTaskFailed(CronScheduledEvent event) {
            events.add(EventType.FAILED);
        }
    }

}
