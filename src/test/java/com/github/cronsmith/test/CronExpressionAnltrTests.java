package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;
import com.github.cronsmith.antlr.CronExpressionBaseVisitor;
import com.github.cronsmith.antlr.CronExpressionLexer;
import com.github.cronsmith.antlr.CronExpressionParser;
import com.github.cronsmith.antlr.CronExpressionParser.YearContext;

/**
 *
 * Grammar-level tests: each expression is tokenized, parsed and rebuilt field by field from the
 * parse tree, which checks the grammar splits the seven fields the way it is supposed to.
 *
 * @Description: CronExpressionAnltrTests
 * @Author: Fred Feng
 * @Date: 10/03/2025
 * @Version 1.0.0
 */
public class CronExpressionAnltrTests {

    private static final int Y = CronTestSupport.currentYear();

    /** Parses the expression and rebuilds it out of the parse tree, which has to match the input. */
    private static void assertParsedBackTo(String cronExpr) {
        CharStream input = CharStreams.fromString(cronExpr);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        assertEquals(cronExpr, new CronVisitor().visit(tree));
    }

    @Test
    public void testA() {
        assertParsedBackTo("*/5 * * * * ?");
    }

    @Test
    public void testB() {
        assertParsedBackTo("*/5 0 12 15W,L * 6#2");
    }

    @Test
    public void testC() {
        assertParsedBackTo("0 2/3,5/7 17,18,19 1-15,LW JAN-JUL ? " + Y + "-" + (Y + 5));
    }

    @Test
    public void testD() {
        assertParsedBackTo("5-30/7 0-12/3,15-45/2 2,3,4-17/2 ? JAN-JUL MON-THU/2 " + Y + "-" + (Y + 5));
    }

    @Test
    public void testSixFieldsWithoutYear() {
        assertParsedBackTo("0 0 0 1 1 ?");
        assertParsedBackTo("59 59 23 31 DEC SAT");
    }

    @Test
    public void testYearVariants() {
        assertParsedBackTo("0 0 12 * * ? *");
        assertParsedBackTo("0 0 12 * * ? " + Y);
        assertParsedBackTo("0 0 12 * * ? " + Y + "/2");
        assertParsedBackTo("0 0 12 * * ? " + Y + "," + (Y + 2) + "," + (Y + 4));
    }

    @Test
    public void testLastAndWeekdayTags() {
        assertParsedBackTo("0 0 12 L * ?");
        assertParsedBackTo("0 0 12 L-3 * ?");
        assertParsedBackTo("0 0 12 LW * ?");
        assertParsedBackTo("0 0 12 1W,15W,LW * ?");
        assertParsedBackTo("0 0 12 ? * 1L,5L");
        assertParsedBackTo("0 0 12 ? * MON#1,FRI#3,7L");
    }

    public static class CronVisitor extends CronExpressionBaseVisitor<String> {
        @Override
        public String visitCron(CronExpressionParser.CronContext ctx) {
            String secondPart = visit(ctx.second());
            String minutePart = visit(ctx.minute());
            String hourPart = visit(ctx.hour());
            String dayPart = visit(ctx.dayOfMonth());
            String monthPart = visit(ctx.month());
            String dayOfWeekPart = visit(ctx.dayOfWeek());
            String yearPart = ctx.year() != null ? visit(ctx.year()) : "";
            return (secondPart + " " + minutePart + " " + hourPart + " " + dayPart + " " + monthPart
                    + " " + dayOfWeekPart + " " + yearPart).trim();
        }

        @Override
        public String visitSecond(CronExpressionParser.SecondContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitMinute(CronExpressionParser.MinuteContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitHour(CronExpressionParser.HourContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitDayOfMonth(CronExpressionParser.DayOfMonthContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitMonth(CronExpressionParser.MonthContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitDayOfWeek(CronExpressionParser.DayOfWeekContext ctx) {
            return ctx.getText();
        }

        @Override
        public String visitYear(YearContext ctx) {
            return ctx.getText();
        }

    }
}
