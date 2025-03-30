package com.github.cronsmith.scheduler;

import java.time.temporal.TemporalUnit;
import java.util.List;

/**
 * 
 * @Description: TaskManager
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface TaskManager {

    void saveTask(Task task, String initialParameter);

    void removeTask(Task task);

    Task getTask(TaskId taskId);

    boolean hasTask(TaskId taskId);

    String getInitialParameter(TaskId taskId);

    int getTaskCount();

    List<TaskId> getLatestTaskWillRunWithin(long period, TemporalUnit timeUnit);

    void pauseTask(TaskId taskId);

    void resumeTask(TaskId taskId);

    void setTaskStatus(TaskId taskId, TaskStatus status);

    TaskStatus getTaskStatus(TaskId taskId);
}
