package com.github.cronsmith.test;

import static org.junit.Assert.assertEquals;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;
import org.junit.Test;
import com.github.cronsmith.antlr.CronExpressionBaseVisitor;
import com.github.cronsmith.antlr.CronExpressionLexer;
import com.github.cronsmith.antlr.CronExpressionParser;
import com.github.cronsmith.antlr.CronExpressionParser.YearContext;

/**
 * 
 * @Description: CronExpressionAnltrTests
 * @Author: Fred Feng
 * @Date: 10/03/2025
 * @Version 1.0.0
 */
public class CronExpressionAnltrTests {

    @Test
    public void testA() {
        String cronExpr = "*/5 * * * * ?";
        CharStream input = CharStreams.fromString(cronExpr);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        printTree(tree, parser, 0);
        CronVisitor visitor = new CronVisitor();
        String result = visitor.visit(tree);
        assertEquals(cronExpr, result);
    }

    @Test
    public void testB() {
        String cronExpr = "*/5 0 12 15W,L * 6#2";
        CharStream input = CharStreams.fromString(cronExpr);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        printTree(tree, parser, 0);
        CronVisitor visitor = new CronVisitor();
        String result = visitor.visit(tree);
        assertEquals(cronExpr, result);
    }

    @Test
    public void testC() {
        String cronExpr = "0 2/3,5/7 17,18,19 1-15,LW JAN-JUL ? 2025-2030";
        CharStream input = CharStreams.fromString(cronExpr);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        printTree(tree, parser, 0);
        CronVisitor visitor = new CronVisitor();
        String result = visitor.visit(tree);
        assertEquals(cronExpr, result);
    }

    @Test
    public void testD() {
        String cronExpr = "5-30/7 0-12/3,15-45/2 2,3,4-17/2 ? JAN-JUL MON-THU/2 2025-2030";
        CharStream input = CharStreams.fromString(cronExpr);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        printTree(tree, parser, 0);
        CronVisitor visitor = new CronVisitor();
        String result = visitor.visit(tree);
        assertEquals(cronExpr, result);
    }

    private static void printTree(ParseTree tree, Parser parser, int indent) {
        String indentString = "  ".repeat(indent);
        String nodeText = Trees.getNodeText(tree, parser);
        System.out.println(indentString + nodeText);

        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), parser, indent + 2);
        }
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
