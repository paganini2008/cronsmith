package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Random;
import org.junit.Test;
import com.github.cronsmith.YCRON;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronType;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;
import com.github.cronsmith.parser.ycron.YCronParserException;

/**
 * Round-trips the year-based (YCRON) parser against its renderer: {@code parse(render(x))} must read
 * back to the same string. Uses its own grammar and parser, entirely apart from the traditional
 * cron path.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class YCronParserTests {

    /** Every string here is already in canonical (rendered) form, so it must round-trip verbatim. */
    private static final String[] ROUND_TRIPS = {
            // day-of-year, standalone
            "0 0 12 ? ? 100", "0 0 12 ? ? 100 2026", "0 0 0 ? ? 100-200/10", "0 0 0 ? ? 100,L",
            "0 0 0 ? ? 100,200W", "0 0 0 ? ? 100,LW", "0 0 0 ? ? 100,L-3", "0 0 0 ? ? 100-L",
            "0 0 0 ? ? 100-LW", "0 0 0 ? ? 100-200W",
            // day-of-week + week-of-year
            "0 0 9 MON 20 ?", "0 0 18 FRI 20 ? 2026", "0 0 0 MON-FRI 20 ?", "0 0 0 MON-SUN/2 40 ?",
            "0 0 9 MON 10,12,14 ?", "0 0 9 MON 20,L ?",
            // time ranges / steps carried unchanged from traditional cron
            "0 0 8-18 ? ? 100", "0 0 8-18/2 ? ? 100", "0 0/15 12 ? ? 100", "0 30 12 ? ? 100",
            "30 0 0 ? ? 100",};

    @Test
    public void testRoundTrips() {
        for (String cron : ROUND_TRIPS) {
            CronExpression parsed = YCRON.parse(cron);
            assertEquals(cron, parsed.toString());
            assertEquals("must parse as year-based: " + cron, CronType.YCRON, parsed.getCronType());
        }
    }

    /** Whatever the builder renders as YCRON, the parser must read back to the same string. */
    @Test
    public void testBuilderRendersRoundTrip() {
        CronExpression[] built = {new CronBuilder().year(2026).day(100).at(12, 0, 0),
                new CronBuilder().everyYear().day(100).toDay(200, 10).at(0, 0, 0),
                new CronBuilder().everyYear().day(100).andLastDay().at(0, 0, 0),
                new CronBuilder().year(2026).week(20).Mon().at(9, 0, 0),
                new CronBuilder().everyYear().week(40).everyDay().at(0, 0, 0),
                new CronBuilder().everyYear().week(10).toWeek(20, 2).Mon().at(9, 0, 0)};
        for (CronExpression e : built) {
            String rendered = e.toString();
            assertEquals(rendered, YCRON.parse(rendered).toString());
        }
    }

    /** '*' in the day-of-week field is an input alias, canonicalised to MON-SUN, as in plain cron. */
    @Test
    public void testEveryDayOfWeekCanonicalises() {
        assertEquals("0 0 0 MON-SUN 40 ?", YCRON.parse("0 0 0 * 40 ?").toString());
        assertEquals("0 0 0 MON-SUN/2 40 ?", YCRON.parse("0 0 0 */2 40 ?").toString());
    }

    @Test
    public void testDayOfYearFiresOnThatDay() {
        CronExpression e = YCRON.parse("0 0 12 ? ? 100 2026");
        LocalDateTime fired = e.getNextFiredDateTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertEquals(2026, fired.getYear());
        assertEquals(100, fired.getDayOfYear());
        assertEquals(12, fired.getHour());
    }

    @Test
    public void testWeekOfYearFiresInThatWeek() {
        CronExpression e = YCRON.parse("0 0 9 MON 20 ? 2026");
        LocalDateTime fired = e.getNextFiredDateTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertEquals(DayOfWeek.MONDAY, fired.getDayOfWeek());
        assertEquals(20, fired.get(WeekFields.ISO.weekOfYear()));
    }

    @Test
    public void testDayOfYearAndWeekAreMutuallyExclusive() {
        // day-of-year set at the same time as day-of-week + week-of-year
        assertRejected("0 0 0 MON 20 100");
        // day-of-week without its week-of-year partner
        assertRejected("0 0 0 MON ? ?");
        // week-of-year without its day-of-week partner
        assertRejected("0 0 0 ? 20 ?");
    }

    @Test
    public void testTraditionalCronIsNotAYCronLine() {
        // A month-based line has '*' where YCRON wants a week-of-year, so the grammar rejects it.
        assertRejected("0 0 12 * * ?");
    }

    @Test
    public void testEmptyIsRejected() {
        assertRejected("");
        assertRejected("   ");
    }

    /** Every year-field shape the renderer can emit must read back verbatim. */
    @Test
    public void testYearFieldForms() {
        CronExpression[] built = {new CronBuilder().year(2026).toYear(2028).day(100).at(12, 0, 0),
                new CronBuilder().year(2026).toYear(2030, 2).day(100).at(12, 0, 0),
                new CronBuilder().everyYear(2).day(100).at(12, 0, 0),
                new CronBuilder().year(2026).andYear(2028).day(100).at(12, 0, 0)};
        for (CronExpression e : built) {
            assertEquals(e.toString(), YCRON.parse(e.toString()).toString());
        }
        // '*' as the year means every year, so it is dropped on the way back out.
        assertEquals("0 0 12 ? ? 100", YCRON.parse("0 0 12 ? ? 100 *").toString());
        // '*/2' is an input alias, canonicalised to the fixed-start form.
        assertStable("0 0 12 ? ? 100 */2");
    }

    /** Second / minute / hour carry the full traditional vocabulary; they must round-trip. */
    @Test
    public void testTimeFieldForms() {
        CronExpression[] built =
                {new CronBuilder().everyYear().day(100).everyHour().minute(0).second(0),
                        new CronBuilder().everyYear().day(100).hour(12).everyMinute().second(0),
                        new CronBuilder().everyYear().day(100).hour(12).minute(0).everySecond(),
                        new CronBuilder().everyYear().day(100).everyHour(0, 2).minute(0).second(0),
                        new CronBuilder().everyYear().day(100).hour(8).toHour(18, 2).minute(0)
                                .second(0),
                        new CronBuilder().everyYear().day(100).hour(12).minute(0).toMinute(30, 5)
                                .second(0),
                        new CronBuilder().everyYear().day(100).hour(12).minute(0).second(0)
                                .toSecond(30, 10)};
        for (CronExpression e : built) {
            assertEquals(e.toString(), YCRON.parse(e.toString()).toString());
        }
    }

    /** Numeric day-of-week and week-of-year ranges are accepted; parsing is idempotent. */
    @Test
    public void testNumericAndRangeInputAliases() {
        assertStable("0 0 9 2 20 ?");
        assertStable("0 0 9 2-5 20 ?");
        assertStable("0 0 9 2/2 20 ?");
        assertStable("0 0 9 MON 10-14/2 ?");
    }

    /**
     * A last-day / last-week marker cannot stand alone in a YCRON field: that schedule is a fixed
     * December date, which is plain cron, not YCRON.
     */
    @Test
    public void testStandaloneLastMarkersRejected() {
        assertRejected("0 0 0 ? ? L");
        assertRejected("0 0 9 MON L ?");
    }

    /** A few more grammar-valid shapes across the remaining field branches. */
    @Test
    public void testMoreInputAliases() {
        assertStable("0 0 9 MON/2 20 ?"); // day-of-week name with step
        assertStable("0 0 8/2 ? ? 100"); // hour step
        assertStable("0 0 9 MON 10-14 ?"); // week-of-year plain range
    }

    /**
     * The grammar admits a stepped day-of-year in the star form, but "every nth day of the year"
     * has no builder to back it, so the driver rejects it rather than pretend.
     */
    @Test
    public void testEveryNthDayOfYearRejected() {
        assertRejected("0 0 0 ? ? */2");
    }

    /**
     * The real safety net: build a large, varied population of YCRON expressions with the builder,
     * render each, parse the render, and require the string to survive the round trip. Whatever the
     * renderer can emit, the parser must read back - so a new render form that the parser forgot
     * shows up here immediately. Seeded, so any failure is reproducible and prints the offender.
     */
    @Test
    public void testGenerativeRoundTrip() {
        Random r = new Random(20260827L);
        int rounds = 5000;
        int tested = 0;
        for (int i = 0; i < rounds; i++) {
            CronExpression built;
            try {
                built = randomYcron(r);
            } catch (RuntimeException invalidCombo) {
                // An out-of-range or out-of-order builder call is not a parser concern; skip it.
                continue;
            }
            String rendered = built.toString();
            String reparsed;
            try {
                reparsed = YCRON.parse(rendered).toString();
            } catch (RuntimeException parseFailure) {
                throw new AssertionError("YCRON failed to parse its own render: [" + rendered + "]",
                        parseFailure);
            }
            assertEquals("round-trip mismatch for [" + rendered + "]", rendered, reparsed);
            assertEquals("[" + rendered + "]", CronType.YCRON, built.getCronType());
            tested++;
        }
        // Guard against the generator silently degenerating to almost nothing.
        assertTrue("too few valid samples exercised: " + tested, tested > rounds / 2);
    }

    private static CronExpression randomYcron(Random r) {
        CronBuilder b = new CronBuilder();
        Year year = randomYear(b, r);
        Day date = r.nextBoolean() ? randomDayOfYear(year, r)
                : randomDayOfWeek(randomWeekOfYear(year, r), r);
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

    private static Day randomDayOfYear(Year year, Random r) {
        int d = 1 + r.nextInt(300);
        int d2 = d + 1 + r.nextInt(60);
        int step = 1 + r.nextInt(5);
        switch (r.nextInt(10)) {
            case 0:
                return year.day(d);
            case 1:
                return year.day(d).toDay(d2, step);
            case 2:
                return year.day(d).andDay(d2);
            case 3:
                return year.day(d).andLastDay();
            case 4:
                return year.day(d).andLastDay(1 + r.nextInt(10));
            case 5:
                return year.day(d).andLastWeekday();
            case 6:
                return year.day(d).andLatestWeekday(d2);
            case 7:
                return year.day(d).toLastDay(step);
            case 8:
                return year.day(d).toLastWeekday(step);
            default:
                return year.day(d).toLatestWeekday(d2, step);
        }
    }

    private static Week randomWeekOfYear(Year year, Random r) {
        int w = 1 + r.nextInt(45);
        int w2 = w + 1 + r.nextInt(Math.max(1, 52 - w));
        int step = 1 + r.nextInt(4);
        switch (r.nextInt(4)) {
            case 0:
                return year.week(w);
            case 1:
                return year.week(w).toWeek(w2, step);
            case 2:
                return year.week(w).andWeek(w2);
            default:
                return year.week(w).andLastWeek();
        }
    }

    private static Day randomDayOfWeek(Week week, Random r) {
        int k = 1 + r.nextInt(7);
        int step = 1 + r.nextInt(3);
        switch (r.nextInt(5)) {
            case 0:
                return week.day(k);
            case 1:
                return week.everyDay();
            case 2:
                return week.everyDay(1, step);
            case 3:
                return week.everyWeekday();
            default:
                if (k >= 7) {
                    return week.day(k);
                }
                return week.day(k).toDay(k + 1 + r.nextInt(7 - k), step);
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

    /** Parsing the rendered form again must give the same string - a stable fixed point. */
    private static void assertStable(String cron) {
        String once = YCRON.parse(cron).toString();
        assertEquals(once, YCRON.parse(once).toString());
    }

    private static void assertRejected(String cron) {
        try {
            YCRON.parse(cron);
            fail("expected YCRON to reject: " + cron);
        } catch (YCronParserException e) {
            assertTrue(String.valueOf(e.getMessage()), e.getMessage() != null);
        }
    }

}
