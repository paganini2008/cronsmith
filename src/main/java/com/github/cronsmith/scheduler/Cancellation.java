package com.github.cronsmith.scheduler;

/**
 * 
 * Customizing cancellation implementation that stop cron task with specific business logic.
 * 
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
@FunctionalInterface
public interface Cancellation {

    boolean cancelTask(Runnable task, Exception reason);

}
