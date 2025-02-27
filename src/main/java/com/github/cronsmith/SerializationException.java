package com.github.cronsmith;

/**
 * 
 * @Description: SerializationException
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = -6834736422931573649L;

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(String msg, Throwable e) {
        super(msg, e);
    }

    public SerializationException(Throwable e) {
        super(e);
    }

}
