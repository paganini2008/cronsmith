package com.github.cronsmith.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import com.github.cronsmith.antlr.CronExpressionBaseVisitor;
import com.github.cronsmith.antlr.CronExpressionLexer;
import com.github.cronsmith.antlr.CronExpressionParser;
import com.github.cronsmith.antlr.CronExpressionParser.YearContext;

public class TestMain {

    public static class CronVisitor extends CronExpressionBaseVisitor<String> {
        @Override
        public String visitCron(CronExpressionParser.CronContext ctx) {
            String secondPart = visit(ctx.second());
            String minutePart = visit(ctx.minute());
            String hourPart = visit(ctx.hour());
            String dayPart = visit(ctx.dayOfMonth());
            String monthPart = visit(ctx.month());
            String dayOfWeekPart = visit(ctx.dayOfWeek());
            String yearPart = ctx.year() != null ? visit(ctx.year()) : "未指定";

            return "秒: " + secondPart + ", 分: " + minutePart + ", 时: " + hourPart + ", 日: "
                    + dayPart + ", 月: " + monthPart + ", 星期：" + dayOfWeekPart + ", 年: " + yearPart;
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

    public static void main(String[] args) {
        String cronExpr = "5-30/7 0-12/3,15-45/2 2,3,4-17/2 ? JAN-JULYYY MON-THU/2 2025-2030";
        // cronExpr = "0,3,6,9 12 0/4 * * SUN-MON 2025-2030,2032-2038/2";
        // cronExpr = "0/1 0 12 5 * ?";
        // cronExpr = "0 0 12 ? * 5L";
        // cronExpr = "5-30/7 * * * * ?";
        CharStream input = CharStreams.fromString(cronExpr);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();

        System.out.println("Parse Tree: " + tree.toStringTree(parser));

        CronVisitor visitor = new CronVisitor();
        String result = visitor.visit(tree);

        System.out.println(result);
    }

}
