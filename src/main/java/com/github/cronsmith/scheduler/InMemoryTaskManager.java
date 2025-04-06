package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: InMemoryTaskManager
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class InMemoryTaskManager implements TaskManager {

    private final Map<TaskId, SimpleTaskDetail> taskStore = new ConcurrentHashMap<>();

    private static class SimpleTaskDetail implements TaskDetail {

        private final Task task;
        private final String initialParameter;
        private TaskStatus taskStatus;
        private LocalDateTime prevousFiredDateTime;
        private LocalDateTime nextFiredDateTime;

        SimpleTaskDetail(Task task, String initialParameter, TaskStatus taskStatus) {
            this.task = task;
            this.initialParameter = initialParameter;
            this.taskStatus = taskStatus;
        }

        public Task getTask() {
            return task;
        }

        public String getInitialParameter() {
            return initialParameter;
        }

        public TaskStatus getTaskStatus() {
            return taskStatus;
        }

        public void setTaskStatus(TaskStatus taskStatus) {
            this.taskStatus = taskStatus;
        }

        public void setPrevousFiredDateTime(LocalDateTime prevousFiredDateTime) {
            this.prevousFiredDateTime = prevousFiredDateTime;
        }

        public LocalDateTime getNextFiredDateTime() {
            return nextFiredDateTime;
        }

        public void setNextFiredDateTime(LocalDateTime nextFiredDateTime) {
            this.nextFiredDateTime = nextFiredDateTime;
        }

        public LocalDateTime getPrevousFiredDateTime() {
            return prevousFiredDateTime;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("Task Id: ").append(task.getTaskId()).append(", Task Status: ")
                    .append(taskStatus).append(", Prevous Fired: ").append(prevousFiredDateTime)
                    .append(", Next Fired: ").append(nextFiredDateTime);
            return str.toString();
        }

    }

    @Override
    public TaskDetail saveTask(Task task, String initialParameter) {
        task.getCronExpression().sync();
        taskStore.put(task.getTaskId(),
                new SimpleTaskDetail(task, initialParameter, TaskStatus.STANDBY));
        return taskStore.get(task.getTaskId());
    }

    @Override
    public List<LocalDateTime> findNextFiredDateTimes(TaskId taskId,
            final LocalDateTime startDateTime, final LocalDateTime endDateTime) {
        SimpleTaskDetail taskDetail = taskStore.get(taskId);
        if (taskDetail.isUnavailable()) {
            return Collections.emptyList();
        }
        CronExpression cronExpression = taskDetail.getTask().getCronExpression();
        List<LocalDateTime> results = cronExpression.list(startDateTime, endDateTime);
        return results;
    }

    @Override
    public TaskDetail removeTask(TaskId taskId) {
        return taskStore.remove(taskId);
    }

    @Override
    public String getInitialParameter(TaskId taskId) {
        return taskStore.containsKey(taskId) ? taskStore.get(taskId).getInitialParameter() : null;
    }

    @Override
    public int getTaskCount() {
        return taskStore.size();
    }

    @Override
    public boolean hasTask(TaskId taskId) {
        return taskStore.containsKey(taskId);
    }

    @Override
    public void setTaskStatus(TaskId taskId, TaskStatus status) {
        if (taskStore.containsKey(taskId)) {
            taskStore.get(taskId).setTaskStatus(status);
        }
    }

    @Override
    public TaskStatus getTaskStatus(TaskId taskId) {
        return taskStore.containsKey(taskId) ? taskStore.get(taskId).getTaskStatus() : null;
    }

    @Override
    public TaskDetail getTaskDetail(TaskId taskId) {
        return taskStore.get(taskId);
    }

    @Override
    public List<TaskId> findUpcomingTasksBetween(LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        return taskStore.entrySet().stream().filter(e -> {
            if (e.getValue().getTaskStatus() != TaskStatus.STANDBY) {
                return false;
            }
            LocalDateTime nextFiredTime = e.getValue().getNextFiredDateTime();
            return (nextFiredTime.isAfter(startDateTime) || nextFiredTime.isEqual(startDateTime))
                    && (nextFiredTime.isBefore(endDateTime));
        }).map(e -> e.getKey()).collect(Collectors.toList());
    }

    @Override
    public LocalDateTime computeNextFiredDateTime(TaskId taskId,
            LocalDateTime previousFiredDateTime) {
        if (taskStore.containsKey(taskId)) {
            SimpleTaskDetail taskDetail = taskStore.get(taskId);
            LocalDateTime nextFiredDateTime = taskDetail.getTask().getCronExpression()
                    .getNextFiredDateTime(previousFiredDateTime);
            taskDetail.setPrevousFiredDateTime(taskDetail.getNextFiredDateTime());
            taskDetail.setNextFiredDateTime(nextFiredDateTime);
            return nextFiredDateTime;
        }
        return null;
    }

}
