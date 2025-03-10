package com.github.cronsmith.cron;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 
 * Represents a function that accepts Integer type and produces a result.
 * 
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public interface IntFunction<T> extends Function<T, Integer>, Serializable {

}
