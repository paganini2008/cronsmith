package com.github.cronsmith.scheduler;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled Executor or ThreadPool Implementation
 * 
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public interface PeriodicalExecutor {

    Future<?> schedule(Runnable task, long delay, TimeUnit timeUnit);

}
