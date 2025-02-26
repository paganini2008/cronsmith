
package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheMonth
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public interface TheMonth extends Month {

    TheMonth andMonth(int from);

    default TheMonth toMonth(int to) {
        return toMonth(to, 1);
    }

    TheMonth toMonth(int to, int interval);

    default TheMonth toMar() {
        return toMonth(3);
    }

    default TheMonth toApr() {
        return toMonth(4);
    }

    default TheMonth toMay() {
        return toMonth(5);
    }

    default TheMonth toJune() {
        return toMonth(6);
    }

    default TheMonth toJuly() {
        return toMonth(7);
    }

    default TheMonth toAug() {
        return toMonth(8);
    }

    default TheMonth toSept() {
        return toMonth(9);
    }

    default TheMonth toOct() {
        return toMonth(10);
    }

    default TheMonth toNov() {
        return toMonth(11);
    }

    default TheMonth toDec() {
        return toMonth(12);
    }

    default TheMonth andJan() {
        return andMonth(1);
    }

    default TheMonth andFeb() {
        return andMonth(2);
    }

    default TheMonth andMar() {
        return andMonth(3);
    }

    default TheMonth andApr() {
        return andMonth(4);
    }

    default TheMonth andMay() {
        return andMonth(5);
    }

    default TheMonth andJune() {
        return andMonth(6);
    }

    default TheMonth andJuly() {
        return andMonth(7);
    }

    default TheMonth andAug() {
        return andMonth(8);
    }

    default TheMonth andSept() {
        return andMonth(9);
    }

    default TheMonth andOct() {
        return andMonth(10);
    }

    default TheMonth andNov() {
        return andMonth(11);
    }

    default TheMonth andDec() {
        return andMonth(12);
    }
}
