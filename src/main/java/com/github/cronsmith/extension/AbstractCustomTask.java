package com.github.cronsmith.extension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * A task rebuilt from a stored row. Every property is read out of the row rather than compiled in,
 * so the same class serves any persisted task; subclasses only have to say how the body is reached.
 * 
 * @Description: AbstractCustomTask
 * @Author: Fred Feng
 * @Date: 25/04/2025
 * @Version 1.0.0
 */
public abstract class AbstractCustomTask implements CustomTask {

    protected final Map<String, Object> record;

    public AbstractCustomTask(Map<String, Object> record) {
        if (record == null) {
            throw new IllegalArgumentException("Task record is required");
        }
        this.record = record;
    }

    @Override
    public TaskId getTaskId() {
        return TaskId.of(stringOf("taskGroup", TaskId.DEFAULT_GROUP), stringOf("taskName", null));
    }

    @Override
    public String getTaskClassName() {
        return stringOf("taskClass", null);
    }

    @Override
    public String getTaskMethodName() {
        return stringOf("taskMethod", DEFAULT_METHOD_NAME);
    }

    @Override
    public String getUrl() {
        return stringOf("url", null);
    }

    @Override
    public String getDescription() {
        return stringOf("description", "");
    }

    @Override
    public long getTimeout() {
        return longOf("timeout", -1L);
    }

    @Override
    public int getMaxRetryCount() {
        return (int) longOf("maxRetryCount", 0L);
    }

    @Override
    public String getInitialParameter() {
        return stringOf("initialParameter", "");
    }

    @Override
    public MisfirePolicy getMisfirePolicy() {
        Object value = record.get("misfirePolicy");
        if (value instanceof MisfirePolicy) {
            return (MisfirePolicy) value;
        }
        if (value instanceof CharSequence) {
            return MisfirePolicy.valueOf(value.toString().trim().toUpperCase());
        }
        return MisfirePolicy.FIRE_ONCE_NOW;
    }

    /**
     * Reads whichever form the schedule was stored in: an expression object, its serialized bytes,
     * the expression text, or a plain instant meaning "once, then".
     */
    @Override
    public CronExpression getCronExpression() {
        Object object = record.get("cronExpression");
        if (object instanceof CronExpression) {
            return (CronExpression) object;
        } else if (object instanceof byte[]) {
            return CronExpression.deserialize((byte[]) object);
        } else if (object instanceof LocalDateTime) {
            return CRON.atFuture((LocalDateTime) object);
        } else if (object instanceof LocalDate) {
            return CRON.atFuture((LocalDate) object);
        } else if (object instanceof LocalTime) {
            return CRON.setInterval((LocalTime) object);
        } else if (object instanceof CharSequence) {
            return CRON.parse(object.toString());
        }
        // The 'cron' column holds the text form and is written alongside the bytes, so it is worth
        // trying before giving up.
        Object cron = record.get("cron");
        if (cron instanceof CharSequence) {
            return CRON.parse(cron.toString());
        }
        throw new TaskException("No cron expression stored for task: " + getTaskId());
    }

    @Override
    public Object execute(String initialParameter) {
        return invokeTaskMethod(getTaskId(), getTaskClassName(), getTaskMethodName(),
                initialParameter);
    }

    @Override
    public void handleResult(Object result, Throwable reason) {
        Object taskObject = TaskReflectionUtils.getTaskObject(getTaskClassName());
        if (taskObject instanceof Task) {
            ((Task) taskObject).handleResult(result, reason);
        }
    }

    /**
     * Reaches the real body. Subclasses decide whether that means reflection, an HTTP call, or a
     * lookup in whatever container owns the object.
     */
    protected abstract Object invokeTaskMethod(TaskId taskId, String taskClassName,
            String taskMethodName, String initialParameter);

    private String stringOf(String key, String defaultValue) {
        Object value = record.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private long longOf(String key, long defaultValue) {
        Object value = record.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof CharSequence) {
            try {
                return Long.parseLong(value.toString().trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return "TaskId: " + getTaskId() + ", TaskClass: " + getTaskClassName() + ", TaskMethod: "
                + getTaskMethodName();
    }

}
