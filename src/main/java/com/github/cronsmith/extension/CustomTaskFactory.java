package com.github.cronsmith.extension;

import java.util.Map;

/**
 * 
 * Turns a stored row back into a runnable task. Replace the default to control how a persisted task
 * finds its body, for instance by resolving it from a dependency injection container instead of
 * constructing it reflectively.
 * 
 * @Description: CustomTaskFactory
 * @Author: Fred Feng
 * @Date: 25/04/2025
 * @Version 1.0.0
 */
public interface CustomTaskFactory {

    CustomTask createTaskObject(Map<String, Object> record);

}
