package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Running one or more tasks with specified cron expression and managing them
 * 
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public interface CronScheduler {

    default CronFuture runTask(final Runnable task, final int count) {
        AtomicInteger counter = new AtomicInteger(0);
        return runTask(task, (t, reason) -> {
            return counter.incrementAndGet() == count;
        });
    }

    default CronFuture runTask(final Runnable task, final LocalDateTime until) {
        return runTask(task, (t, reason) -> {
            return until.isBefore(LocalDateTime.now());
        });
    }

    CronFuture runTask(Runnable task, Cancellation cancellation);

    void pauseTask(Runnable task);

    void resumeTask(Runnable task);

    void removeTask(Runnable task);

    boolean checkExisted(Runnable task);

    int countOfTasks();

    CronScheduler subscribe(CronSchedulerListener schedulerListener);

    void unsubscribe(CronSchedulerListener schedulerListener);

    CronScheduler setDebuged(boolean debuged);

}
