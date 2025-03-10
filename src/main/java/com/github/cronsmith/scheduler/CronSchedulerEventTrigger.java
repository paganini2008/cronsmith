package com.github.cronsmith.scheduler;

import com.github.cronsmith.scheduler.CronScheduledEvent.EventType;

/**
 * 
 * CronSchedulerEventTrigger
 * 
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
@FunctionalInterface
public interface CronSchedulerEventTrigger {

    void onEventTriggered(EventType eventType, Exception reason);

}
