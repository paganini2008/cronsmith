package com.github.cronsmith.cron;

import java.io.Serializable;

/**
 * 
 * @Description: Range
 * @Author: Fred Feng
 * @Date: 05/03/2025
 * @Version 1.0.0
 */
class Range implements Serializable {

    private static final long serialVersionUID = 1530351762307315226L;
    private Object from;
    private Object to;
    private Integer interval;

    public Range(Object from) {
        this.from = from;
    }

    public Range(Object from, Object to, Integer interval) {
        this.from = from;
        this.to = to;
        this.interval = interval;
    }

    public Object getFrom() {
        return from;
    }

    public Range setFrom(Object from) {
        this.from = from;
        return this;
    }

    public Object getTo() {
        return to;
    }

    public Range setTo(Object to) {
        this.to = to;
        return this;
    }

    public Integer getInterval() {
        return interval;
    }

    public Range setInterval(Integer interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getFrom());
        if (getTo() != null) {
            if (getInterval() != null && getInterval() > 1) {
                str.append("-").append(getTo()).append("/").append(getInterval());
            } else {
                str.append("-").append(getTo());
            }
        }
        return str.toString();
    }

}
