
package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * <table cellspacing="8">
 * <tr>
 * <th align="left">Field Name</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Values</th>
 * <th align="left">&nbsp;</th>
 * <th align="left">Allowed Special Characters</th>
 * </tr>
 * <tr>
 * <td align="left"><code>Seconds</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Minutes</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-59</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Hours</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-23</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-31</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L W</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Month</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>0-11 or JAN-DEC</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Day-of-Week</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>1-7 or SUN-SAT</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * ? / L #</code></td>
 * </tr>
 * <tr>
 * <td align="left"><code>Year (Optional)</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>empty, 1970-2199</code></td>
 * <td align="left">&nbsp;</th>
 * <td align="left"><code>, - * /</code></td>
 * </tr>
 * </table>
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

    default byte[] serialize() {
        return SerializationUtils.serialize(this);
    }

    static CronExpression deserialize(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    /**
     * Consume CronExpression but not affect ifself
     * 
     * @param consumer
     * @param n
     * @return
     */
    default CronExpression consume(Consumer<LocalDateTime> consumer, int n) {
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

    default List<LocalDateTime> list(LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> dataList = new ArrayList<>();
        LocalDateTime dateTime;
        for (CronExpression cronExpression : IteratorUtils
                .forEach((Iterator<CronExpression>) copy())) {
            dateTime = cronExpression.getTime();
            if ((dateTime.isEqual(from) || dateTime.isAfter(from))
                    && (dateTime.isBefore(to) || dateTime.isEqual(to))) {
                dataList.add(dateTime);
            }
            if (dateTime.isAfter(from) && dateTime.isAfter(to)) {
                break;
            }
        }
        return dataList;
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
