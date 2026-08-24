package com.github.cronsmith.extension.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.github.cronsmith.extension.CustomTask;
import com.github.cronsmith.extension.Task;
import com.github.cronsmith.extension.TaskId;
import com.github.cronsmith.extension.TaskInvocationException;
import com.github.cronsmith.extension.TaskReflectionUtils;
import com.github.cronsmith.extension.test.TestTasks.ReflectiveTarget;

/**
 * 
 * Covers how a stored task definition is resolved back into a class, an instance and a method,
 * including the caching that keeps a per-second task from paying reflection costs on every run.
 * 
 * @Description: TaskReflectionUtilsTests
 * @Author: Fred Feng
 * @Date: 24/08/2026
 * @Version 1.0.0
 */
public class TaskReflectionUtilsTests {

    private static final String TARGET = ReflectiveTarget.class.getName();

    @Before
    public void setUp() {
        TaskReflectionUtils.clearCaches();
        ReflectiveTarget.resetCalls();
    }

    @Test
    public void testGetTaskClass() {
        assertEquals(ReflectiveTarget.class, TaskReflectionUtils.getTaskClass(TARGET));
    }

    @Test
    public void testTaskClassIsCached() {
        assertSame(TaskReflectionUtils.getTaskClass(TARGET),
                TaskReflectionUtils.getTaskClass(TARGET));
    }

    @Test(expected = TaskInvocationException.class)
    public void testMissingClassThrows() {
        TaskReflectionUtils.getTaskClass("com.example.NoSuchClass");
    }

    @Test(expected = TaskInvocationException.class)
    public void testBlankClassNameThrows() {
        TaskReflectionUtils.getTaskClass("  ");
    }

    @Test
    public void testTaskObjectIsSingletonPerClass() {
        Object one = TaskReflectionUtils.getTaskObject(TARGET);
        Object two = TaskReflectionUtils.getTaskObject(TARGET);
        assertSame("a task class gets one shared instance", one, two);
    }

    @Test
    public void testInvokeResolvedMethod() throws Exception {
        Object target = TaskReflectionUtils.getTaskObject(TARGET);
        Method method = TaskReflectionUtils.getTaskMethod(TaskId.of("t"), TARGET, "execute");
        assertEquals("reflective:hi", method.invoke(target, "hi"));
        assertEquals(1, ReflectiveTarget.getCalls());
    }

    @Test
    public void testResolveAlternativeMethodName() throws Exception {
        Object target = TaskReflectionUtils.getTaskObject(TARGET);
        Method method = TaskReflectionUtils.getTaskMethod(TaskId.of("t"), TARGET, "other");
        assertEquals("other:x", method.invoke(target, "x"));
    }

    @Test
    public void testResolvePackagePrivateMethod() throws Exception {
        Object target = TaskReflectionUtils.getTaskObject(TARGET);
        Method method =
                TaskReflectionUtils.getTaskMethod(TaskId.of("t"), TARGET, "packagePrivate");
        assertEquals("package:y", method.invoke(target, "y"));
    }

    @Test(expected = TaskInvocationException.class)
    public void testMissingMethodThrows() {
        TaskReflectionUtils.getTaskMethod(TaskId.of("t"), TARGET, "noSuchMethod");
    }

    @Test
    public void testTaskClassNameOfPlainTask() {
        Task plain = new Task() {
            @Override
            public com.github.cronsmith.cron.CronExpression getCronExpression() {
                return null;
            }

            @Override
            public Object execute(String p) {
                return null;
            }
        };
        assertTrue(TaskReflectionUtils.taskClassNameOf(plain).contains("TaskReflectionUtilsTests"));
    }

    @Test
    public void testTaskClassNameOfCustomTaskUsesTargetClass() {
        Map<String, Object> record = new HashMap<>();
        record.put("taskGroup", "g");
        record.put("taskName", "n");
        record.put("taskClass", TARGET);
        record.put("cron", "0 0 12 * * ?");
        CustomTask custom =
                TaskReflectionUtils.getCustomTaskFactory().createTaskObject(record);
        assertEquals(TARGET, TaskReflectionUtils.taskClassNameOf(custom));
    }

    @Test
    public void testGetTaskObjectForCustomClassWrapsInCustomTask() {
        Map<String, Object> record = new HashMap<>();
        record.put("taskGroup", "g");
        record.put("taskName", "n");
        record.put("taskClass", TARGET);
        record.put("cron", "0 0 12 * * ?");
        // ReflectiveTarget is not a Task, so it is wrapped by the custom task factory rather than
        // used as a task itself.
        Task task = TaskReflectionUtils.getTaskObject(TARGET, record);
        assertNotNull(task);
        assertTrue(task instanceof CustomTask);
        assertEquals(TaskId.of("g", "n"), task.getTaskId());
    }

}
