package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 
 * @Description: InMemoryTaskManager
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class InMemoryTaskManager implements TaskManager {

    private final Map<TaskId, Task> taskStore = new ConcurrentHashMap<TaskId, Task>();
    private final Map<TaskId, String> parameters = new ConcurrentHashMap<TaskId, String>();
    private final Map<TaskId, TaskStatus> taskStatus = new ConcurrentHashMap<TaskId, TaskStatus>();

    @Override
    public void saveTask(Task task, String initialParameter) {
        taskStore.put(task.getTaskId(), task);
        parameters.put(task.getTaskId(), initialParameter);
        taskStatus.put(task.getTaskId(), TaskStatus.STANDBY);
    }

    @Override
    public void removeTask(Task task) {
        taskStore.remove(task.getTaskId(), task);
        parameters.remove(task.getTaskId());
        taskStatus.remove(task.getTaskId());
    }

    @Override
    public Task getTask(TaskId taskId) {
        return taskStore.get(taskId);
    }

    @Override
    public String getInitialParameter(TaskId taskId) {
        return parameters.get(taskId);
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
    public void pauseTask(TaskId taskId) {
        taskStatus.put(taskId, TaskStatus.PAUSED);
    }

    @Override
    public void resumeTask(TaskId taskId) {
        taskStatus.put(taskId, TaskStatus.STANDBY);
    }

    @Override
    public void setTaskStatus(TaskId taskId, TaskStatus status) {
        taskStatus.put(taskId, status);
    }

    @Override
    public TaskStatus getTaskStatus(TaskId taskId) {
        return taskStatus.get(taskId);
    }

    @Override
    public List<TaskId> getLatestTaskWillRunWithin(long period, TemporalUnit timeUnit) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime future = now.plus(period, timeUnit);
        return taskStore.entrySet().stream().filter(e -> {
            LocalDateTime nextFiredTime =
                    e.getValue().getCronExpression().getNextFiredDateTime(now);
            return nextFiredTime.compareTo(now) >= 0 && nextFiredTime.compareTo(future) < 0;
        }).map(e -> e.getKey()).collect(Collectors.toList());
    }

}
