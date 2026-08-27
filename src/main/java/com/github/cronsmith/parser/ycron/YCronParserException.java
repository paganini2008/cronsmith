package com.github.cronsmith.parser.ycron;

import com.github.cronsmith.parser.CronParserException;

/**
 * Thrown when a YCRON (year-based) expression cannot be parsed. Kept separate from the traditional
 * {@code CronParserException} so the two parsing paths stay fully independent.
 *
 * @Author: Fred Feng
 * @Date: 27/08/2026
 * @Version 1.0.0
 */
public class YCronParserException extends CronParserException {

    private static final long serialVersionUID = 7710035179031510632L;

    public YCronParserException(String msg) {
        super(msg);
    }

    public YCronParserException(String msg, Throwable e) {
        super(msg, e);
    }
}
