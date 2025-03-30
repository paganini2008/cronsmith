package com.github.cronsmith.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.github.cronsmith.IteratorUtils;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.scheduler.CronScheduledEvent.EventType;

/**
 * 
 * CronSchedulerImpl
 * 
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class CronSchedulerImpl implements CronScheduler {

    private final CronExpression cronExpression;
    private final PeriodicalExecutor periodicalExecutor;

    public CronSchedulerImpl(CronExpression cronExpression, PeriodicalExecutor periodicalExecutor) {
        this.cronExpression = cronExpression.sync();
        this.periodicalExecutor = periodicalExecutor;
    }

    private final Map<Runnable, CronFuture> futures = new ConcurrentHashMap<>();
    private final List<CronSchedulerListener> schedulerListeners = new CopyOnWriteArrayList<>();
    private final DebugCronSchedulerListener debugCronSchedulerListener =
            new DebugCronSchedulerListener();

    @Override
    public CronFuture runTask(Runnable task, Cancellation cancellation) {
        if (futures.containsKey(task)) {
            throw new IllegalStateException("Task' " + task.toString() + "' has been existed");
        }
        LocalDateTime now = LocalDateTime.now(cronExpression.getZoneId());
        CronExpression copy = cronExpression.copy();
        LocalDateTime nextFiredDateTime = getNextFiredDateTime(copy);
        if (nextFiredDateTime != null) {
            long betweenInMs = ChronoUnit.MILLIS.between(now, nextFiredDateTime);
            CronSchedulerEventTrigger eventTrigger = onEventTriggered(task);
            CronFuture future = new CronFutureImpl(
                    periodicalExecutor.schedule(new RunnableWrapper(task, copy, cancellation),
                            betweenInMs, TimeUnit.MILLISECONDS),
                    eventTrigger);
            futures.put(task, future);
            return future;
        }
        System.out.println("futures.size(): " + futures.size());
        return null;
    }

    @Override
    public void pauseTask(Runnable task) {
        CronFuture future = futures.get(task);
        if (future != null) {
            future.pause();
        }
    }

    @Override
    public void resumeTask(Runnable task) {
        CronFuture future = futures.get(task);
        if (future != null) {
            future.resume();
        }
    }

    @Override
    public void removeTask(Runnable task) {
        CronFuture future = futures.remove(task);
        if (future != null) {
            if (!future.isDone()) {
                future.cancel(true);
            }
            future.trigger(EventType.REMOVED, null);
        }
    }

    @Override
    public int countOfTasks() {
        return futures.size();
    }

    @Override
    public boolean checkExisted(Runnable task) {
        return futures.containsKey(task);
    }

    @Override
    public CronScheduler setDebuged(boolean debuged) {
        if (debuged) {
            schedulerListeners.add(debugCronSchedulerListener);
        } else {
            schedulerListeners.remove(debugCronSchedulerListener);
        }
        return this;
    }

    @Override
    public CronScheduler subscribe(CronSchedulerListener schedulerListener) {
        if (schedulerListener != null) {
            if (!schedulerListeners.contains(schedulerListener)) {
                schedulerListeners.add(schedulerListener);
            }
        }
        return this;
    }

    @Override
    public void unsubscribe(CronSchedulerListener schedulerListener) {
        if (schedulerListener != null) {
            schedulerListeners.remove(schedulerListener);
        }
    }

    @SuppressWarnings("unchecked")
    private LocalDateTime getNextFiredDateTime(CronExpression cronExpression) {
        Iterator<CronExpression> iterator = (Iterator<CronExpression>) cronExpression;
        LocalDateTime ldt;
        do {
            CronExpression nextCron = IteratorUtils.getFirst(iterator);
            ldt = nextCron != null ? nextCron.getTime() : null;
        } while (ldt != null && ldt.isBefore(LocalDateTime.now(cronExpression.getZoneId())));
        return ldt;
    }

    protected CronSchedulerEventTrigger onEventTriggered(Runnable task) {
        final CronScheduler source = this;
        return (eventType, e) -> {
            switch (eventType) {
                case SCHEDULED:
                    schedulerListeners.forEach(l -> l
                            .onTaskScheduled(new CronScheduledEvent(source, task, eventType)));
                    break;
                case FINISHED:
                    CronScheduledEvent finishedEvent =
                            new CronScheduledEvent(source, task, eventType);
                    finishedEvent.setNextFiredDateTime(
                            futures.containsKey(task) ? futures.get(task).getNextFiredDateTime()
                                    : null);
                    finishedEvent.setReason(e);
                    schedulerListeners.forEach(l -> l.onTaskScheduled(finishedEvent));
                    break;
                case PAUSED:
                    schedulerListeners.forEach(
                            l -> l.onTaskPaused(new CronScheduledEvent(source, task, eventType)));
                    break;
                case RESUMED:
                    schedulerListeners.forEach(
                            l -> l.onTaskResumed(new CronScheduledEvent(source, task, eventType)));
                    break;
                case CANCELLED:
                    schedulerListeners.forEach(l -> l
                            .onTaskCancelled(new CronScheduledEvent(source, task, eventType)));
                    break;
                case REMOVED:
                    schedulerListeners.forEach(
                            l -> l.onTaskRemoved(new CronScheduledEvent(source, task, eventType)));
                    break;
                case FAILED:
                    CronScheduledEvent failedEvent =
                            new CronScheduledEvent(source, task, eventType);
                    failedEvent.setReason(e);
                    schedulerListeners.forEach(l -> l.onTaskFailed(failedEvent));
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown event type: " + eventType);
            }
        };
    }

    private class RunnableWrapper implements Runnable {

        private final Runnable task;
        private final CronExpression copy;
        private final Cancellation cancellation;

        RunnableWrapper(Runnable task, CronExpression copy, Cancellation cancellation) {
            this.task = task;
            this.copy = copy;
            this.cancellation = cancellation;
        }

        @Override
        public void run() {
            CronFuture cronFuture = futures.get(task);
            if (cronFuture == null) {
                return;
            }
            boolean ended = false;
            Exception reason = null;
            try {
                if (cronFuture.isScheduled()) {
                    cronFuture.trigger(EventType.SCHEDULED, null);
                    task.run();
                }
            } catch (Exception e) {
                reason = e;
                cronFuture.trigger(EventType.FAILED, e);
            }
            LocalDateTime now = LocalDateTime.now(cronExpression.getZoneId());
            LocalDateTime nextFiredDateTime = getNextFiredDateTime(copy);
            if (nextFiredDateTime != null) {
                long betweenInMs = ChronoUnit.MILLIS.between(now, nextFiredDateTime);
                Future<?> future =
                        periodicalExecutor.schedule(this, betweenInMs, TimeUnit.MILLISECONDS);
                cronFuture.update(future, nextFiredDateTime);
            } else {
                removeTask(task);
                ended = true;
            }
            if (ended || cronFuture.isScheduled()) {
                cronFuture.trigger(EventType.FINISHED, reason);
            }
            if (!ended && (cancellation != null && cancellation.cancelTask(task, reason))) {
                removeTask(task);
            }
        }
    }
}
