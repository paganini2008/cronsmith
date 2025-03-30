package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import com.github.cronsmith.cron.CronBuilder;

/**
 * 
 * @Description: Clock
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class Clock implements Runnable {

    static ScheduledExecutorService defaultExecutorService() {
        return Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public Clock() {
        this(defaultExecutorService(), Executors.newCachedThreadPool());
        this.autoClosed = true;
    }

    public Clock(ScheduledExecutorService schedulerExecutor, ExecutorService workerExecutor) {
        this.schedulerExecutor = schedulerExecutor;
        this.workerExecutor = workerExecutor;
    }

    private final ScheduledExecutorService schedulerExecutor;
    private final ExecutorService workerExecutor;
    private TaskManager taskManager = new InMemoryTaskManager();
    private PendingTaskQueue taskQueue = new InMemoryTaskQueue();
    private Cancellation cancellation = (task, reason) -> false;
    private ZoneId zoneId = ZoneId.systemDefault();
    private CronFuture producerFuture;
    private CronFuture consumerFuture;
    private boolean autoClosed;
    private List<TaskListener> taskListeners = new ArrayList<>();
    private ErrorHandler errorHandler;

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public PendingTaskQueue getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(PendingTaskQueue taskQueue) {
        this.taskQueue = taskQueue;
    }

    public void setCancellation(Cancellation cancellation) {
        this.cancellation = cancellation;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public List<TaskListener> getTaskListeners() {
        return taskListeners;
    }

    public void setTaskListeners(List<TaskListener> taskListeners) {
        this.taskListeners = taskListeners;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void schedule(Task task, String initialParameter) {
        taskManager.saveTask(task, initialParameter);
        taskManager.updateTaskWithNextFiredDateTime(task.getTaskId(),
                LocalDateTime.now(zoneId).withNano(0));
    }

    public void start() {
        CronScheduler cronScheduler = new CronBuilder().setZoneId(zoneId).everySecond()
                .scheduler(schedulerExecutor).setDebuged(false);
        producerFuture = cronScheduler.runTaskForEver(new PendingTaskThread());
        consumerFuture = cronScheduler.runTask(this, cancellation);
    }

    private synchronized void pollPendingTask() {
        LocalDateTime now = LocalDateTime.now(zoneId).withNano(0);
        LocalDateTime future = now.plus(5L, ChronoUnit.MINUTES);
        List<TaskId> taskIds = taskManager.getLatestTaskWillRunWithin(now, future);
        for (TaskId taskId : taskIds) {
            TaskDetail taskDetail = taskManager.getTaskDetail(taskId);
            if (taskDetail != null && taskDetail.getNextFiredDateTime() != null) {
                boolean status = taskQueue.addTask(taskDetail.getNextFiredDateTime(), taskId);
                if (status) {
                    taskManager.setTaskStatus(taskId, TaskStatus.SCHEDULED);
                    taskListeners.forEach(l -> {
                        l.onTaskScheduled(taskDetail);
                    });
                }
            } else {
                taskManager.setTaskStatus(taskId, TaskStatus.FINISHED);
                taskListeners.forEach(l -> {
                    l.onTaskFinished(taskDetail);
                });
            }
        }
    }

    public void close() {
        if (producerFuture != null) {
            producerFuture.cancel(true);
        }
        if (consumerFuture != null) {
            consumerFuture.cancel(true);
        }
        if (autoClosed) {
            schedulerExecutor.shutdown();
            if (workerExecutor != null) {
                workerExecutor.shutdown();
            }
        }
    }

    @Override
    public void run() {
        final LocalDateTime now = LocalDateTime.now(zoneId).withNano(0);
        List<TaskId> taskIds = taskQueue.getTaskIds(now);
        if (taskIds != null && taskIds.size() > 0) {
            taskIds.forEach(tid -> {
                TaskDetail taskDetail = taskManager.getTaskDetail(tid);
                if (taskDetail != null) {
                    taskManager.updateTaskWithNextFiredDateTime(tid, now);
                    taskManager.setTaskStatus(tid, TaskStatus.RUNNING);
                    TaskProxy taskProxy =
                            new TaskProxy(taskDetail, workerExecutor, taskListeners, errorHandler);
                    Throwable reason = null;
                    int n = 0;
                    do {
                        try {
                            taskProxy.getProxyObject()
                                    .execute(taskManager.getInitialParameter(tid));
                            break;
                        } catch (Throwable e) {
                            e.printStackTrace();
                            reason = e;
                        }
                    } while (n++ < taskDetail.getTask().getMaxRetryCount() && reason != null);
                    taskManager.setTaskStatus(tid, TaskStatus.STANDBY);
                }
            });
        }
    }

    private class PendingTaskThread implements Runnable {

        @Override
        public void run() {
            pollPendingTask();
        }

    }



}
