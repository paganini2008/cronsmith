package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 
 * @Description: PendingTaskQueue
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface PendingTaskQueue {

    boolean addTask(LocalDateTime ldt, TaskId taskId);

    int length();

    List<TaskId> getTaskIds(LocalDateTime ldt);

}
