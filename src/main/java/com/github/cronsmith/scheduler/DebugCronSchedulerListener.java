package com.github.cronsmith.scheduler;

/**
 * 
 * @Description: DebugCronSchedulerListener
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
public class DebugCronSchedulerListener implements CronSchedulerListener {

    @Override
    public void onTaskScheduled(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskPaused(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskResumed(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskCancelled(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskRemoved(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskFailed(CronScheduledEvent event) {
        System.err.println(event);
        event.getReason().printStackTrace();
    }

    @Override
    public void onTaskFinished(CronScheduledEvent event) {
        if (event.getReason() != null) {
            System.err.println(event);
            event.getReason().printStackTrace();
        } else {
            System.out.println(event);
        }
    }

}
