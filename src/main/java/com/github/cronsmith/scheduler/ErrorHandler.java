package com.github.cronsmith.scheduler;

/**
 * 
 * @Description: ErrorHandler
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public interface ErrorHandler {

    default void onHandleTask(Throwable e) {}

    default void onHandleResult(Throwable e) {}

}
