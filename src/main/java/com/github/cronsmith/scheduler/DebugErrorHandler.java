package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;

/**
 * 
 * @Description: DebugErrorHandler
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class DebugErrorHandler implements ErrorHandler {

    @Override
    public void onHandleScheduler(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onHandleTask(LocalDateTime datetime, Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onHandleTaskResult(LocalDateTime datetime, Throwable e) {
        e.printStackTrace();
    }

}
