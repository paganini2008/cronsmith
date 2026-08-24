package com.github.cronsmith.extension;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.github.cronsmith.utils.MapUtils;

/**
 * 
 * Resolves the classes, instances and methods a stored task definition names.
 * 
 * <p>
 * Everything is cached: a task firing every second would otherwise pay for a class load and a
 * method lookup on every run. The cache also gives a task class a single instance, so a task can
 * keep state across runs; this is the reason task bodies must be thread safe.
 * 
 * @Description: TaskReflectionUtils
 * @Author: Fred Feng
 * @Date: 25/04/2025
 * @Version 1.0.0
 */
public abstract class TaskReflectionUtils {

    private static final Map<String, Class<?>> taskClasses = new ConcurrentHashMap<>();
    private static final Map<String, Object> taskObjects = new ConcurrentHashMap<>();
    private static final Map<String, Method> taskMethods = new ConcurrentHashMap<>();

    private static volatile CustomTaskFactory customTaskFactory = new DefaultCustomTaskFactory();

    /**
     * Replaces the factory used to rebuild tasks from stored rows.
     */
    public static void setCustomTaskFactory(CustomTaskFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("CustomTaskFactory is required");
        }
        customTaskFactory = factory;
    }

    public static CustomTaskFactory getCustomTaskFactory() {
        return customTaskFactory;
    }

    /**
     * The task a stored row stands for. A class that implements {@link Task} directly is
     * instantiated as itself; anything else is wrapped by the custom task factory, which knows how
     * to call it.
     */
    public static Task getTaskObject(String taskClassName, Map<String, Object> record) {
        Class<?> taskClass = getTaskClass(taskClassName);
        if (!Task.class.isAssignableFrom(taskClass)
                || CustomTask.class.isAssignableFrom(taskClass)) {
            return customTaskFactory.createTaskObject(record);
        }
        return (Task) getTaskObject(taskClassName);
    }

    /**
     * The shared instance of the named class, constructed through its no-argument constructor.
     */
    public static Object getTaskObject(String taskClassName) {
        return MapUtils.getOrCreate(taskObjects, taskClassName,
                () -> doGetTaskObject(taskClassName));
    }

    private static Object doGetTaskObject(String taskClassName) {
        Class<?> taskClass = getTaskClass(taskClassName);
        try {
            java.lang.reflect.Constructor<?> constructor = taskClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new TaskInvocationException(
                    "Cannot instantiate task class: " + taskClassName + ". A public no-argument"
                            + " constructor is required.",
                    e);
        }
    }

    /**
     * The method to call for a task, cached per class and method name.
     */
    public static Method getTaskMethod(TaskId taskId, String taskClassName, String taskMethodName) {
        String key = taskClassName + "#" + taskMethodName;
        return MapUtils.getOrCreate(taskMethods, key,
                () -> doGetTaskMethod(taskClassName, taskMethodName));
    }

    private static Method doGetTaskMethod(String taskClassName, String taskMethodName) {
        Class<?> taskClass = getTaskClass(taskClassName);
        try {
            Method method = taskClass.getMethod(taskMethodName, String.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            // Fall through to the wider search below: the method may be declared on the class
            // itself without being public, or take a parameter a String can be assigned to.
        }
        for (Class<?> current = taskClass; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(taskMethodName) || method.getParameterCount() != 1
                        || Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                if (method.getParameterTypes()[0].isAssignableFrom(String.class)) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new TaskInvocationException("No method '" + taskMethodName
                + "(String)' on task class: " + taskClassName);
    }

    public static Class<?> getTaskClass(String taskClassName) {
        return MapUtils.getOrCreate(taskClasses, taskClassName,
                () -> doGetTaskClass(taskClassName));
    }

    private static Class<?> doGetTaskClass(String taskClassName) {
        if (taskClassName == null || taskClassName.trim().isEmpty()) {
            throw new TaskInvocationException("Task class name is required");
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = TaskReflectionUtils.class.getClassLoader();
        }
        try {
            return Class.forName(taskClassName, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new TaskInvocationException("No such task class: " + taskClassName, e);
        }
    }

    /**
     * The class name a task is filtered and stored by: for a custom task that is the class it
     * stands in for, rather than the wrapper's own class.
     */
    public static String taskClassNameOf(Task task) {
        return task instanceof CustomTask ? ((CustomTask) task).getTaskClassName()
                : task.getClass().getName();
    }

    /**
     * Empties every cache. Intended for tests, and for hosts that reload classes.
     */
    public static void clearCaches() {
        taskClasses.clear();
        taskObjects.clear();
        taskMethods.clear();
    }

}
