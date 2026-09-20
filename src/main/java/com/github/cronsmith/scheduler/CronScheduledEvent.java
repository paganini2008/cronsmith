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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EventObject;

/**
 * 
 * CronScheduledEvent will be triggered by CronScheduler while running task
 * 
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
public class CronScheduledEvent extends EventObject {

    private static final long serialVersionUID = 1551880421717089697L;

    public CronScheduledEvent(Object source, Runnable task, EventType eventType) {
        super(source);
        this.task = task;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    private final Runnable task;
    private final long timestamp;
    private final EventType eventType;

    private Exception reason;
    private LocalDateTime nextFiredDateTime;

    public Runnable getTask() {
        return task;
    }

    public EventType getEventType() {
        return eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setReason(Exception reason) {
        this.reason = reason;
    }

    public Exception getReason() {
        return reason;
    }

    public LocalDateTime getNextFiredDateTime() {
        return nextFiredDateTime;
    }

    public void setNextFiredDateTime(LocalDateTime nextFiredDateTime) {
        this.nextFiredDateTime = nextFiredDateTime;
    }

    @Override
    public String toString() {
        String str = "Task: " + task + ", EventType: " + eventType + ", Happened: " + Instant
                .ofEpochMilli(getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (eventType == EventType.FINISHED) {
            if (nextFiredDateTime != null) {
                str += ", NextFired: " + nextFiredDateTime;
            } else {
                str += ", Ended: true";
            }
        }
        return str;
    }

    /**
     * 
     * @Description: EventType
     * @Author: Fred Feng
     * @Date: 09/03/2025
     * @Version 1.0.0
     */
    public static enum EventType {

        SCHEDULED, FINISHED, PAUSED, RESUMED, CANCELLED, REMOVED, FAILED;

    }

}
