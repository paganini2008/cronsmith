package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.parser.AsteriskTagVisitor;
import com.github.cronsmith.parser.CommaTagVisitor;
import com.github.cronsmith.parser.CronExpressionContext;
import com.github.cronsmith.parser.HashTagVisitor;
import com.github.cronsmith.parser.HyphenTagVisitor;
import com.github.cronsmith.parser.IgnoredTagVistor;
import com.github.cronsmith.parser.LastTagVisitor;
import com.github.cronsmith.parser.NumberTagVisitor;
import com.github.cronsmith.parser.SlashTagVisitor;
import com.github.cronsmith.parser.TagVisitor;
import com.github.cronsmith.parser.TextTagVisitor;
import com.github.cronsmith.parser.UnsupportedTagException;
import com.github.cronsmith.parser.WeekdayVisitor;

/**
 *
 * The tag visitors are driven directly here rather than through a whole expression. That reaches
 * the branches a full parse never does - a visitor asked to start an expression from scratch, and a
 * visitor handed a tag it does not own with nobody left in the chain to pass it on to.
 *
 * @Author: Fred Feng
 * @Version 1.0.0
 */
public class CronTagVisitorTests {

    private static CronExpressionContext context() {
        return new CronExpressionContext();
    }

    @Test
    public void testVisitorsAreOrdered() {
        // The chain is assembled in ascending order, so the '*' visitor sees a tag first and the
        // ',' visitor - which splits a list and feeds the pieces back in - comes last.
        assertEquals(0, new AsteriskTagVisitor().getOrder());
        assertEquals(1, new IgnoredTagVistor().getOrder());
        assertEquals(2, new NumberTagVisitor().getOrder());
        assertEquals(3, new TextTagVisitor().getOrder());
        assertEquals(Integer.MAX_VALUE, new CommaTagVisitor().getOrder());
        assertTrue(new HyphenTagVisitor().getOrder() > 3);
        assertTrue(new SlashTagVisitor().getOrder() > 3);
        assertTrue(new LastTagVisitor().getOrder() > 3);
        assertTrue(new WeekdayVisitor().getOrder() > 3);
        assertTrue(new HashTagVisitor().getOrder() > 3);
    }

    @Test
    public void testVisitorTags() {
        assertEquals("*", new AsteriskTagVisitor().getTag());
        assertEquals("?", new IgnoredTagVistor().getTag());
        assertEquals(",", new CommaTagVisitor().getTag());
        assertEquals("-", new HyphenTagVisitor().getTag());
        assertEquals("/", new SlashTagVisitor().getTag());
        assertEquals("L", new LastTagVisitor().getTag());
        assertEquals("W", new WeekdayVisitor().getTag());
        assertEquals("#", new HashTagVisitor().getTag());
        assertEquals("", new NumberTagVisitor().getTag());
        assertEquals("", new TextTagVisitor().getTag());
    }

    // ------------------------------------------------------------------ //
    // Starting an expression from scratch                                //
    // ------------------------------------------------------------------ //

    @Test
    public void testAsteriskStartsAFreshExpressionInEveryField() {
        AsteriskTagVisitor visitor = new AsteriskTagVisitor();
        assertNotNull(visitor.visitSecond("*", null, context()));
        assertNotNull(visitor.visitMinute("*", null, context()));
        assertNotNull(visitor.visitHour("*", null, context()));
        assertNotNull(visitor.visitDayOfMonth("*", null, context()));
        assertNotNull(visitor.visitMonth("*", null, context()));
        assertNotNull(visitor.visitDayOfWeek("*", null, context()));
        assertNotNull(visitor.visitYear("*", null, context()));
    }

    @Test
    public void testNumberStartsAFreshExpressionInEveryField() {
        NumberTagVisitor visitor = new NumberTagVisitor();
        assertNotNull(visitor.visitSecond("30", null, context()));
        assertNotNull(visitor.visitMinute("30", null, context()));
        assertNotNull(visitor.visitHour("12", null, context()));
        assertNotNull(visitor.visitDayOfMonth("15", null, context()));
        assertNotNull(visitor.visitMonth("6", null, context()));
        assertNotNull(visitor.visitDayOfWeek("3", null, context()));
        assertNotNull(visitor.visitYear(String.valueOf(CronTestSupport.currentYear()), null, context()));
    }

    @Test
    public void testTextStartsAFreshExpressionForNamedFields() {
        TextTagVisitor visitor = new TextTagVisitor();
        assertNotNull(visitor.visitMonth("MAR", null, context()));
        assertNotNull(visitor.visitDayOfWeek("WED", null, context()));
    }

