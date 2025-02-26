
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheMinute
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public interface TheMinute extends Minute {

    TheMinute andMinute(int minute);

    default TheMinute toMinute(int minute) {
        return toMinute(minute, 1);
    }

    TheMinute toMinute(int minute, int interval);

}
