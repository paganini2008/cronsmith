
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

    default ZoneId getZoneId() {
        return getBuilder().getZoneId();
    }

    default CronBuilder getBuilder() {
        return getParent(CronBuilder.class);
    }

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

    default CronExpression consume(final Consumer<LocalDateTime> consumer, final int n) {
        if (!(this instanceof Iterator)) {
            throw new UnsupportedOperationException();
        }
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

    /**
     * Being scheduled with ScheduledExecutorService
     * 
     * @param executorService
     * @return
     */
    default CronScheduler scheduler(ScheduledExecutorService executorService) {
        return scheduler(new DefaultPeriodicalExecutor(executorService));
    }

    default CronScheduler scheduler(PeriodicalExecutor periodicalExecutor) {
        return new CronSchedulerImpl(this, periodicalExecutor);
    }

}
