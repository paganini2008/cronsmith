package com.github.cronsmith;

import java.time.LocalDateTime;
import java.util.concurrent.Future;
import com.github.cronsmith.CronScheduledEvent.EventType;

/**
 * Cronable Future
 * 
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
public interface CronFuture extends Future<Object> {

    void update(Future<?> target, LocalDateTime nextFiredDateTime);

    void pause();

    void resume();

    boolean isScheduled();

    LocalDateTime getNextFiredDateTime();

    void trigger(EventType eventType, Exception reason);

}
