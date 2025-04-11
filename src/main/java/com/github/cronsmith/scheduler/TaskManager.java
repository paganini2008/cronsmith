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

    TaskDetail saveTask(Task task, String initialParameter) throws CronTaskException;

    TaskDetail removeTask(TaskId taskId) throws CronTaskException;

    TaskDetail getTaskDetail(TaskId taskId) throws CronTaskException;

    boolean hasTask(TaskId taskId) throws CronTaskException;

    default String getInitialParameter(TaskId taskId) throws CronTaskException {
        TaskDetail taskDetail = getTaskDetail(taskId);
        return taskDetail != null ? taskDetail.getInitialParameter() : null;
    }

    default TaskStatus getTaskStatus(TaskId taskId) throws CronTaskException {
        TaskDetail taskDetail = getTaskDetail(taskId);
        return taskDetail != null ? taskDetail.getTaskStatus() : null;
    }

    int getTaskCount(String group, String name) throws CronTaskException;

    List<TaskInfoVo> findTaskInfos(String group, String name, int limit, int offset)
            throws CronTaskException;

    List<LocalDateTime> findNextFiredDateTimes(TaskId taskId, LocalDateTime startDateTime,
            LocalDateTime endDateTime) throws CronTaskException;

    List<TaskId> findUpcomingTasksBetween(LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws CronTaskException;

    LocalDateTime computeNextFiredDateTime(TaskId taskId, LocalDateTime previousFiredDateTime)
            throws CronTaskException;

    void setTaskStatus(TaskId taskId, TaskStatus status) throws CronTaskException;


}
