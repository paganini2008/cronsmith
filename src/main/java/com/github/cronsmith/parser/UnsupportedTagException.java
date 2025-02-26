package com.github.cronsmith.parser;

/**
 * 
 * Raising an UnsupportedTagException represents an unsupported tag while resolving a cron
 * expression string
 * 
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public class UnsupportedTagException extends CronParserException {

    private static final long serialVersionUID = -7517956571829299373L;

    public UnsupportedTagException(String msg) {
        super(msg);
    }
}
