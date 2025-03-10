
package com.github.cronsmith.cron;

import java.util.Iterator;

/**
 * 
 * @Description: Minute
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public interface Minute extends Iterator<Minute>, CronExpression {

    int getYear();

    int getMonth();

    int getDay();

    int getHour();

    int getMinute();

    default Second everySecond() {
        return everySecond(1);
    }

    default Second everySecond(int interval) {
        return everySecond(0, interval);
    }

    default Second everySecond(int from, int interval) {
        return everySecond(m -> from, interval);
    }

    TheSecond second(int second);

    Second everySecond(IntFunction<Minute> from, int interval);

}
