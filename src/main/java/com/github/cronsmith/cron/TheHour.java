
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheHour
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public interface TheHour extends Hour {

    TheHour andHour(int hourOfDay);

    default TheHour toHour(int hourOfDay) {
        return toHour(hourOfDay, 1);
    }

    TheHour toHour(int hourOfDay, int interval);

}
