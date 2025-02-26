package com.github.cronsmith.cron;

import java.io.Serializable;

/**
 * 
 * @Description: Range
 * @Author: Fred Feng
 * @Date: 05/02/2025
 * @Version 1.0.0
 */
class Range<T> implements Serializable {

    private static final long serialVersionUID = 1530351762307315226L;
    private T from;
    private T to;
    private Integer interval;

    public Range(T from) {
        this.from = from;
    }

    public Range(T from, T to, Integer interval) {
        this.from = from;
        this.to = to;
        this.interval = interval;
    }

    public T getFrom() {
        return from;
    }

    public Range<T> setFrom(T from) {
        this.from = from;
        return this;
    }

    public T getTo() {
        return to;
    }

    public Range<T> setTo(T to) {
        this.to = to;
        return this;
    }

    public Integer getInterval() {
        return interval;
    }

    public Range<T> setInterval(Integer interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getFrom());
        int interval = getInterval() != null ? getInterval().intValue() : 0;
        if (getTo() != null) {
            if (interval > 1) {
                str.append("-").append(getTo()).append("/").append(interval);
            } else {
                str.append("-").append(getTo());
            }
        } else if (interval > 1) {
            str.append("/").append(interval);
        }
        return str.toString();
    }

}
