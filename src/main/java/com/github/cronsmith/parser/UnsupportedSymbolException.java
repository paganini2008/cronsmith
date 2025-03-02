package com.github.cronsmith.parser;

/**
 * 
 * @Description: UnsupportedSymbolException
 * @Author: Fred Feng
 * @Date: 01/03/2025
 * @Version 1.0.0
 */
public class UnsupportedSymbolException extends CronParserException {

    private static final long serialVersionUID = -7517956571829299373L;

    public UnsupportedSymbolException(String msg) {
        super(msg);
    }
}
