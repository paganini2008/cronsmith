package com.github.cronsmith.scheduler;

import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: ITask
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface ITask {

    default TaskId getTaskId() {
        return TaskId.of(getClass().getSimpleName());
    }

    default String getDescription() {
        return "";
    }

    CronExpression getCronExpression();

    default long getTimeout() {
        return -1L;
    }

    default int getMaxRetryCount() {
        return -1;
    }

    Object execute(String initialParameter);

    default void handleResult(Object result, Throwable reason) {}

}
