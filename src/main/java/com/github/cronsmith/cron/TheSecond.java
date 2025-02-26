
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheSecond
 * @Author: Fred Feng
 * @Date: 12/02/2025
 * @Version 1.0.0
 */
public interface TheSecond extends Second {

    TheSecond andSecond(int second);

    default TheSecond toSecond(int second) {
        return toSecond(second, 1);
    }

    TheSecond toSecond(int second, int interval);

}