    @Test
    public void testSlashStartsAFreshExpressionInEveryField() {
        SlashTagVisitor visitor = new SlashTagVisitor();
        assertNotNull(visitor.visitSecond("*/5", null, context()));
        assertNotNull(visitor.visitMinute("*/5", null, context()));
        assertNotNull(visitor.visitHour("*/5", null, context()));
        assertNotNull(visitor.visitDayOfMonth("*/5", null, context()));
        assertNotNull(visitor.visitMonth("*/3", null, context()));
        assertNotNull(visitor.visitDayOfWeek("*/2", null, context()));
        assertNotNull(visitor.visitYear(CronTestSupport.currentYear() + "/2", null, context()));
    }

    @Test
    public void testHyphenStartsAFreshExpressionInEveryField() {
        HyphenTagVisitor visitor = new HyphenTagVisitor();
        assertNotNull(visitor.visitSecond("10-20", null, context()));
        assertNotNull(visitor.visitMinute("10-20", null, context()));
        assertNotNull(visitor.visitHour("8-18", null, context()));
        assertNotNull(visitor.visitDayOfMonth("5-25", null, context()));
        assertNotNull(visitor.visitMonth("3-9", null, context()));
        assertNotNull(visitor.visitDayOfWeek("2-6", null, context()));
        assertNotNull(visitor.visitYear(
                CronTestSupport.currentYear() + "-" + CronTestSupport.year(4), null, context()));
    }

    @Test
    public void testLastAndWeekdayAndHashStartFreshExpressions() {
        assertNotNull(new LastTagVisitor().visitDayOfMonth("L", null, context()));
        assertNotNull(new LastTagVisitor().visitDayOfMonth("L-3", null, context()));
        assertNotNull(new LastTagVisitor().visitDayOfWeek("5L", null, context()));
        assertNotNull(new WeekdayVisitor().visitDayOfMonth("15W", null, context()));
        // 'LW' belongs to the last-day visitor, not the weekday one.
        assertNotNull(new LastTagVisitor().visitDayOfMonth("LW", null, context()));
        assertNotNull(new HashTagVisitor().visitDayOfWeek("3#2", null, context()));
    }

    @Test
    public void testCommaSplitsAndFeedsBackIntoTheChain() {
        // The comma visitor delegates each piece to the head of the chain, so it needs a real one.
        CronExpressionContext context = context();
        CronExpression cronExpression =
                context.getTagVisitor().visitSecond("5,10,15", null, context);
        assertNotNull(cronExpression);
        assertEquals("5,10,15", cronExpression.toCronString());
    }

    @Test
    public void testQuestionMarkYieldsWhateverWasBuiltSoFar() {
        IgnoredTagVistor visitor = new IgnoredTagVistor();
        CronExpressionContext context = context();
        assertNull(visitor.visitDayOfMonth("?", null, context));
        assertNull(visitor.visitDayOfWeek("?", null, context));
    }

    // ------------------------------------------------------------------ //
    // A tag nobody in the chain owns                                     //
    // ------------------------------------------------------------------ //

    @Test
    public void testUnownedTagsAreReported() {
        assertRejects(new AsteriskTagVisitor(), "5");
        assertRejects(new IgnoredTagVistor(), "5");
        assertRejects(new NumberTagVisitor(), "not-a-number");
        assertRejects(new TextTagVisitor(), "not-a-name");
        assertRejects(new HyphenTagVisitor(), "5");
        assertRejects(new SlashTagVisitor(), "5");
        assertRejects(new LastTagVisitor(), "5");
        assertRejects(new WeekdayVisitor(), "5");
        assertRejects(new HashTagVisitor(), "5");
        assertRejects(new CommaTagVisitor(), "5");
    }

    /** A visitor at the end of the chain has to raise rather than quietly return null. */
    private static void assertRejects(TagVisitor visitor, String text) {
        String name = visitor.getClass().getSimpleName();
        assertRejects(name, () -> visitor.visitSecond(text, null, context()));
        assertRejects(name, () -> visitor.visitMinute(text, null, context()));
        assertRejects(name, () -> visitor.visitHour(text, null, context()));
        assertRejects(name, () -> visitor.visitDayOfMonth(text, null, context()));
        assertRejects(name, () -> visitor.visitMonth(text, null, context()));
        assertRejects(name, () -> visitor.visitDayOfWeek(text, null, context()));
        assertRejects(name, () -> visitor.visitYear(text, null, context()));
    }

    private static void assertRejects(String name, java.util.function.Supplier<?> call) {
        try {
            call.get();
            fail(name + " accepted a tag it does not own");
        } catch (UnsupportedTagException e) {
            // expected
        }
    }

}
