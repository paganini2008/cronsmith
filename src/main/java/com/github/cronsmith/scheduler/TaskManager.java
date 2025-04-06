package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 
 * @Description: TaskManager
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface TaskManager {

    TaskDetail saveTask(Task task, String initialParameter);

    TaskDetail removeTask(TaskId taskId);

    TaskDetail getTaskDetail(TaskId taskId);

    boolean hasTask(TaskId taskId);

    String getInitialParameter(TaskId taskId);

    TaskStatus getTaskStatus(TaskId taskId);

    int getTaskCount();

    List<LocalDateTime> findNextFiredDateTimes(TaskId taskId, LocalDateTime startDateTime,
            LocalDateTime endDateTime);

    List<TaskId> findUpcomingTasksBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    LocalDateTime computeNextFiredDateTime(TaskId taskId, LocalDateTime previousFiredDateTime);

    void setTaskStatus(TaskId taskId, TaskStatus status);


}
