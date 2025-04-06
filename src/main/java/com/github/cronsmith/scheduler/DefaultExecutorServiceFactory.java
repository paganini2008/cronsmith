package com.github.cronsmith.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 
 * @Description: DefaultExecutorServiceFactory
 * @Author: Fred Feng
 * @Date: 06/04/2025
 * @Version 1.0.0
 */
public class DefaultExecutorServiceFactory implements ExecutorServiceFactory {

    private final static int cores = Runtime.getRuntime().availableProcessors();

    @Override
    public ScheduledExecutorService getSchedulerThreads() {
        return Executors.newScheduledThreadPool(cores);
    }

    @Override
    public ExecutorService getWorkerThreads() {
        return Executors.newFixedThreadPool(cores * 2);
    }

    @Override
    public boolean isAutoClosed() {
        return true;
    }

}
