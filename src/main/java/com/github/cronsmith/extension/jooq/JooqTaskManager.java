package com.github.cronsmith.extension.jooq;

import static com.github.cronsmith.extension.jooq.TaskTables.ATTEMPT;
import static com.github.cronsmith.extension.jooq.TaskTables.COMPLETED_DATETIME;
import static com.github.cronsmith.extension.jooq.TaskTables.CRON;
import static com.github.cronsmith.extension.jooq.TaskTables.CRON_EXPRESSION;
import static com.github.cronsmith.extension.jooq.TaskTables.DESCRIPTION;
import static com.github.cronsmith.extension.jooq.TaskTables.ELAPSED;
import static com.github.cronsmith.extension.jooq.TaskTables.ERROR_DETAIL;
import static com.github.cronsmith.extension.jooq.TaskTables.FAILURE_COUNT;
import static com.github.cronsmith.extension.jooq.TaskTables.FIRED_DATETIME;
import static com.github.cronsmith.extension.jooq.TaskTables.INITIAL_PARAMETER;
import static com.github.cronsmith.extension.jooq.TaskTables.LAST_MODIFIED;
import static com.github.cronsmith.extension.jooq.TaskTables.MAX_RETRY_COUNT;
import static com.github.cronsmith.extension.jooq.TaskTables.MISFIRE_COUNT;
import static com.github.cronsmith.extension.jooq.TaskTables.MISFIRE_POLICY;
import static com.github.cronsmith.extension.jooq.TaskTables.NEXT_FIRED_DATETIME;
import static com.github.cronsmith.extension.jooq.TaskTables.PREV_FIRED_DATETIME;
import static com.github.cronsmith.extension.jooq.TaskTables.RETRY_INTERVAL;
import static com.github.cronsmith.extension.jooq.TaskTables.RETURN_VALUE;
import static com.github.cronsmith.extension.jooq.TaskTables.RUN_COUNT;
import static com.github.cronsmith.extension.jooq.TaskTables.SCHEDULED_DATETIME;
import static com.github.cronsmith.extension.jooq.TaskTables.SUCCESS;
import static com.github.cronsmith.extension.jooq.TaskTables.TASK_CLASS;
import static com.github.cronsmith.extension.jooq.TaskTables.TASK_GROUP;
import static com.github.cronsmith.extension.jooq.TaskTables.TASK_METHOD;
import static com.github.cronsmith.extension.jooq.TaskTables.TASK_NAME;
import static com.github.cronsmith.extension.jooq.TaskTables.TASK_STATUS;
import static com.github.cronsmith.extension.jooq.TaskTables.TIMEOUT;
import static com.github.cronsmith.extension.jooq.TaskTables.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectLimitStep;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.JDBCUtils;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.extension.ApiCallTask;
import com.github.cronsmith.extension.BeanReflectionTask;
import com.github.cronsmith.extension.MisfirePolicy;
import com.github.cronsmith.extension.Settings;
import com.github.cronsmith.extension.Task;
import com.github.cronsmith.extension.TaskDetail;
import com.github.cronsmith.extension.TaskDetailNotFoundException;
import com.github.cronsmith.extension.TaskException;
import com.github.cronsmith.extension.TaskExecutionLog;
import com.github.cronsmith.extension.TaskId;
import com.github.cronsmith.extension.TaskManager;
import com.github.cronsmith.extension.TaskQuery;
import com.github.cronsmith.extension.TaskReflectionUtils;
import com.github.cronsmith.extension.TaskRestoreHandler;
import com.github.cronsmith.extension.TaskStatus;
import com.github.cronsmith.utils.CamelCasedLinkedHashMap;
import com.github.cronsmith.utils.StringUtils;

/**
 * 
 * A {@link TaskManager} backed by a relational database through JOOQ, so that a schedule survives a
 * restart and can be inspected and edited outside the process that runs it.
 * 
 * <p>
 * No SQL is written by hand and no dialect-specific construct is used, so the same code runs on
 * every dialect JOOQ supports; only the DDL differs, and one script per dialect ships under
 * {@code db/} in the resources. Statements run on whatever connection the {@link DSLContext} hands
 * out and are left to that connection's transaction settings: this class never commits, which is
 * what lets a caller wrap several operations in a transaction of their own.
 * 
 * <p>
 * Status changes go through a conditional update rather than a read followed by a write, so two
 * schedulers racing for the same task cannot both believe they won. That is also the piece the
 * clustered scheduler will build on.
 * 
 * @Description: JooqTaskManager
 * @Author: Fred Feng
 * @Date: 08/04/2025
 * @Version 1.0.0
 */
