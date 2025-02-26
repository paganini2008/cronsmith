package com.github.cronsmith;

import java.util.Iterator;

/**
 * 
 * @Description: IteratorUtils
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public abstract class IteratorUtils {

    public static <T> Iterable<T> forEach(Iterator<T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("Null iterator");
        }
        return () -> delegate;
    }

    public static <T> T getFirst(Iterator<T> it) {
        return getFirst(it, null);
    }

    public static <T> T getFirst(Iterator<T> it, T defaultValue) {
        if (it != null && it.hasNext()) {
            return it.next();
        }
        return defaultValue;
    }

    public static <T> T getFirst(Iterable<T> c) {
        return getFirst(c, null);
    }

    public static <T> T getFirst(Iterable<T> c, T defaultValue) {
        if (c == null) {
            return defaultValue;
        }
        return getFirst(c.iterator(), defaultValue);
    }

    public static <T> T getLast(Iterator<T> it) {
        return getLast(it, null);
    }

    public static <T> T getLast(Iterator<T> it, T defaultValue) {
        if (it == null || !it.hasNext()) {
            return defaultValue;
        }
        T last;
        while (true) {
            last = it.next();
            if (!it.hasNext()) {
                return last;
            }
        }
    }

    public static <T> T getLast(Iterable<T> c) {
        return getLast(c, null);
    }

    public static <T> T getLast(Iterable<T> c, T defaultValue) {
        if (c == null) {
            return defaultValue;
        }
        return getLast(c.iterator(), defaultValue);
    }

    public static <T> T get(Iterator<T> it, int index) {
        return get(it, index, null);
    }

    public static <T> T get(Iterator<T> it, int index, T defaultValue) {
        if (it == null || !it.hasNext()) {
            return defaultValue;
        }
        int i = 0;
        T find;
        while (true) {
            find = it.next();
            if (i++ == index) {
                return find;
            } else if (!it.hasNext()) {
                break;
            }
        }
        return null;
    }

}
