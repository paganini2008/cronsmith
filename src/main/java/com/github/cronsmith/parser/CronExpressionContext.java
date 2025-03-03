package com.github.cronsmith.parser;

import java.util.TimeZone;
import com.github.cronsmith.antlr.CronExpressionBaseVisitor;
import com.github.cronsmith.antlr.CronExpressionParser.CronContext;
import com.github.cronsmith.antlr.CronExpressionParser.DayOfMonthContext;
import com.github.cronsmith.antlr.CronExpressionParser.DayOfWeekContext;
import com.github.cronsmith.antlr.CronExpressionParser.HourContext;
import com.github.cronsmith.antlr.CronExpressionParser.MinuteContext;
import com.github.cronsmith.antlr.CronExpressionParser.MonthContext;
import com.github.cronsmith.antlr.CronExpressionParser.SecondContext;
import com.github.cronsmith.antlr.CronExpressionParser.YearContext;
import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: CronExpressionContext
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class CronExpressionContext extends CronExpressionBaseVisitor<CronExpression> {

    public CronExpressionContext() {
        AsteriskVisitor asteriskVisitor = new AsteriskVisitor();
        IntegerVisitor integerVisitor = new IntegerVisitor();
        AbbreviationVisitor abbreviationVisitor = new AbbreviationVisitor();
        IntegerHyphenVisitor integerHyphenVisitor = new IntegerHyphenVisitor();
        AbbreviationHyphenVisitor abbreviationHyphenVisitor = new AbbreviationHyphenVisitor();
        SlashVisitor slashVisitor = new SlashVisitor();
        IgnoredVistor ignoredVistor = new IgnoredVistor();
        LastToVisitor lastToVisitor = new LastToVisitor();
        WeekdayVisitor weekdayVisitor = new WeekdayVisitor();
        CommaVisitor commaVisitor = new CommaVisitor();

        asteriskVisitor.setNextVisitor(integerVisitor);
        integerVisitor.setNextVisitor(abbreviationVisitor);
        abbreviationVisitor.setNextVisitor(integerHyphenVisitor);
        integerHyphenVisitor.setNextVisitor(abbreviationHyphenVisitor);
        abbreviationHyphenVisitor.setNextVisitor(slashVisitor);
        slashVisitor.setNextVisitor(ignoredVistor);
        ignoredVistor.setNextVisitor(lastToVisitor);
        lastToVisitor.setNextVisitor(weekdayVisitor);
        weekdayVisitor.setNextVisitor(commaVisitor);
        this.symbolVisitor = asteriskVisitor;
    }

    private CronExpression cronExpression;
    private final SymbolVisitor symbolVisitor;

    private TimeZone timeZone = TimeZone.getDefault();

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public CronExpression visitCron(CronContext ctx) {
        cronExpression = ctx.year() != null ? visit(ctx.year()) : null;
        cronExpression = visit(ctx.dayOfWeek());
        cronExpression = visit(ctx.month());
        cronExpression = visit(ctx.dayOfMonth());
        cronExpression = visit(ctx.hour());
        cronExpression = visit(ctx.minute());
        cronExpression = visit(ctx.second());
        return cronExpression;
    }

    @Override
    public CronExpression visitYear(YearContext ctx) {
        String text = ctx.getText();
        return symbolVisitor.visitYear(text, null, this);
    }

    @Override
    public CronExpression visitDayOfWeek(DayOfWeekContext ctx) {
        String text = ctx.getText();
        return symbolVisitor.visitDayOfWeek(text, null, this);
    }

    @Override
    public CronExpression visitMonth(MonthContext ctx) {
        String text = ctx.getText();
        return symbolVisitor.visitMonth(text, null, this);
    }

    @Override
    public CronExpression visitDayOfMonth(DayOfMonthContext ctx) {
        String text = ctx.getText();
        return symbolVisitor.visitDayOfMonth(text, null, this);
    }

    @Override
    public CronExpression visitHour(HourContext ctx) {
        String text = ctx.getText();
        return symbolVisitor.visitHour(text, null, this);
    }

    @Override
    public CronExpression visitMinute(MinuteContext ctx) {
        String text = ctx.getText();
        return symbolVisitor.visitMinute(text, null, this);
    }

    @Override
    public CronExpression visitSecond(SecondContext ctx) {
        String text = ctx.getText();
        return symbolVisitor.visitSecond(text, null, this);
    }

    public void setCronExpression(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public SymbolVisitor getSymbolVisitor() {
        return symbolVisitor;
    }

}
