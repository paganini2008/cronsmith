package com.github.cronsmith.parser;

/**
 * 
 * @Description: CronParserException
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public class CronParserException extends RuntimeException {

    private static final long serialVersionUID = 4589768359031510631L;

    public CronParserException(String msg) {
        super(msg);
    }

    public CronParserException(String msg, Throwable e) {
        super(msg, e);
    }

}
