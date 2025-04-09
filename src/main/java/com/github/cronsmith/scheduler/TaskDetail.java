package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;

/**
 * 
 * @Description: TaskDetail
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface TaskDetail {

    ITask getTask();

    String getInitialParameter();

    TaskStatus getTaskStatus();

    LocalDateTime getNextFiredDateTime();

    LocalDateTime getPreviousFiredDateTime();

    default boolean isUnavailable() {
        return getTaskStatus() == TaskStatus.FINISHED || getTaskStatus() == TaskStatus.CANCELED
                || getTaskStatus() == TaskStatus.PAUSED;
    }

}
