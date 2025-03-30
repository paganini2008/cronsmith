package com.github.cronsmith.scheduler;

/**
 * 
 * @Description: TaskListener
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface TaskListener {

    default void onTaskScheduled(Task task) {};

    default void onTaskBegin(Task task) {}

    default void onTaskEnd(Task task, Throwable e) {}

    default void onTaskCanceled(Task task) {}

    default void onTaskFinished(Task task) {};

}
