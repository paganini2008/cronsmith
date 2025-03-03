package com.github.cronsmith;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

public class TestMain {

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        now = now.with(TemporalAdjusters.lastDayOfYear());
        int weekCount = now.get(WeekFields.ISO.weekOfYear());
        System.out.println(weekCount);

        LocalDateTime copy = LocalDateTime.now();
        copy = copy.with(WeekFields.ISO.weekOfYear(), weekCount);
        System.out.println(copy);

        LocalDateTime tmp = LocalDateTime.now();
        tmp.plusYears(1);
        tmp = tmp.with(TemporalAdjusters.firstDayOfYear());
        weekCount = tmp.get(WeekFields.ISO.weekOfYear());
        System.out.println(weekCount);
        tmp = tmp.with(WeekFields.ISO.weekOfYear(), weekCount);
        System.out.println(copy);
    }

}
