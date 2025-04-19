package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;

/**
 * 
 * @Description: ErrorHandler
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface ErrorHandler {

    default void onHandleScheduler(Throwable e) {}

    default void onHandleTask(LocalDateTime datetime, Throwable e) {}

    default void onHandleTaskResult(LocalDateTime datetime, Throwable e) {}

}
