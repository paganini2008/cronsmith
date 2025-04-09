package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @Description: ClockWheel
 * @Author: Fred Feng
 * @Date: 30/03/2025
 * @Version 1.0.0
 */
public class ClockWheel {

    public ClockWheel() {
        this(new DefaultExecutorServiceFactory());
    }

    public ClockWheel(ExecutorServiceFactory executorServiceFactory) {
        this.schedulerThreads = executorServiceFactory.getSchedulerThreads();
        this.workerThreads = executorServiceFactory.getWorkerThreads();
        this.executorServiceFactory = executorServiceFactory;
    }

    private final ScheduledExecutorService schedulerThreads;
    private final ExecutorService workerThreads;
    private final ExecutorServiceFactory executorServiceFactory;
    private TaskManager taskManager = new InMemoryTaskManager();
    private UpcomingTaskQueue taskQueue = new InMemoryTaskQueue();
    private ZoneId zoneId = ZoneId.systemDefault();
    private List<TaskListener> taskListeners = new ArrayList<>();
    private ErrorHandler errorHandler;
    private final AtomicBoolean started = new AtomicBoolean();
    private OneOffTimer consumer;

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public UpcomingTaskQueue getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(UpcomingTaskQueue taskQueue) {
        this.taskQueue = taskQueue;
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

    public void schedule(ITask task, String initialParameter) {
        taskManager.saveTask(task, initialParameter);
        preloadUpcomingTasks(task.getTaskId());
    }

    public void pause(ITask task) {
        TaskStatus taskStatus = taskManager.getTaskStatus(task.getTaskId());
        if (taskStatus == TaskStatus.SCHEDULED || taskStatus == TaskStatus.STANDBY) {
            taskManager.setTaskStatus(task.getTaskId(), TaskStatus.PAUSED);
        }
    }

    public void resume(ITask task) {
        if (taskManager.getTaskStatus(task.getTaskId()) == TaskStatus.PAUSED) {
            preloadUpcomingTasks(task.getTaskId());
        }
    }

    public void cancel(ITask task) {
        TaskStatus taskStatus = taskManager.getTaskStatus(task.getTaskId());
        if (taskStatus == TaskStatus.SCHEDULED || taskStatus == TaskStatus.STANDBY) {
            taskManager.setTaskStatus(task.getTaskId(), TaskStatus.CANCELED);
        }
    }

    public void remove(ITask task) {
        if (taskManager.getTaskStatus(task.getTaskId()) == TaskStatus.CANCELED) {
            taskManager.removeTask(task.getTaskId());
        }
    }

    private boolean preloadUpcomingTasks(TaskId taskId) {
        boolean preloaded = false;
        LocalDateTime now = getNow();
        LocalDateTime nextFiredDateTime = taskManager.computeNextFiredDateTime(taskId, now);
        if (nextFiredDateTime == null) {
            taskManager.setTaskStatus(taskId, TaskStatus.FINISHED);
            TaskDetail taskDetail = taskManager.getTaskDetail(taskId);
            taskListeners.forEach(l -> {
                l.onTaskFinished(taskDetail);
            });
        } else {
            LocalDateTime duration = now.plus(1L, ChronoUnit.MINUTES);
            List<LocalDateTime> firedDateTimes =
                    taskManager.findNextFiredDateTimes(taskId, now, duration);
            if (firedDateTimes != null && firedDateTimes.size() > 0) {
                for (LocalDateTime ldt : firedDateTimes) {
                    taskQueue.addTask(ldt, taskId);
                }
                taskManager.setTaskStatus(taskId, TaskStatus.SCHEDULED);
                TaskDetail taskDetail = taskManager.getTaskDetail(taskId);
                taskListeners.forEach(l -> {
                    l.onTaskScheduled(taskDetail);
                });
                preloaded = true;
            } else {
                taskManager.setTaskStatus(taskId, TaskStatus.STANDBY);
            }
        }
        return preloaded;
    }

    public void start() {
        started.set(true);
        consumer = new OneOffTimer(1, 1, TimeUnit.SECONDS, new TaskQueueLoop());
        consumer.start(false, true);
    }

    public void close() {
        if (consumer != null) {
            consumer.close();
        }
        if (executorServiceFactory.isAutoClosed()) {
            executorServiceFactory.shutdown(workerThreads);
            executorServiceFactory.shutdown(schedulerThreads);
        }
    }

    private LocalDateTime getNow() {
        return LocalDateTime.now(zoneId).withNano(0);
    }

    private class TaskQueueLoop implements OneOffTask {

        @Override
        public boolean execute() {
            Collection<TaskId> taskIds = taskQueue.matchTaskIds(getNow());
            if (taskIds != null && taskIds.size() > 0) {
                taskIds.forEach(taskId -> {

                    workerThreads.execute(() -> preloadUpcomingTasks(taskId));
                    TaskDetail taskDetail = taskManager.getTaskDetail(taskId);
                    if (taskDetail != null && !taskDetail.isUnavailable()) {
                        taskManager.setTaskStatus(taskId, TaskStatus.RUNNING);
                        TaskProxy taskProxy = new TaskProxy(taskDetail, workerThreads,
                                taskListeners, errorHandler);
                        Throwable reason = null;
                        int n = 0;
                        do {
                            try {
                                taskProxy.getProxyObject()
                                        .execute(taskManager.getInitialParameter(taskId));
                                break;
                            } catch (Throwable e) {
                                reason = e;
                            }
                        } while (n++ < taskDetail.getTask().getMaxRetryCount() && reason != null);
                        taskManager.setTaskStatus(taskId, TaskStatus.STANDBY);
                    }
                });
            }
            return started.get();
        }
    }


}
