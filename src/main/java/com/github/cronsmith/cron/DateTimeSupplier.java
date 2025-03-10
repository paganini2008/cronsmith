package com.github.cronsmith.cron;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * 
 * Represents a function that always produces LocalDateTime object.
 * 
 * @Author: Fred Feng
 * @Date: 03/02/2025
 * @Version 1.0.0
 */
public interface DateTimeSupplier extends Supplier<LocalDateTime>, Serializable {

}
