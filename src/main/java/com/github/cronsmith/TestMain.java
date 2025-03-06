package com.github.cronsmith;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestMain {


    public static int getLastWeekdayOfYear(int dayOfYear) {
        ChronoField.DAY_OF_YEAR.checkValidValue(dayOfYear);
        LocalDateTime ldt = LocalDateTime.now().withDayOfYear(dayOfYear);
        LocalDateTime nextDay;
        if (ldt.getDayOfWeek() == DayOfWeek.SATURDAY) {
            nextDay = ldt.minusDays(1);
            if (nextDay.getMonthValue() != ldt.getMonthValue()) {
                nextDay = ldt.plusDays(2);
            }
        } else if (ldt.getDayOfWeek() == DayOfWeek.SUNDAY) {
            nextDay = ldt.plusDays(1);
            if (nextDay.getMonthValue() != ldt.getMonthValue()) {
                nextDay = ldt.minusDays(2);
            }
        } else {
            nextDay = ldt;
        }
        return nextDay.getDayOfMonth();
    }

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

        repr = "L-1";
        Pattern pattern = Pattern.compile("L\\-(\\d+)");
        Matcher matcher = pattern.matcher(repr);
        if (matcher.matches()) {
            int n = Integer.parseInt(matcher.group(1));
            int lastDayOfMonth =
                    LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth()
                            - n;
            System.out.println(lastDayOfMonth);
        }
        System.out.println(LocalDateTime.now().withDayOfYear(365));
        System.out.println(getLastWeekdayOfYear(365));
    }

}
