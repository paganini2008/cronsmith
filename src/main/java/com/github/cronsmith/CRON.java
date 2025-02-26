
package com.github.cronsmith;


import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;
import com.github.cronsmith.antlr.CronExpressionLexer;
import com.github.cronsmith.antlr.CronExpressionParser;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.DayOfWeek;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;
import com.github.cronsmith.parser.CronExpressionContext;

/**
 * 
 * CRON is the entry class of cronsmith library. Start your work from here.
 * 
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public abstract class CRON {

    public static CronExpression parse(String cronExpression) {
        CharStream input = CharStreams.fromString(cronExpression);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        CronExpressionContext context = new CronExpressionContext();
        return context.visit(tree);
    }

    public static String printParseTree(String cronExpression) {
        CharStream input = CharStreams.fromString(cronExpression);
        CronExpressionLexer lexer = new CronExpressionLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CronExpressionParser parser = new CronExpressionParser(tokens);
        ParseTree tree = parser.cron();
        printTree(tree, parser, 0);
        return cronExpression;
    }

    private static void printTree(ParseTree tree, Parser parser, int indent) {
        String indentString = "  ".repeat(indent);
        String nodeText = Trees.getNodeText(tree, parser);
        System.out.println(indentString + nodeText);
        for (int i = 0; i < tree.getChildCount(); i++) {
            printTree(tree.getChild(i), parser, indent + 2);
        }
    }

    public static String toCronString(CronExpression cronExpression) {
        CronExpression copy = cronExpression.copy();
        if (copy instanceof Year) {
            copy = ((Year) copy).Jan().day(1).at(0, 0, 0);
        } else if (copy instanceof Month) {
            copy = ((Month) copy).day(1).at(0, 0, 0);
        } else if (copy instanceof Week) {
            copy = ((Week) copy).Mon().at(0, 0, 0);
        } else if (copy instanceof Day) {
            copy = ((Day) copy).at(0, 0, 0);
        } else if (copy instanceof Hour) {
            copy = ((Hour) copy).at(0, 0);
        } else if (copy instanceof Minute) {
            copy = ((Minute) copy).second(0);
        }

        final StringBuilder cron = new StringBuilder();
        CronExpression second = copy;
        CronExpression minute = second.getParent();
        CronExpression hour = minute.getParent();
        cron.append(second.toCronString()).append(" ").append(minute.toCronString()).append(" ")
                .append(hour.toCronString()).append(" ");

        CronExpression day = hour.getParent();
        boolean hasDayOfWeek = false;
        if (day instanceof DayOfWeek) {
            hasDayOfWeek = true;
        }
        if (hasDayOfWeek) {
            cron.append("?").append(" ");
        } else {
            cron.append(day.toCronString()).append(" ");
        }

        CronExpression month;
        if (hasDayOfWeek && day.getParent() instanceof Week) {
            month = day.getParent().getParent();
        } else {
            month = day.getParent();
        }

        cron.append(month.toCronString()).append(" ");

        if (hasDayOfWeek) {
            cron.append(day.toCronString());
        } else {
            cron.append("?");
        }

        CronExpression year = month.getParent();
        if (year instanceof Year) {
            if (!"*".equals(year.toCronString())) {
                cron.append(" ").append(year.toCronString());
            }
        }
        return cron.toString();
    }



    public static byte[] toByteArray(CronExpression cronExpression) {
        return SerializationUtils.serialize(cronExpression);
    }

    public static CronExpression load(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

}
