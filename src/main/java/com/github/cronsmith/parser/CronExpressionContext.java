package com.github.cronsmith.parser;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * Anltr Context
 * 
 * @Author: Fred Feng
 * @Date: 27/02/2025
 * @Version 1.0.0
 */
public class CronExpressionContext extends CronExpressionBaseVisitor<CronExpression> {

    public CronExpressionContext() {
        tagVisitors.add(new AsteriskTagVisitor());
        tagVisitors.add(new IgnoredTagVistor());
        tagVisitors.add(new NumberTagVisitor());
        tagVisitors.add(new TextTagVisitor());
        tagVisitors.add(new HyphenTagVisitor());
        tagVisitors.add(new SlashTagVisitor());
        tagVisitors.add(new LastTagVisitor());
        tagVisitors.add(new WeekdayVisitor());
        tagVisitors.add(new HashTagVisitor());
        tagVisitors.add(new CommaTagVisitor());
        Collections.sort(tagVisitors, (a, b) -> a.getOrder() - b.getOrder());
        TagVisitor tagVisitor = tagVisitors.get(0);
        for (int i = 1; i < tagVisitors.size(); i++) {
            tagVisitor.setNextVisitor(tagVisitors.get(i));
            tagVisitor = tagVisitors.get(i);
        }
        this.tagVisitor = tagVisitors.get(0);
    }

    private final List<TagVisitor> tagVisitors = new ArrayList<TagVisitor>();
    private final TagVisitor tagVisitor;
    private CronExpression cronExpression;

    private ZoneId zoneId = ZoneId.of("UTC");

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public CronExpression visitCron(CronContext ctx) {
        cronExpression = ctx.year() != null ? visit(ctx.year()) : null;
        cronExpression = visit(ctx.month());
        cronExpression = visit(ctx.dayOfWeek());
        cronExpression = visit(ctx.dayOfMonth());
        cronExpression = visit(ctx.hour());
        cronExpression = visit(ctx.minute());
        cronExpression = visit(ctx.second());
        return cronExpression;
    }

    @Override
    public CronExpression visitYear(YearContext ctx) {
        String text = ctx.getText();
        return tagVisitor.visitYear(text, null, this);
    }

    @Override
    public CronExpression visitDayOfWeek(DayOfWeekContext ctx) {
        String text = ctx.getText();
        return tagVisitor.visitDayOfWeek(text, null, this);
    }

    @Override
    public CronExpression visitMonth(MonthContext ctx) {
        String text = ctx.getText();
        return tagVisitor.visitMonth(text, null, this);
    }

    @Override
    public CronExpression visitDayOfMonth(DayOfMonthContext ctx) {
        String text = ctx.getText();
        return tagVisitor.visitDayOfMonth(text, null, this);
    }

    @Override
    public CronExpression visitHour(HourContext ctx) {
        String text = ctx.getText();
        return tagVisitor.visitHour(text, null, this);
    }

    @Override
    public CronExpression visitMinute(MinuteContext ctx) {
        String text = ctx.getText();
        return tagVisitor.visitMinute(text, null, this);
    }

    @Override
    public CronExpression visitSecond(SecondContext ctx) {
        String text = ctx.getText();
        return tagVisitor.visitSecond(text, null, this);
    }

    public void setCronExpression(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public TagVisitor getTagVisitor() {
        return tagVisitor;
    }

}
