package com.github.cronsmith.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 
 * Helpers for reporting and unwrapping throwables.
 * 
 * @Description: ExceptionUtils
 * @Author: Fred Feng
 * @Date: 14/04/2025
 * @Version 1.0.0
 */
public abstract class ExceptionUtils {

    /**
     * The stack trace as individual lines, with tabs expanded and blank lines dropped, ready to be
     * stored one row at a time or rendered in a UI.
     */
    public static String[] toArray(Throwable e) {
        if (e == null) {
            return new String[0];
        }
        List<String> results = new ArrayList<String>();
        for (String line : toString(e).split("\\R")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            results.add(line.replace("\t", "    "));
        }
        return results.toArray(new String[0]);
    }

    /**
     * The stack trace as a single string.
     */
    public static String toString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter out = new StringWriter();
        try (PrintWriter writer = new PrintWriter(out)) {
            e.printStackTrace(writer);
        }
        return out.toString();
    }

    /**
     * Whether the throwable is an instance of any of the given types.
     */
    public static boolean ignoreException(Throwable e,
            Class<? extends Throwable>[] exceptionClasses) {
        if (e == null || exceptionClasses == null || exceptionClasses.length == 0) {
            return false;
        }
        for (Class<?> exceptionClass : exceptionClasses) {
            if (exceptionClass.isAssignableFrom(e.getClass())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Strips the wrappers that reflection and the executor framework put around a failure, so that
     * callers report what the task actually threw instead of the plumbing around it. Unwrapping
     * repeats, because a task invoked reflectively inside a Future arrives wrapped twice.
     */
    public static Throwable getOriginalException(Throwable e) {
        Throwable current = e;
        while (current != null) {
            Throwable cause;
            if (current instanceof InvocationTargetException) {
                cause = ((InvocationTargetException) current).getTargetException();
            } else if (current instanceof UndeclaredThrowableException) {
                cause = ((UndeclaredThrowableException) current).getUndeclaredThrowable();
            } else if (current instanceof ExecutionException) {
                cause = current.getCause();
            } else {
                return current;
            }
            if (cause == null || cause == current) {
                return current;
            }
            current = cause;
        }
        return e;
    }

}
