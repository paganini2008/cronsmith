package com.github.cronsmith.scheduler;

/**
 * 
 * @Description: CronTaskException
 * @Author: Fred Feng
 * @Date: 08/04/2025
 * @Version 1.0.0
 */
public class CronTaskException extends RuntimeException {

    private static final long serialVersionUID = 3594026796604900019L;

    public CronTaskException(String msg) {
        super(msg);
    }

    public CronTaskException(String msg, Throwable e) {
        super(msg, e);
    }

}
