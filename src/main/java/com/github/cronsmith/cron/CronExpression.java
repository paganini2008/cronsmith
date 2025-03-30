
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import com.github.cronsmith.IteratorUtils;
import com.github.cronsmith.SerializationUtils;
import com.github.cronsmith.scheduler.CronScheduler;
import com.github.cronsmith.scheduler.CronSchedulerImpl;
import com.github.cronsmith.scheduler.DefaultPeriodicalExecutor;
import com.github.cronsmith.scheduler.PeriodicalExecutor;

/**
 * 
 * CronExpression represents the entire syntax tree structure.
 * 
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
@SuppressWarnings("unchecked")
public interface CronExpression extends CronStringBuilder, Serializable {

    /**
     * Get related ZoneId
     * 
     * @return
     */
    default ZoneId getZoneId() {
        return getBuilder().getZoneId();
    }

    default CronBuilder getBuilder() {
        return getParent(CronBuilder.class);
    }

    /**
     * Find Parent CronExpression
     * 
     * @param <T>
     * @param type
     * @return
     */
    default <T extends CronExpression> T getParent(Class<T> type) {
        T parent = (T) this;
        while ((parent = (T) parent.getParent()) != null) {
            if (type.isInstance(parent)) {
                return parent;
            }
        }
        return null;
    }

    CronExpression getParent();

    LocalDateTime getTime();

    /**
     * Sync with local date-time
     * 
     * @return
     */
    default CronExpression sync() {
        return sync(getBuilder().getStartTime());
    }

    /**
     * Sync with target date-time server
     * 
     * @param globalDateTime
     * @return
     */
    CronExpression sync(LocalDateTime globalDateTime);

    /**
     * Copy it self as a new object
     * 
     * @return
     */
    default CronExpression copy() {
        return SerializationUtils.copy(this);
    }

    /**
     * Consume CronExpression but not affect ifself
     * 
     * @param consumer
     * @param n
     * @return
     */
    default CronExpression consume(final Consumer<LocalDateTime> consumer, final int n) {
        LocalDateTime dateTime;
        int i = 0;
        for (CronExpression cronExpression : IteratorUtils
                .forEach((Iterator<CronExpression>) copy())) {
            dateTime = cronExpression.getTime();
            if (n < 0 || i++ < n) {
                consumer.accept(dateTime);
            } else {
                break;
            }
        }
        return this;
    }

    default LocalDateTime getNextFiredDateTime() {
        return getNextFiredDateTime(getBuilder().getStartTime());
    }

    /**
     * Consume ifself and get next fired date-time
     * 
     * @param future
     * @return
     */
    default LocalDateTime getNextFiredDateTime(LocalDateTime future) {
        Iterator<CronExpression> iterator = (Iterator<CronExpression>) this.sync();
        LocalDateTime ldt;
        do {
            CronExpression nextCron = IteratorUtils.getFirst(iterator);
            ldt = nextCron != null ? nextCron.getTime() : null;
        } while (ldt != null && future != null && (ldt.isEqual(future) || ldt.isBefore(future)));
        return ldt;
    }

    /**
     * Being scheduled with ScheduledExecutorService
     * 
     * @param executorService
     * @return
     */
    default CronScheduler scheduler(ScheduledExecutorService executorService) {
        return scheduler(new DefaultPeriodicalExecutor(executorService));
    }

    /**
     * Being scheduled with PeriodicalExecutor
     * 
     * @param periodicalExecutor
     * @return
     */
    default CronScheduler scheduler(PeriodicalExecutor periodicalExecutor) {
        return new CronSchedulerImpl(this, periodicalExecutor);
    }

}
