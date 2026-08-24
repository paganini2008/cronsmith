package com.github.cronsmith.extension;

/**
 * 
 * A task whose body lives somewhere else: another class to be called reflectively, or an HTTP
 * endpoint. What identifies the work is stored as data rather than compiled in, which is what lets
 * a task be restored from a database row.
 * 
 * @Description: CustomTask
 * @Author: Fred Feng
 * @Date: 12/04/2025
 * @Version 1.0.0
 */
public interface CustomTask extends Task {

    /** The class that carries the real body. */
    String getTaskClassName();

    /** The method on that class, or {@link Task#DEFAULT_METHOD_NAME}. */
    String getTaskMethodName();

    /** The endpoint to call, for tasks whose work is an HTTP request. */
    String getUrl();

}
