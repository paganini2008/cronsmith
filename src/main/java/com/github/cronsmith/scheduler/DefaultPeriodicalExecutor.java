package com.github.cronsmith.scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * DefaultPeriodicalExecutor implementation with ScheduledExecutorService
 * 
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public class DefaultPeriodicalExecutor implements PeriodicalExecutor {

    private final ScheduledExecutorService executorService;

    public DefaultPeriodicalExecutor(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Future<?> schedule(Runnable task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }

}
