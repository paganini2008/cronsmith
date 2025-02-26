
package com.github.cronsmith.cron;

import java.util.Iterator;

/**
 * 
 * @Description: Hour
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public interface Hour extends Iterator<Hour>, CronExpression {

    int getYear();

    int getMonth();

    int getDay();

    int getHour();

    default Minute everyMinute() {
        return everyMinute(1);
    }

    default Minute everyMinute(int interval) {
        return everyMinute(0, 59, interval);
    }

    default Minute everyMinute(int from, int to, int interval) {
        return everyMinute(h -> from, h -> to, interval);
    }

    TheMinute minute(int minute);

    default TheSecond at(int minute, int second) {
        return minute(minute).second(second);
    }

    Minute everyMinute(IntFunction<Hour> from, IntFunction<Hour> to, int interval);

}
