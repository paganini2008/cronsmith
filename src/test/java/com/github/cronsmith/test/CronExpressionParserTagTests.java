package com.github.cronsmith.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import com.github.cronsmith.CRON;
import com.github.cronsmith.cron.CronExpression;

/**
 *
 * One pass per tag visitor. Each of {@code * ? , - / L W #} is fed through every field it is
 * allowed in, with and without a year, so the whole visitor chain is walked rather than just the
 * shapes the round-trip tests happen to use.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronExpressionParserTagTests {

    private static final int Y = CronTestSupport.currentYear();

    /** Parses the expression and walks a few occurrences, which is what really exercises a tag. */
    private static CronExpression assertUsable(String cron) {
        CronExpression cronExpression = CRON.parse(cron);
        assertNotNull(cronExpression, cron);
        assertNotNull(cronExpression.toString(), cron);
        LocalDateTime[] previous = new LocalDateTime[1];
        cronExpression.consume(ldt -> {
            if (previous[0] != null) {
                assertTrue(ldt.isAfter(previous[0]), cron + ": " + previous[0] + " -> " + ldt);
            }
            previous[0] = ldt;
        }, 5);
        assertNotNull(previous[0], cron + " never fired");
        return cronExpression;
    }

    // ------------------------------------------------------------------ //
    // Slash                                                              //
    // ------------------------------------------------------------------ //

    @Test
    public void testSlashInSecondsMinutesAndHours() {
        assertUsable("*/10 * * * * ?");
        assertUsable("0/30 * * * * ?");
        assertUsable("0 */10 * * * ?");
        assertUsable("0 0/15 * * * ?");
        assertUsable("0 0 */4 * * ?");
        assertUsable("0 0 0/6 * * ?");
        assertUsable("0/30 0/15 0/6 * * ?");
    }

    @Test
    public void testSlashInDayOfMonth() {
        assertUsable("0 0 12 */5 * ?");
        assertUsable("0 0 12 1/5 * ?");
        assertUsable("0 0 12 10/7 * ?");
    }

    @Test
    public void testSlashInMonth() {
        assertUsable("0 0 12 1 */3 ?");
        assertUsable("0 0 12 1 2/3 ?");
        // A month abbreviation combined with a step (FEB/3) is not part of the grammar; the
        // numeric form has to be used instead.
    }

    @Test
    public void testSlashInDayOfWeek() {
        assertUsable("0 0 12 ? * */2");
        assertUsable("0 0 12 ? * 2/2");
        // A weekday abbreviation combined with a step (MON/2) is not part of the grammar either.
    }

    @Test
    public void testSlashInYear() {
        assertUsable("0 0 12 1 1 ? " + Y + "/2");
        assertUsable("0 0 12 1 1 ? */2");
    }

    // ------------------------------------------------------------------ //
    // Hyphen                                                             //
    // ------------------------------------------------------------------ //

    @Test
    public void testHyphenInEveryField() {
        assertUsable("10-40 * * * * ?");
        assertUsable("0 10-40 * * * ?");
        assertUsable("0 0 8-18 * * ?");
        assertUsable("0 0 12 5-25 * ?");
        assertUsable("0 0 12 1 3-9 ?");
        assertUsable("0 0 12 1 MAR-SEP ?");
        assertUsable("0 0 12 ? * 2-6");
        assertUsable("0 0 12 ? * TUE-SAT");
        assertUsable("0 0 12 1 1 ? " + Y + "-" + (Y + 4));
    }

    @Test
    public void testHyphenWithAStep() {
        assertUsable("10-40/5 * * * * ?");
        assertUsable("0 10-40/5 * * * ?");
        assertUsable("0 0 8-18/2 * * ?");
        assertUsable("0 0 12 5-25/4 * ?");
        assertUsable("0 0 12 1 MAR-SEP/2 ?");
        assertUsable("0 0 12 ? * TUE-SAT/2");
        assertUsable("0 0 12 1 1 ? " + Y + "-" + (Y + 8) + "/2");
    }

    // ------------------------------------------------------------------ //
    // Comma                                                              //
    // ------------------------------------------------------------------ //

    @Test
    public void testCommaInEveryField() {
        assertUsable("5,15,25 * * * * ?");
        assertUsable("0 5,15,25 * * * ?");
        assertUsable("0 0 8,12,18 * * ?");
        assertUsable("0 0 12 1,15,28 * ?");
        assertUsable("0 0 12 1 MAR,JUN,SEP ?");
        assertUsable("0 0 12 ? * MON,WED,FRI");
        assertUsable("0 0 12 1 1 ? " + Y + "," + (Y + 2));
    }

    @Test
    public void testCommaMixedWithOtherTags() {
        assertUsable("0 0 12 1,10-20,L * ?");
        assertUsable("0 0 12 1,15W,LW * ?");
        assertUsable("0 0 12 ? * MON#1,FRI#3,7L");
        assertUsable("0 0 12 ? * MON-WED,FRI");
        assertUsable("0-10/2,20,30-40/5 * * * * ?");
    }

    // ------------------------------------------------------------------ //
    // L and W                                                            //
    // ------------------------------------------------------------------ //

    @Test
    public void testLastAndWeekdayTags() {
        assertUsable("0 0 12 L * ?");
        assertUsable("0 0 12 L-1 * ?");
        assertUsable("0 0 12 L-5 * ?");
        assertUsable("0 0 12 LW * ?");
        assertUsable("0 0 12 1W * ?");
        assertUsable("0 0 12 15W * ?");
        assertUsable("0 0 12 31W * ?");
        assertUsable("0 0 12 ? * 1L");
        assertUsable("0 0 12 ? * 7L");
    }

    @Test
    public void testHashTag() {
        for (int week = 1; week <= 5; week++) {
            for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
                assertUsable("0 0 12 ? * " + dayOfWeek + "#" + week);
            }
        }
    }

    // ------------------------------------------------------------------ //
    // Question mark                                                      //
    // ------------------------------------------------------------------ //

    @Test
    public void testQuestionMarkIsAcceptedInBothDayFields() {
        assertUsable("0 0 12 ? * MON");
        assertUsable("0 0 12 1 * ?");
    }

    // ------------------------------------------------------------------ //
    // Expressions without a leading year field                           //
    // ------------------------------------------------------------------ //

    @Test
    public void testMonthIsTheFirstFieldWhenNoYearIsGiven() {
        // Without a year the month is the first field the visitor chain sees, which is the branch
        // that has to start a fresh builder instead of extending an existing expression.
        assertEquals("0 0 12 1 MAR ?", CRON.parse("0 0 12 1 MAR ?").toString());
        assertEquals("0 0 12 1 */3 ?", CRON.parse("0 0 12 1 */3 ?").toString());
        assertUsable("0 0 12 1 3-9 ?");
        assertUsable("0 0 12 1 3,6,9 ?");
    }

    @Test
    public void testEveryFieldAsAsterisk() {
        assertUsable("* * * * * ?");
        assertUsable("* * * ? * *");
    }

}
