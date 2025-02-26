package com.github.cronsmith;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import com.github.cronsmith.CronScheduledEvent.EventType;

/**
 * 
 * CronFutureImpl
 * 
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
public class CronFutureImpl implements CronFuture {

    private final AtomicBoolean scheduled;
    private final CronSchedulerEventTrigger eventTrigger;
    private Future<?> target;
    private LocalDateTime nextFiredDateTime;

    CronFutureImpl(Future<?> target, CronSchedulerEventTrigger eventTrigger) {
        this.target = target;
        this.scheduled = new AtomicBoolean(true);
        this.eventTrigger = eventTrigger;
    }

    @Override
    public void update(Future<?> target, LocalDateTime nextFiredDateTime) {
        this.target = target;
        this.nextFiredDateTime = nextFiredDateTime;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            return target.cancel(mayInterruptIfRunning);
        } finally {
            trigger(EventType.CANCELLED, null);
        }
    }

    @Override
    public boolean isCancelled() {
        return target.isCancelled();
    }

    @Override
    public boolean isDone() {
        return target.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return target.get();
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return target.get(timeout, unit);
    }

    @Override
    public void pause() {
        scheduled.set(false);
        trigger(EventType.PAUSED, null);
    }

    @Override
    public void resume() {
        scheduled.set(true);
        trigger(EventType.RESUMED, null);
    }

    @Override
    public boolean isScheduled() {
        return !isDone() && scheduled.get();
    }

    @Override
    public LocalDateTime getNextFiredDateTime() {
        return nextFiredDateTime;
    }

    @Override
    public void trigger(EventType eventType, Exception reason) {
        eventTrigger.onEventTriggered(eventType, reason);
    }

}
