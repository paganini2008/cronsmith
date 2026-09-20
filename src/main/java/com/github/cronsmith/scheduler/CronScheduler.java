/*
 * Copyright 2026 Fred Feng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        return runTask(task, (t, reason) -> until.isBefore(LocalDateTime.now()));
    }

    default CronFuture runTaskForEver(final Runnable task) {
        return runTask(task, (t, reason) -> false);
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
