
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheWeek
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public interface TheWeek extends Week {

    TheWeek andWeek(int weekOfMonth);

    Week andLastWeek();

    default TheWeek toWeek(int weekOfMonth) {
        return toWeek(weekOfMonth, 1);
    }

    TheWeek toWeek(int weekOfMonth, int interval);

    default Week toLastWeek() {
        return toLastWeek(1);
    }

    Week toLastWeek(int interval);
}
