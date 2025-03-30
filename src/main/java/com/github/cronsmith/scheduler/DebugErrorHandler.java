package com.github.cronsmith.scheduler;

/**
 * 
 * @Description: DebugErrorHandler
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class DebugErrorHandler implements ErrorHandler {

    @Override
    public void onHandleTask(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onHandleResult(Throwable e) {
        e.printStackTrace();
    }

}
