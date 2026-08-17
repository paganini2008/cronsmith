package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.time.LocalDateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.parser.CronParserException;

/**
 *
 * Round-trip tests for the parser: every expression below is parsed and printed again, and the
 * printed form has to match the input character for character.
 * <p>
 * Expressions carrying a year field are assembled from {@link CronTestSupport#currentYear()},
 * because the parser builds on a {@code CronBuilder} whose start time is <em>now</em> and which
 * rejects any year in the past.
 *
 * @Description: CronExpressionParserTests
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CronExpressionParserTests {

    private static final int Y = CronTestSupport.currentYear();

    /** Parses the expression and asserts it prints back exactly as it went in. */
    private static CronExpression assertRoundTrip(String cron) {
        CronExpression cronExpression = CRON.parse(cron);
        assertEquals(cron, cronExpression.toString());
        return cronExpression;
    }

    @Test
    public void testA() {
        assertRoundTrip("0 0 12 * * ?");
    }

    @Test
    public void testB() {
        assertRoundTrip("0 15 10 ? * MON-FRI");
    }

    @Test
    public void testC() {
        assertRoundTrip("0 10,20,30 9-17 1,5,L * ?");
    }

    @Test
    public void testD() {
        // '7L' is the last Saturday under cron numbering. Mixing names and numbers in one field
        // makes the last one written decide how the whole field prints, hence the numeric form.
        assertEquals("1,3,5,7,9 3-30/3 12-16 ? * 3#1,4#2,7L",
                CRON.parse("1,3,5,7,9 3-30/3 12-16 ? * TUE#1,WED#2,7L").toString());
    }

    @Test
    public void testE() {
        assertRoundTrip("0 0 6 ? 1-5 MON,WED,FRI");
    }

    @Test
    public void testF() {
        assertRoundTrip("*/5 1 12,15,18-22 10,15W,LW * ? " + Y);
    }

    @Test
    public void testG() {
        // 1-5 is SUN-THU, which wraps once mapped onto the week, so it prints as a list.
        assertEquals("0 1 0-12,15,16-22/2 ? * 1,2,3,4,5 " + Y + "-" + (Y + 3),
                CRON.parse("0 1 0-12,15,16-22/2 ? * 1-5 " + Y + "-" + (Y + 3)).toString());
    }

    @Test
    public void testH() {
        assertRoundTrip("0 1,5,7,13,29,45 12/2 5,15W,25W,L-1 APR-NOV ?");
    }

    @Test
    public void testI() {
        assertRoundTrip("5/1 */5 12 ? 1-9 3#1,5#2,4#3,6L");
    }

    @Test
    public void testJ() {
        assertRoundTrip("*/5 1,3,5/1 12-16 1,3,20,LW MAR-SEP ? " + (Y + 1) + "/1");
    }

    @Test
    public void testK() {
        assertRoundTrip("5-30/7 0-12/3,15-45/2 2,3,4-17/2 ? JAN-JUL MON-THU/2 " + Y + "-" + (Y + 8));
    }

    @Test
    public void testL() {
        assertEquals(
                "0,15,30,45/1 */10 0-12/3 ? MAR,JUL,SEP 7#1,5#2,5L " + Y + "," + (Y + 1) + ","
                        + (Y + 5) + "/2",
                CRON.parse("0,15,30,45/1 */10 0-12/3 ? MAR,JUL,SEP SAT#1,THU#2,5L " + Y + ","
                        + (Y + 1) + "," + (Y + 5) + "/2").toString());
    }

    @Test
    public void testM() {
        assertRoundTrip("*/2 0 12 1-10,13-22/2,L */2 ? " + Y + "/2");
    }

    @Test
    public void testN() {
        assertRoundTrip("10,20,30 0 12 7W,L */2 ? " + Y + "/3");
    }

    // ------------------------------------------------------------------ //
    // One test per tag, field by field                                   //
    // ------------------------------------------------------------------ //

    @Test
    public void testAsteriskInEveryField() {
        assertRoundTrip("* * * * * ?");
        assertRoundTrip("0 0 0 * * ?");
    }

    @Test
    public void testSlashInEveryField() {
        assertRoundTrip("*/2 */3 */4 */5 */6 ?");
        // A month starting at 5 is normalized to the explicit range it stands for.
        assertEquals("5/2 5/3 5/4 5/5 5-12/6 ?", CRON.parse("5/2 5/3 5/4 5/5 5/6 ?").toString());
    }

    @Test
    public void testHyphenInEveryField() {
        assertRoundTrip("1-30 1-30 1-12 1-28 1-6 ?");
        assertRoundTrip("1-30/2 1-30/3 1-12/4 1-28/5 1-6/2 ?");
    }

    @Test
    public void testCommaInEveryField() {
        assertEquals("1,2,3 4,5,6 7,8,9 10,11,12 JAN,FEB,MAR ?",
                CRON.parse("1,2,3 4,5,6 7,8,9 10,11,12 1,2,3 ?").toString());
        assertRoundTrip("1,2,3 4,5,6 7,8,9 10,11,12 JAN,FEB,MAR ?");
    }

    @Test
    public void testNumericMonthAndDayOfWeekArePrintedAsAbbreviations() {
        // Listing months or weekdays by number is accepted, but the printer falls back to its
        // default vocabulary; only the '/' and '-' forms keep the numeric notation.
        assertEquals("0 0 12 ? JAN,MAR,MAY SUN,TUE,THU",
                CRON.parse("0 0 12 ? 1,3,5 1,3,5").toString());
        assertEquals("0 0 12 1 JAN ?", CRON.parse("0 0 12 1 1 ?").toString());
        assertRoundTrip("0 0 12 1 1-5 ?");
    }

    @Test
    public void testMonthAbbreviations() {
        assertRoundTrip("0 0 12 1 JAN,FEB,MAR,APR,MAY,JUN,JUL,AUG,SEP,OCT,NOV,DEC ?");
    }

    @Test
    public void testDayOfWeekAbbreviations() {
        assertRoundTrip("0 0 12 ? * SUN,MON,TUE,WED,THU,FRI,SAT");
    }

    @Test
    public void testLastDayOfMonthTags() {
        assertRoundTrip("0 0 12 L * ?");
        assertRoundTrip("0 0 12 L-3 * ?");
        assertRoundTrip("0 0 12 LW * ?");
    }

    @Test
    public void testWeekdayTag() {
        assertRoundTrip("0 0 12 1W * ?");
        assertRoundTrip("0 0 12 15W * ?");
        assertRoundTrip("0 0 12 1W,15W,LW * ?");
    }

    @Test
    public void testHashAndLastDayOfWeekTags() {
        assertRoundTrip("0 0 12 ? * MON#1");
        assertRoundTrip("0 0 12 ? * MON#1,FRI#3");
        assertRoundTrip("0 0 12 ? * MON#1,TUE#2,WED#3,THU#4,FRI#5");
        assertRoundTrip("0 0 12 ? * MONL");
        assertRoundTrip("0 0 12 ? * MONL,FRIL");
        // 'L' combined with '#' entries, which is the extended multi-value form.
        assertRoundTrip("1,3,5,7,9 3-30/3 12-16 ? * 3#1,4#2,7L");
        assertRoundTrip("5/1 */5 12 ? 1-9 3#1,5#2,4#3,6L");
    }

    @Test
    public void testYearTags() {
        assertRoundTrip("0 0 12 * * ? " + Y);
        assertRoundTrip("0 0 12 * * ? " + Y + "-" + (Y + 5));
        assertRoundTrip("0 0 12 * * ? " + Y + "-" + (Y + 5) + "/2");
        assertRoundTrip("0 0 12 * * ? " + Y + "/3");
        assertRoundTrip("0 0 12 * * ? " + Y + "," + (Y + 2) + "," + (Y + 4));
    }

    @Test
    public void testAsteriskYearIsOmittedWhenPrinted() {
        // '*' means "every year", which the printer represents by leaving the field out.
        assertEquals("0 0 12 * * ?", CRON.parse("0 0 12 * * ? *").toString());
    }

    @Test
    public void testSixFieldExpressionHasNoYear() {
        CronExpression cronExpression = assertRoundTrip("0 0 12 * * ?");
        assertNotNull(cronExpression.getNextFiredDateTime());
    }

    // ------------------------------------------------------------------ //
    // Parsed expressions have to be usable, not just printable           //
    // ------------------------------------------------------------------ //

    @Test
    public void testParsedExpressionFiresInTheFuture() {
        LocalDateTime now = CronTestSupport.now();
        for (String cron : new String[] {"0 0 12 * * ?", "*/5 * * * * ?", "0 15 10 ? * MON-FRI",
                "0 0 12 L * ?", "0 0 12 LW * ?", "0 0 12 15W * ?", "0 0 12 1-10/2 JAN-DEC/3 ?"}) {
            LocalDateTime next = CRON.parse(cron).getNextFiredDateTime(now);
            assertNotNull(cron, next);
            assertTrue(cron + " -> " + next, next.isAfter(now));
        }
    }

    @Test
    public void testParsedExpressionIsMonotonic() {
        CronExpression cronExpression = CRON.parse("0 0 12 1,10,20,L * ?");
        LocalDateTime[] previous = new LocalDateTime[1];
        cronExpression.consume(ldt -> {
            if (previous[0] != null) {
                assertTrue(previous[0] + " -> " + ldt, ldt.isAfter(previous[0]));
            }
            previous[0] = ldt;
        }, 24);
    }

    @Test
    public void testZoneIdIsCarriedByTheParsedExpression() {
        assertEquals(CronTestSupport.BUILDER_ZONE, CRON.parse("0 0 12 * * ?").getZoneId());
    }

    // ------------------------------------------------------------------ //
    // Malformed input                                                    //
    // ------------------------------------------------------------------ //

    @Test(expected = CronParserException.class)
    public void testUnknownDayOfMonthTagIsRejected() {
        CRON.parse("0 0 12 X * ?");
    }

    @Test(expected = CronParserException.class)
    public void testUnknownMonthNameIsRejected() {
        CRON.parse("0 0 12 1 XYZ ?");
    }

    @Test(expected = CronParserException.class)
    public void testUnknownDayOfWeekNameIsRejected() {
        CRON.parse("0 0 12 ? * XYZ");
    }

    @Test(expected = RuntimeException.class)
    public void testEmptyExpressionIsRejected() {
        CRON.parse("");
    }

    @Test(expected = RuntimeException.class)
    public void testGarbageIsRejected() {
        CRON.parse("not a cron expression at all");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPastYearIsRejected() {
        CRON.parse("0 0 12 * * ? " + (Y - 1));
    }

    @Test
    public void testPrintParseTreeReturnsTheInput() {
        String cron = "0 0 12 * * ?";
        assertEquals(cron, CRON.printParseTree(cron));
    }

}
