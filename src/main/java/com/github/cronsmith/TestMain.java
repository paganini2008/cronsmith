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

        // System.out.println("L-12".matches("L|LW|L\\-\\d{1,2}"));

        String repr = "((SUN|MON|TUE|WED|THU|FRI|SAT)|[1-7])(#[1-5])";
        repr = "((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)|(\\d{1,2}))\\/(\\d{1,})";
        repr = "((SUN|MON|TUE|WED|THU|FRI|SAT)|[1-7])(\\/\\d{1,})";
        repr = "(\\d+\\-\\d+)\\/(\\d+)";
        repr = "(((SUN|MON|TUE|WED|THU|FRI|SAT)\\-(SUN|MON|TUE|WED|THU|FRI|SAT))|([1-7]\\-[1-7]))(\\/\\d+)?";
        repr = "(\\d+\\-\\d+)(\\/\\d+)?";
        System.out.println("0-12/3".matches(repr));
    }

}
