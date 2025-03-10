package com.github.cronsmith.scheduler;

import java.util.EventListener;

/**
 * 
 * An callback interface to listening task's life cycle
 * 
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
public interface CronSchedulerListener extends EventListener {

    default void onTaskScheduled(CronScheduledEvent event) {}

    default void onTaskPaused(CronScheduledEvent event) {}

    default void onTaskResumed(CronScheduledEvent event) {}

    default void onTaskCancelled(CronScheduledEvent event) {};

    default void onTaskRemoved(CronScheduledEvent event) {};

    default void onTaskFailed(CronScheduledEvent event) {};

    default void onTaskFinished(CronScheduledEvent event) {};

}
