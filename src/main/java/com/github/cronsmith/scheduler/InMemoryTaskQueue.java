package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 
 * @Description: InMemoryTaskQueue
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class InMemoryTaskQueue implements PendingTaskQueue {

    private final Map<LocalDateTime, Set<TaskId>> taskIds = new ConcurrentHashMap<>();

    @Override
    public boolean addTask(LocalDateTime ldt, TaskId taskId) {
        Set<TaskId> ids = taskIds.get(ldt);
        if (ids == null) {
            taskIds.putIfAbsent(ldt, new CopyOnWriteArraySet<TaskId>());
            ids = taskIds.get(ldt);
        }
        return ids.add(taskId);
    }

    @Override
    public List<TaskId> getTaskIds(LocalDateTime ldt) {
        Set<TaskId> set = taskIds.remove(ldt);
        return set != null ? new ArrayList<TaskId>(set) : Collections.emptyList();
    }

    @Override
    public int length() {
        return taskIds.size();
    }

}
