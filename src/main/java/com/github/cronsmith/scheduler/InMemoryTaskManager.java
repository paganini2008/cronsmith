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

    private final Map<TaskId, InMemoryTaskDetail> taskStore = new ConcurrentHashMap<>();

    private static class InMemoryTaskDetail implements TaskDetail {

        private final ITask task;
        private final String initialParameter;
        private TaskStatus taskStatus;
        private LocalDateTime previousFiredDateTime;
        private LocalDateTime nextFiredDateTime;
        private LocalDateTime lastModified;

        InMemoryTaskDetail(ITask task, String initialParameter, TaskStatus taskStatus) {
            this.task = task;
            this.initialParameter = initialParameter;
            this.taskStatus = taskStatus;
            this.lastModified = LocalDateTime.now();
        }

        public ITask getTask() {
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
            this.lastModified = LocalDateTime.now();
        }

        public void setPreviousFiredDateTime(LocalDateTime previousFiredDateTime) {
            this.previousFiredDateTime = previousFiredDateTime;
            this.lastModified = LocalDateTime.now();
        }

        public LocalDateTime getNextFiredDateTime() {
            return nextFiredDateTime;
        }

        public void setNextFiredDateTime(LocalDateTime nextFiredDateTime) {
            this.nextFiredDateTime = nextFiredDateTime;
            this.lastModified = LocalDateTime.now();
        }

        public LocalDateTime getPreviousFiredDateTime() {
            return previousFiredDateTime;
        }

        public LocalDateTime getLastModified() {
            return lastModified;
        }

        public void setLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("Task Id: ").append(task.getTaskId()).append(", Task Status: ")
                    .append(taskStatus).append(", Previous Fired: ").append(previousFiredDateTime)
                    .append(", Next Fired: ").append(nextFiredDateTime);
            return str.toString();
        }

    }

    @Override
    public TaskDetail saveTask(ITask task, String initialParameter) {
        task.getCronExpression().sync();
        taskStore.put(task.getTaskId(),
                new InMemoryTaskDetail(task, initialParameter, TaskStatus.STANDBY));
        return taskStore.get(task.getTaskId());
    }

    @Override
    public List<LocalDateTime> findNextFiredDateTimes(TaskId taskId, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        InMemoryTaskDetail taskDetail = taskStore.get(taskId);
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
    public int getTaskCount(String group, String name) {
        return (int) taskStore.keySet().stream()
                .filter(tid -> StringUtils.isBlank(group) || tid.getGroup().equals(group))
                .filter(tid -> StringUtils.isBlank(name) || tid.getName().equals(name)).count();

    }

    @Override
    public List<TaskInfoVo> findTaskInfos(String group, String name, int limit, int offset) {
        return taskStore.entrySet().stream()
                .filter(e -> StringUtils.isBlank(group) || e.getKey().getGroup().equals(group))
                .filter(e -> StringUtils.isBlank(name) || e.getKey().getName().equals(name))
                .skip(offset).limit(limit).map(e -> convert2Vo(e.getValue()))
                .collect(Collectors.toList());
    }

    private TaskInfoVo convert2Vo(TaskDetail taskDetail) {
        TaskInfoVo vo = new TaskInfoVo();
        vo.setTaskGroup(taskDetail.getTask().getTaskId().getGroup());
        vo.setTaskName(taskDetail.getTask().getTaskId().getName());
        vo.setTaskClass(taskDetail.getTask().getClass().getName());
        vo.setDescription(taskDetail.getTask().getDescription());
        vo.setCron(taskDetail.getTask().getCronExpression().toString());
        vo.setTimeout(taskDetail.getTask().getTimeout());
        vo.setMaxRetryCount(taskDetail.getTask().getMaxRetryCount());
        vo.setNextFiredDateTime(taskDetail.getNextFiredDateTime());
        vo.setPrevFiredDateTime(taskDetail.getPreviousFiredDateTime());
        vo.setTaskStatus(taskDetail.getTaskStatus().name());
        return vo;
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
            InMemoryTaskDetail taskDetail = taskStore.get(taskId);
            LocalDateTime nextFiredDateTime = taskDetail.getTask().getCronExpression()
                    .getNextFiredDateTime(previousFiredDateTime);
            taskDetail.setPreviousFiredDateTime(taskDetail.getNextFiredDateTime());
            taskDetail.setNextFiredDateTime(nextFiredDateTime);
            return nextFiredDateTime;
        }
        return null;
    }

}