public class JooqTaskManager implements TaskManager {

    /** How many times a status change is retried when another writer got there first. */
    private static final int MAX_CAS_ATTEMPTS = 8;

    private final DSLContext dsl;
    private final TaskTables tables;

    public JooqTaskManager(DataSource dataSource) {
        this(DSL.using(dataSource, detectDialect(dataSource)), new TaskTables());
    }

    public JooqTaskManager(DSLContext dsl) {
        this(dsl, new TaskTables());
    }

    public JooqTaskManager(DSLContext dsl, TaskTables tables) {
        if (dsl == null) {
            throw new IllegalArgumentException("DSLContext is required");
        }
        this.dsl = dsl;
        this.tables = tables != null ? tables : new TaskTables();
    }

    /**
     * Asks the driver which dialect it speaks, so callers that only have a DataSource do not have
     * to name it themselves.
     */
    private static SQLDialect detectDialect(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource is required");
        }
        try (Connection connection = dataSource.getConnection()) {
            return JDBCUtils.dialect(connection);
        } catch (SQLException e) {
            throw new TaskException("Cannot determine the SQL dialect of the DataSource", e);
        }
    }

    public TaskTables getTables() {
        return tables;
    }

    public DSLContext getDslContext() {
        return dsl;
    }

    /**
     * 
     * A task detail backed by one row, read lazily. The task object itself is rebuilt from the row
     * through the custom task factory, so a task whose class no longer exists fails only when
     * something actually asks for it.
     * 
     * @Description: JooqTaskDetail
     * @Author: Fred Feng
     * @Date: 08/04/2025
     * @Version 1.0.0
     */
    static class JooqTaskDetail implements TaskDetail {

        private final Map<String, Object> record;

        JooqTaskDetail(Map<String, Object> record) {
            this.record = record;
        }

        @Override
        public Task getTask() {
            return TaskReflectionUtils.getTaskObject((String) record.get("taskClass"), record);
        }

        @Override
        public TaskId getTaskId() {
            return TaskId.of((String) record.get("taskGroup"), (String) record.get("taskName"));
        }

        @Override
        public String getInitialParameter() {
            return (String) record.get("initialParameter");
        }

        @Override
        public TaskStatus getTaskStatus() {
            return TaskStatus.forName((String) record.get("taskStatus"));
        }

        // The datetime columns are read by their exact column names, not a camel-cased alias.
        // "next_fired_datetime" spells datetime as one word, whereas the property "nextFiredDateTime"
        // would be de-camelised to "next_fired_date_time" and miss the column.
        @Override
        public LocalDateTime getNextFiredDateTime() {
            return (LocalDateTime) record.get("next_fired_datetime");
        }

        @Override
        public LocalDateTime getPreviousFiredDateTime() {
            return (LocalDateTime) record.get("prev_fired_datetime");
        }

        @Override
        public LocalDateTime getLastModified() {
            return (LocalDateTime) record.get("last_modified");
        }

        @Override
        public long getRunCount() {
            return longOf(record.get("runCount"));
        }

        @Override
        public long getFailureCount() {
            return longOf(record.get("failureCount"));
        }

        @Override
        public long getMisfireCount() {
            return longOf(record.get("misfireCount"));
        }

        private static long longOf(Object value) {
            return value instanceof Number ? ((Number) value).longValue() : 0L;
        }

        /**
         * The row as stored, for callers that want a column this interface does not expose.
         */
        public Map<String, Object> getRecord() {
            return Collections.unmodifiableMap(record);
        }

        @Override
        public String toString() {
            return "Task Id: " + getTaskId() + ", Task Status: " + getTaskStatus()
                    + ", Previous Fired: " + getPreviousFiredDateTime() + ", Next Fired: "
                    + getNextFiredDateTime();
        }

    }

    @Override
    public TaskDetail saveTask(Task task, String initialParameter) {
        if (task == null) {
            throw new IllegalArgumentException("Task is required");
        }
        TaskId taskId = task.getTaskId();
        String parameter = StringUtils.isNotBlank(initialParameter) ? initialParameter
                : task.getInitialParameter();
        // Anchored to now before it is stored, so the fire times computed from the row start from
        // when the task was registered rather than from whenever its builder was constructed.
        CronExpression cronExpression = task.getCronExpression().sync();
        String taskClass;
        String taskMethod;
        String url;
        if (task instanceof ApiCallTask apiCallTask) {
            // A data-only HTTP task: no class or method; the request line lives in the url column.
            taskClass = null;
            taskMethod = null;
            url = apiCallTask.getUrl();
        } else if (task instanceof BeanReflectionTask beanReflectionTask) {
            taskClass = beanReflectionTask.getTaskClassName();
            taskMethod = beanReflectionTask.getTaskMethodName();
            url = null;
        } else {
            taskClass = task.getClass().getName();
            taskMethod = Task.DEFAULT_METHOD_NAME;
            url = null;
        }
        try {
            // Re-saving is a re-registration: the definition is refreshed and the task drops back
            // to standby with no fire times, but its counters are history and are left alone.
            int updated = dsl.update(tables.taskDetail()).set(TASK_CLASS, taskClass)
                    .set(TASK_METHOD, taskMethod).set(URL, url)
                    .set(DESCRIPTION, task.getDescription())
                    .set(CRON_EXPRESSION, cronExpression.serialize())
                    .set(CRON, cronExpression.toString()).set(INITIAL_PARAMETER, parameter)
                    .set(MAX_RETRY_COUNT, task.getMaxRetryCount())
                    .set(RETRY_INTERVAL, task.getRetryInterval()).set(TIMEOUT, task.getTimeout())
                    .set(MISFIRE_POLICY, task.getMisfirePolicy().name())
                    .set(TASK_STATUS, TaskStatus.STANDBY.name())
                    .set(NEXT_FIRED_DATETIME, (LocalDateTime) null)
                    .set(PREV_FIRED_DATETIME, (LocalDateTime) null)
                    .set(LAST_MODIFIED, Settings.now()).where(idCondition(taskId)).execute();
            if (updated == 0) {
                dsl.insertInto(tables.taskDetail()).set(TASK_GROUP, taskId.getGroup())
                        .set(TASK_NAME, taskId.getName()).set(TASK_CLASS, taskClass)
                        .set(TASK_METHOD, taskMethod).set(URL, url)
                        .set(DESCRIPTION, task.getDescription())
                        .set(CRON_EXPRESSION, cronExpression.serialize())
                        .set(CRON, cronExpression.toString()).set(INITIAL_PARAMETER, parameter)
                        .set(MAX_RETRY_COUNT, task.getMaxRetryCount())
                        .set(RETRY_INTERVAL, task.getRetryInterval())
                        .set(TIMEOUT, task.getTimeout())
                        .set(MISFIRE_POLICY, task.getMisfirePolicy().name())
                        .set(TASK_STATUS, TaskStatus.STANDBY.name()).set(RUN_COUNT, 0L)
                        .set(FAILURE_COUNT, 0L).set(MISFIRE_COUNT, 0L)
                        .set(LAST_MODIFIED, Settings.now()).execute();
            }
        } catch (DataAccessException e) {
            throw new TaskException("Cannot save task " + taskId, e);
        }
        return getTaskDetail(taskId, true);
    }

    @Override
    public TaskDetail removeTask(TaskId taskId) {
        TaskDetail taskDetail = getTaskDetail(taskId, false);
        if (taskDetail == null) {
            return null;
        }
        try {
            dsl.deleteFrom(tables.taskLog()).where(idCondition(taskId)).execute();
            dsl.deleteFrom(tables.taskDetail()).where(idCondition(taskId)).execute();
        } catch (DataAccessException e) {
            throw new TaskException("Cannot remove task " + taskId, e);
        }
        return taskDetail;
    }

    @Override
    public TaskDetail getTaskDetail(TaskId taskId, boolean thrown) {
        Map<String, Object> record = taskId != null ? fetchRecord(taskId) : null;
        if (record != null) {
            return new JooqTaskDetail(record);
        }
        if (thrown) {
            throw new TaskDetailNotFoundException(taskId);
        }
        return null;
    }

    private Map<String, Object> fetchRecord(TaskId taskId) {
        try {
            Record record = dsl.select(tables.detailFields()).from(tables.taskDetail())
                    .where(idCondition(taskId)).fetchOne();
            return record != null ? toMap(record) : null;
        } catch (DataAccessException e) {
            throw new TaskException("Cannot read task " + taskId, e);
        }
    }

    /**
     * Turns a row into a map keyed by property name, which is the form
     * {@link com.github.cronsmith.extension.AbstractTask} reads a stored task from.
     */
    private static Map<String, Object> toMap(Record record) {
        CamelCasedLinkedHashMap map = new CamelCasedLinkedHashMap(record.size());
        for (org.jooq.Field<?> field : record.fields()) {
            map.put(field.getName(), record.get(field));
        }
        return map;
    }

    @Override
    public boolean hasTask(TaskId taskId) {
        if (taskId == null) {
            return false;
        }
        try {
            return dsl.fetchExists(dsl.selectOne().from(tables.taskDetail())
                    .where(idCondition(taskId)));
        } catch (DataAccessException e) {
            throw new TaskException("Cannot check task " + taskId, e);
        }
    }

    @Override
    public int getTaskCount(TaskQuery query) {
        try {
            Integer count = dsl.selectCount().from(tables.taskDetail())
                    .where(conditionsOf(query)).fetchOne(0, Integer.class);
            return count != null ? count : 0;
        } catch (DataAccessException e) {
            throw new TaskException("Cannot count tasks", e);
        }
    }

    @Override
    public List<TaskDetail> findTaskDetails(TaskQuery query) {
        try {
            SelectConditionStep<Record> where = dsl.select(tables.detailFields())
                    .from(tables.taskDetail()).where(conditionsOf(query));
            SelectLimitStep<Record> ordered = where.orderBy(LAST_MODIFIED.desc());
            Result<Record> result;
            if (query != null && query.getLimit() > 0) {
                result = ordered.limit(query.getLimit()).offset(query.getOffset()).fetch();
            } else if (query != null && query.getOffset() > 0) {
                result = ordered.offset(query.getOffset()).fetch();
            } else {
                result = ordered.fetch();
            }
            List<TaskDetail> taskDetails = new ArrayList<>(result.size());
            for (Record record : result) {
                taskDetails.add(new JooqTaskDetail(toMap(record)));
            }
            return taskDetails;
        } catch (DataAccessException e) {
            throw new TaskException("Cannot list tasks", e);
        }
    }

    private List<Condition> conditionsOf(TaskQuery query) {
        List<Condition> conditions = new ArrayList<>();
        if (query == null) {
            return conditions;
        }
        if (StringUtils.isNotBlank(query.getTaskGroup())) {
            conditions.add(TASK_GROUP.eq(query.getTaskGroup()));
        }
        if (StringUtils.isNotBlank(query.getTaskName())) {
            conditions.add(TASK_NAME.contains(query.getTaskName()));
        }
        if (StringUtils.isNotBlank(query.getTaskClass())) {
            conditions.add(TASK_CLASS.contains(query.getTaskClass()));
        }
        if (!query.getStatuses().isEmpty()) {
            List<String> names = new ArrayList<>(query.getStatuses().size());
            query.getStatuses().forEach(s -> names.add(s.name()));
            conditions.add(TASK_STATUS.in(names));
        }
        return conditions;
    }

    @Override
    public List<LocalDateTime> findNextFiredDateTimes(TaskId taskId, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        Map<String, Object> record = fetchRecord(taskId);
        if (record == null) {
            return Collections.emptyList();
        }
        TaskStatus status = TaskStatus.forName((String) record.get("taskStatus"));
        if (status != null && status.isUnavailable()) {
            return Collections.emptyList();
        }
        return readCronExpression(record, taskId).list(startDateTime, endDateTime);
    }

    @Override
    public List<TaskId> findUpcomingTasksBetween(LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        try {
            return dsl.select(TASK_GROUP, TASK_NAME).from(tables.taskDetail())
                    .where(NEXT_FIRED_DATETIME.greaterOrEqual(startDateTime))
                    .and(NEXT_FIRED_DATETIME.lessThan(endDateTime))
                    .and(TASK_STATUS.notIn(TaskStatus.FINISHED.name(), TaskStatus.CANCELED.name(),
                            TaskStatus.PAUSED.name()))
                    .fetch(r -> TaskId.of(r.value1(), r.value2()));
        } catch (DataAccessException e) {
            throw new TaskException("Cannot list upcoming tasks", e);
        }
    }

    @Override
    public LocalDateTime computeNextFiredDateTime(TaskId taskId,
            LocalDateTime previousFiredDateTime) {
        Map<String, Object> record = fetchRecord(taskId);
        if (record == null) {
            return null;
        }
        CronExpression cronExpression = readCronExpression(record, taskId);
        LocalDateTime nextFiredDateTime =
                cronExpression.getNextFiredDateTime(previousFiredDateTime);
        try {
            dsl.update(tables.taskDetail()).set(NEXT_FIRED_DATETIME, nextFiredDateTime)
                    .set(PREV_FIRED_DATETIME, previousFiredDateTime)
                    // The expression carries its own position, so the advanced form is written
                    // back; otherwise a restart would recompute from where it was first stored.
                    .set(CRON_EXPRESSION, cronExpression.serialize())
                    .set(LAST_MODIFIED, Settings.now()).where(idCondition(taskId)).execute();
        } catch (DataAccessException e) {
            throw new TaskException("Cannot store the next fire time of " + taskId, e);
        }
        return nextFiredDateTime;
    }

    /**
     * Reads the schedule from a row, preferring the serialized form and falling back to the text
     * form so that a row written by a different version is still usable.
     */
    private CronExpression readCronExpression(Map<String, Object> record, TaskId taskId) {
        Object bytes = record.get("cronExpression");
        if (bytes instanceof byte[]) {
            try {
                return CronExpression.deserialize((byte[]) bytes);
            } catch (RuntimeException e) {
                Object cron = record.get("cron");
                if (cron != null) {
                    return com.github.cronsmith.CRON.parse(cron.toString());
                }
                throw new TaskException("Cannot read the schedule of task " + taskId, e);
            }
        }
        Object cron = record.get("cron");
        if (cron != null) {
            return com.github.cronsmith.CRON.parse(cron.toString());
        }
        throw new TaskException("No schedule stored for task " + taskId);
    }

    @Override
    public void restoreTasks(TaskRestoreHandler restoreHandler) {
        if (restoreHandler == null) {
            return;
        }
        org.jooq.Result<? extends Record> rows;
        try {
            // RUNNING is included on purpose: a row left in that state is a task that was mid-run
            // when the process died, and it has to be picked up again rather than stranded.
            rows = dsl.select(TASK_GROUP, TASK_NAME, NEXT_FIRED_DATETIME).from(tables.taskDetail())
                    .where(TASK_STATUS.in(TaskStatus.STANDBY.name(), TaskStatus.SCHEDULED.name(),
                            TaskStatus.RUNNING.name()))
                    .fetch();
        } catch (DataAccessException e) {
            throw new TaskException("Cannot read tasks to restore", e);
        }
        for (Record row : rows) {
            TaskId taskId = TaskId.of(row.get(TASK_GROUP), row.get(TASK_NAME));
            // Whatever it was doing, it is standing by now; the scheduler decides what comes next.
            forceStatus(taskId, TaskStatus.STANDBY);
            restoreHandler.onRestore(taskId, row.get(NEXT_FIRED_DATETIME));
        }
    }

    @Override
    public boolean setTaskStatus(TaskId taskId, TaskStatus status) {
        for (int attempt = 0; attempt < MAX_CAS_ATTEMPTS; attempt++) {
            TaskStatus current = getTaskStatus(taskId);
            if (current == null || !current.canTransitionTo(status)) {
                return false;
            }
            if (current == status) {
                return true;
            }
            if (casStatus(taskId, current, status)) {
                return true;
            }
            // Someone else moved it first; look again and decide against the new state.
        }
        return false;
    }

    @Override
    public boolean compareAndSetTaskStatus(TaskId taskId, TaskStatus expected, TaskStatus target) {
        if (expected == null || target == null || !expected.canTransitionTo(target)) {
            return false;
        }
        return casStatus(taskId, expected, target);
    }

    /**
     * The one place a status is written: a conditional update, so the database decides the winner
     * when two callers race.
     */
    private boolean casStatus(TaskId taskId, TaskStatus expected, TaskStatus target) {
        try {
            return dsl.update(tables.taskDetail()).set(TASK_STATUS, target.name())
                    .set(LAST_MODIFIED, Settings.now()).where(idCondition(taskId))
                    .and(TASK_STATUS.eq(expected.name())).execute() > 0;
        } catch (DataAccessException e) {
            throw new TaskException("Cannot change the status of task " + taskId, e);
        }
    }

    private void forceStatus(TaskId taskId, TaskStatus target) {
        try {
            dsl.update(tables.taskDetail()).set(TASK_STATUS, target.name())
                    .set(LAST_MODIFIED, Settings.now()).where(idCondition(taskId)).execute();
        } catch (DataAccessException e) {
            throw new TaskException("Cannot change the status of task " + taskId, e);
        }
    }

    @Override
    public void recordExecution(TaskExecutionLog executionLog) {
        if (executionLog == null) {
            return;
        }
        TaskId taskId = executionLog.getTaskId();
        try {
            dsl.insertInto(tables.taskLog()).set(TASK_GROUP, taskId.getGroup())
                    .set(TASK_NAME, taskId.getName())
                    .set(SCHEDULED_DATETIME, executionLog.getScheduledDateTime())
                    .set(FIRED_DATETIME, executionLog.getFiredDateTime())
                    .set(COMPLETED_DATETIME, executionLog.getCompletedDateTime())
                    .set(RETURN_VALUE, executionLog.getReturnValue())
                    .set(ERROR_DETAIL, executionLog.getErrorDetail())
                    .set(ELAPSED, executionLog.getElapsed())
                    .set(ATTEMPT, executionLog.getAttempt())
                    .set(SUCCESS, executionLog.isSuccess()).execute();

            // Counters are bumped in SQL rather than read-modify-written, so concurrent runs of
            // different tasks, or retries of the same one, cannot lose an increment.
            dsl.update(tables.taskDetail()).set(RUN_COUNT, RUN_COUNT.plus(1L))
                    .set(FAILURE_COUNT,
                            executionLog.isSuccess() ? FAILURE_COUNT.plus(0L)
                                    : FAILURE_COUNT.plus(1L))
                    .set(LAST_MODIFIED, Settings.now()).where(idCondition(taskId)).execute();
        } catch (DataAccessException e) {
            throw new TaskException("Cannot record the execution of task " + taskId, e);
        }
    }

    @Override
    public List<TaskExecutionLog> findExecutionLogs(TaskId taskId, int limit, int offset) {
        try {
            SelectLimitStep<Record> ordered = dsl.select(tables.logFields())
                    .from(tables.taskLog()).where(idCondition(taskId))
                    .orderBy(SCHEDULED_DATETIME.desc(), ATTEMPT.desc());
            Result<Record> result = limit > 0
                    ? ordered.limit(limit).offset(Math.max(0, offset)).fetch()
                    : ordered.offset(Math.max(0, offset)).fetch();
            List<TaskExecutionLog> logs = new ArrayList<>(result.size());
            for (Record record : result) {
                TaskExecutionLog executionLog =
                        new TaskExecutionLog(taskId, record.get(SCHEDULED_DATETIME))
                                .firedAt(record.get(FIRED_DATETIME))
                                .completedAt(record.get(COMPLETED_DATETIME))
                                .elapsed(orZero(record.get(ELAPSED)))
                                .attempt(record.get(ATTEMPT) != null ? record.get(ATTEMPT) : 0)
                                .success(Boolean.TRUE.equals(record.get(SUCCESS)));
                executionLog.setStoredReturnValue(record.get(RETURN_VALUE));
                executionLog.setStoredErrorDetail(record.get(ERROR_DETAIL));
                logs.add(executionLog);
            }
            return logs;
        } catch (DataAccessException e) {
            throw new TaskException("Cannot read the execution logs of task " + taskId, e);
        }
    }

    private static long orZero(Long value) {
        return value != null ? value : 0L;
    }

    @Override
    public void recordMisfire(TaskId taskId, LocalDateTime missedDateTime) {
        try {
            dsl.update(tables.taskDetail()).set(MISFIRE_COUNT, MISFIRE_COUNT.plus(1L))
                    .set(LAST_MODIFIED, Settings.now()).where(idCondition(taskId)).execute();
        } catch (DataAccessException e) {
            throw new TaskException("Cannot record a misfire of task " + taskId, e);
        }
    }

    /**
     * The primary key predicate every statement here is keyed on.
     */
    private Condition idCondition(TaskId taskId) {
        return TASK_GROUP.eq(taskId.getGroup()).and(TASK_NAME.eq(taskId.getName()));
    }

    /**
     * The misfire policy stored for a task, for callers reading a row directly.
     */
    static MisfirePolicy misfirePolicyOf(Map<String, Object> record) {
        Object value = record.get("misfirePolicy");
        if (value instanceof CharSequence) {
            return MisfirePolicy.valueOf(value.toString().trim().toUpperCase());
        }
        return MisfirePolicy.FIRE_ONCE_NOW;
    }

}
