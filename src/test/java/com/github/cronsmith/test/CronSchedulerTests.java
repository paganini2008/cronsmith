package com.github.cronsmith.test;

import static org.junit.Assert.assertTrue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.github.cronsmith.CronFuture;
import com.github.cronsmith.cron.CronBuilder;

/**
 * 
 * @Description: CronSchedulerTests
 * @Author: Fred Feng
 * @Date: 10/03/2025
 * @Version 1.0.0
 */
public class CronSchedulerTests {

    private ScheduledExecutorService scheduledExecutorService;

    @Before
    public void start() {
        scheduledExecutorService =
                Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Test
    public void testScheduler1() {
        int N = 10; // Run 10 times
        final CountDownLatch latch = new CountDownLatch(N);
        final AtomicInteger counter = new AtomicInteger();
        CronFuture future = new CronBuilder().everySecond(1).scheduler(scheduledExecutorService)
                .setDebuged(false).runTask(() -> {
                    System.out.println("Run task_" + counter.incrementAndGet());
                    latch.countDown();
                }, N);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        future.cancel(true);
        assertTrue(future.isDone() && counter.get() == N);
    }

    @Test
    public void testScheduler2() {
        int N = 10; // Run 10 times
        final CountDownLatch latch = new CountDownLatch(N);
        final AtomicInteger counter = new AtomicInteger();
        CronFuture future = new CronBuilder().everySecond(5).scheduler(scheduledExecutorService)
                .setDebuged(false).runTask(() -> {
                    System.out.println("Run task_" + counter.incrementAndGet());
                    latch.countDown();
                }, N);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        future.cancel(true);
        assertTrue(future.isDone() && counter.get() == N);
    }

    @After
    public void release() {
        scheduledExecutorService.shutdown();
    }

}
