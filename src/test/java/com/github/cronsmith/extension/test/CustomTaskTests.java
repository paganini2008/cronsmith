package com.github.cronsmith.extension.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.extension.CustomTask;
import com.github.cronsmith.extension.MisfirePolicy;
import com.github.cronsmith.extension.TaskException;
import com.github.cronsmith.extension.TaskId;
import com.github.cronsmith.extension.TaskReflectionUtils;
import com.github.cronsmith.extension.test.TestTasks.ReflectiveTarget;

/**
 * 
 * Covers how a task rebuilt from a stored row reads each of its properties, especially the several
 * forms the schedule can be stored in.
 * 
 * @Description: CustomTaskTests
 * @Author: Fred Feng
 * @Date: 24/08/2026
 * @Version 1.0.0
 */
public class CustomTaskTests {

    private Map<String, Object> baseRecord() {
        Map<String, Object> record = new HashMap<>();
        record.put("taskGroup", "g");
        record.put("taskName", "n");
        record.put("taskClass", ReflectiveTarget.class.getName());
        record.put("taskMethod", "execute");
        return record;
    }

    private CustomTask custom(Map<String, Object> record) {
        return TaskReflectionUtils.getCustomTaskFactory().createTaskObject(record);
    }

    @Test
    public void testScheduleFromExpressionObject() {
        Map<String, Object> record = baseRecord();
        CronExpression expr = CRON.parse("0 0 12 * * ?");
        record.put("cronExpression", expr);
        assertEquals(expr, custom(record).getCronExpression());
    }

    @Test
    public void testScheduleFromBytes() {
        Map<String, Object> record = baseRecord();
        record.put("cronExpression", CRON.parse("0 0 12 * * ?").serialize());
        assertEquals("0 0 12 * * ?", custom(record).getCronExpression().toString());
    }

    @Test
    public void testScheduleFromText() {
        Map<String, Object> record = baseRecord();
        record.put("cronExpression", "0 0/5 * * * ?");
        assertEquals("0 0/5 * * * ?", custom(record).getCronExpression().toString());
    }

    @Test
    public void testScheduleFromCronColumnFallback() {
        Map<String, Object> record = baseRecord();
        record.put("cron", "0 0 12 * * ?");
        assertNotNull(custom(record).getCronExpression());
    }

    @Test
    public void testScheduleFromLocalDateTime() {
        Map<String, Object> record = baseRecord();
        record.put("cronExpression", LocalDateTime.now().plusDays(1));
        assertNotNull(custom(record).getCronExpression());
    }

    @Test
    public void testScheduleFromLocalDate() {
        Map<String, Object> record = baseRecord();
        record.put("cronExpression", LocalDate.now().plusDays(1));
        assertNotNull(custom(record).getCronExpression());
    }

    @Test
    public void testScheduleFromLocalTime() {
        Map<String, Object> record = baseRecord();
        record.put("cronExpression", LocalTime.of(23, 30));
        assertNotNull(custom(record).getCronExpression());
    }

    @Test(expected = TaskException.class)
    public void testMissingScheduleThrows() {
        custom(baseRecord()).getCronExpression();
    }

    @Test
    public void testIdentityAndMetadata() {
        Map<String, Object> record = baseRecord();
        record.put("url", "http://x");
        record.put("description", "desc");
        record.put("timeout", 5000L);
        record.put("maxRetryCount", 3);
        record.put("initialParameter", "p");
        record.put("misfirePolicy", "SKIP");
        CustomTask task = custom(record);
        assertEquals(TaskId.of("g", "n"), task.getTaskId());
        assertEquals(ReflectiveTarget.class.getName(), task.getTaskClassName());
        assertEquals("execute", task.getTaskMethodName());
        assertEquals("http://x", task.getUrl());
        assertEquals("desc", task.getDescription());
        assertEquals(5000L, task.getTimeout());
        assertEquals(3, task.getMaxRetryCount());
        assertEquals("p", task.getInitialParameter());
        assertEquals(MisfirePolicy.SKIP, task.getMisfirePolicy());
    }

    @Test
    public void testNumericFieldsFromStrings() {
        Map<String, Object> record = baseRecord();
        record.put("cron", "0 0 12 * * ?");
        record.put("timeout", "2000");
        record.put("maxRetryCount", "4");
        CustomTask task = custom(record);
        assertEquals(2000L, task.getTimeout());
        assertEquals(4, task.getMaxRetryCount());
    }

    @Test
    public void testDefaultsWhenAbsent() {
        Map<String, Object> record = baseRecord();
        record.put("cron", "0 0 12 * * ?");
        CustomTask task = custom(record);
        assertEquals(-1L, task.getTimeout());
        assertEquals(0, task.getMaxRetryCount());
        assertEquals(MisfirePolicy.FIRE_ONCE_NOW, task.getMisfirePolicy());
        assertEquals("execute", task.getTaskMethodName());
    }

    @Test
    public void testExecuteReachesTargetReflectively() {
        Map<String, Object> record = baseRecord();
        record.put("cron", "0 0 12 * * ?");
        ReflectiveTarget.resetCalls();
        Object result = custom(record).execute("arg");
        assertEquals("reflective:arg", result);
        assertTrue(ReflectiveTarget.getCalls() >= 1);
    }

    @Test
    public void testToString() {
        Map<String, Object> record = baseRecord();
        record.put("cron", "0 0 12 * * ?");
        assertTrue(custom(record).toString().contains("g#n"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRecordRejected() {
        new com.github.cronsmith.extension.DefaultCustomTaskFactory().createTaskObject(null)
                .getTaskId();
    }

}
