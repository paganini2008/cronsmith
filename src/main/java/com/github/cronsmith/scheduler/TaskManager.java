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

    void saveTask(Task task, String initialParameter);

    void removeTask(TaskId taskId);

    TaskDetail getTaskDetail(TaskId taskId);

    boolean hasTask(TaskId taskId);

    String getInitialParameter(TaskId taskId);

    TaskStatus getTaskStatus(TaskId taskId);

    int getTaskCount();

    List<TaskId> getLatestTaskWillRunWithin(LocalDateTime startDateTime, LocalDateTime endDateTime);

    void updateTaskWithNextFiredDateTime(TaskId taskId, LocalDateTime previousFiredDateTime);

    void pauseTask(TaskId taskId);

    void resumeTask(TaskId taskId);

    void setTaskStatus(TaskId taskId, TaskStatus status);


}
