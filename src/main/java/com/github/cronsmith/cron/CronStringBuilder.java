
package com.github.cronsmith.cron;

/**
 * 
 * @Description: CronStringBuilder
 * @Author: Fred Feng
 * @Date: 02/02/2025
 * @Version 1.0.0
 */
public interface CronStringBuilder {

    default boolean supportCronString() {
        return true;
    }

    default String toCronString() {
        throw new UnsupportedOperationException();
    }

    String toString();

}
