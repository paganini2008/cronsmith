package com.github.cronsmith.extension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 
 * Rebuilds a stored task and calls it reflectively on a shared instance of its class.
 * 
 * @Description: DefaultCustomTaskFactory
 * @Author: Fred Feng
 * @Date: 25/04/2025
 * @Version 1.0.0
 */
public class DefaultCustomTaskFactory implements CustomTaskFactory {

    @Override
    public CustomTask createTaskObject(Map<String, Object> record) {
        return new DefaultCustomTask(record);
    }

    /**
     * 
     * @Description: DefaultCustomTask
     * @Author: Fred Feng
     * @Date: 25/04/2025
     * @Version 1.0.0
     */
    static class DefaultCustomTask extends AbstractCustomTask {

        DefaultCustomTask(Map<String, Object> record) {
            super(record);
        }

        @Override
        protected Object invokeTaskMethod(TaskId taskId, String taskClassName,
                String taskMethodName, String initialParameter) {
            Object taskObject = TaskReflectionUtils.getTaskObject(taskClassName);
            Method method =
                    TaskReflectionUtils.getTaskMethod(taskId, taskClassName, taskMethodName);
            try {
                return method.invoke(taskObject, initialParameter);
            } catch (InvocationTargetException e) {
                // Report what the task threw, not the reflection wrapper around it.
                throw new TaskInvocationException(e.getTargetException().getMessage(),
                        e.getTargetException());
            } catch (ReflectiveOperationException | RuntimeException e) {
                throw new TaskInvocationException(e.getMessage(), e);
            }
        }

    }

}
