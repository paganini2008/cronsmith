
package com.github.cronsmith.cron;

import java.util.Iterator;

/**
 * 
 * @Description: Second
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public interface Second extends Iterator<Second>, CronExpression {

    int getYear();

    int getMonth();

    int getDay();

    int getHour();

    int getMinute();

    int getSecond();

}
