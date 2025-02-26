
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheYear
 * @Author: Fred Feng
 * @Date: 02/03/2024
 * @Version 1.0.0
 */
public interface TheYear extends Year {

    TheYear andYear(int year);

    default TheYear toYear(int year) {
        return toYear(year, 1);
    }

    TheYear toYear(int year, int interval);

    default TheYear toEnd(int interval) {
        return toYear(MAX_YEAR, interval);
    }

}
