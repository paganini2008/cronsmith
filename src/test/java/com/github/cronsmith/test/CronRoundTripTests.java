package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronType;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.Year;

/**
 * The traditional-cron counterpart of {@code YCronParserTests#testGenerativeRoundTrip}: build a
 * large, varied population of month-based expressions with the builder, render each, parse the
 * render, and require the string to survive the round trip. Whatever {@link CronExpression#toString}
 * can emit, {@link CRON#parse} must read back to the same string.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronRoundTripTests {

    @Test
    public void testGenerativeRoundTrip() {
        Random r = new Random(20260827L);
        int rounds = 5000;
        int tested = 0;
        for (int i = 0; i < rounds; i++) {
            CronExpression built;
            try {
                built = randomCron(r);
            } catch (RuntimeException invalidCombo) {
                // An out-of-range or out-of-order builder call is not a parser concern; skip it.
                continue;
            }
            String rendered = built.toString();
            String reparsed;
            try {
                reparsed = CRON.parse(rendered).toString();
            } catch (RuntimeException parseFailure) {
                throw new AssertionError("CRON failed to parse its own render: [" + rendered + "]",
                        parseFailure);
            }
            assertEquals(rendered, reparsed, "round-trip mismatch for [" + rendered + "]");
            assertEquals(CronType.CRON, built.getCronType(), "[" + rendered + "]");
            tested++;
        }
        assertTrue(tested > rounds / 2, "too few valid samples exercised: " + tested);
    }

    private static CronExpression randomCron(Random r) {
        CronBuilder b = new CronBuilder();
        Month month = randomMonth(randomYear(b, r), r);
        Day date = r.nextBoolean() ? randomDayOfMonth(month, r) : randomDayOfWeek(month, r);
        return withTime(date, r);
    }

    private static Year randomYear(CronBuilder b, Random r) {
        int y = 2020 + r.nextInt(70);
        switch (r.nextInt(4)) {
            case 0:
                return b.everyYear();
            case 1:
                return b.year(y);
            case 2:
                return b.year(y).toYear(y + 1 + r.nextInt(5));
            default:
                return b.everyYear(1 + r.nextInt(3));
        }
    }

    private static Month randomMonth(Year year, Random r) {
        int mo = 1 + r.nextInt(12);
        switch (r.nextInt(5)) {
            case 0:
                return year.everyMonth();
            case 1:
                return year.everyMonth(1 + r.nextInt(4));
            case 2:
                return year.month(mo);
            case 3:
                if (mo >= 12) {
                    return year.month(mo);
                }
                return year.month(mo).toMonth(mo + 1 + r.nextInt(12 - mo), 1 + r.nextInt(3));
            default:
                if (mo >= 12) {
                    return year.month(mo);
                }
                return year.month(mo).andMonth(mo + 1 + r.nextInt(12 - mo));
        }
    }

    private static Day randomDayOfMonth(Month month, Random r) {
        int d = 1 + r.nextInt(27);
        int d2 = d + 1 + r.nextInt(Math.max(1, 28 - d));
        int step = 1 + r.nextInt(5);
        switch (r.nextInt(14)) {
            case 0:
                return month.day(d);
            case 1:
                return month.day(d).toDay(d2, step);
            case 2:
                return month.day(d).andDay(d2);
            case 3:
                return month.day(d).andLastDay();
            case 4:
                return month.day(d).andLastDay(1 + r.nextInt(10));
            case 5:
                return month.day(d).andLastWeekday();
            case 6:
                return month.day(d).andLatestWeekday(d2);
            case 7:
                return month.day(d).toLastDay(step);
            case 8:
                return month.day(d).toLastWeekday(step); // renders "d-LW/step"
            case 9:
                return month.day(d).toLatestWeekday(d2, step); // renders "d-d2W/step"
            case 10:
                return month.lastDay();
            case 11:
                return month.lastDay(1 + r.nextInt(10));
            case 12:
                return month.lastWeekday();
            default:
                return month.latestWeekday(d);
        }
    }

    private static Day randomDayOfWeek(Month month, Random r) {
        int dow = 1 + r.nextInt(7);
        int step = 1 + r.nextInt(3);
        int w = 1 + r.nextInt(4);
        switch (r.nextInt(6)) {
            case 0:
                return month.everyWeek().day(dow);
            case 1:
                return month.everyWeek().everyDay();
            case 2:
                return month.everyWeek().everyWeekday();
            case 3:
                if (dow >= 7) {
                    return month.everyWeek().day(dow);
                }
                return month.everyWeek().day(dow).toDay(dow + 1 + r.nextInt(7 - dow), step);
            case 4:
                return month.dayOfWeek(w, dow);
            default:
                return month.lastDayOfWeek(dow);
        }
    }

    private static CronExpression withTime(Day day, Random r) {
        int h = r.nextInt(24), m = r.nextInt(60), s = r.nextInt(60);
        switch (r.nextInt(4)) {
            case 0:
                return day.at(h, m, s);
            case 1:
                return day.everyHour().minute(m).second(s);
            case 2:
                if (h >= 23) {
                    return day.at(h, m, s);
                }
                return day.hour(h).toHour(h + 1 + r.nextInt(23 - h), 1 + r.nextInt(3)).minute(m)
                        .second(s);
            default:
                if (m >= 59) {
                    return day.at(h, m, s);
                }
                return day.hour(h).minute(m).toMinute(m + 1 + r.nextInt(59 - m), 1 + r.nextInt(5))
                        .second(s);
        }
    }

}
