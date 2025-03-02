package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDayOfWeekInMonth;

/**
 * 
 * @Description: HashVisitor
 * @Author: Fred Feng
 * @Date: 01/03/2025
 * @Version 1.0.0
 */
public class HashVisitor implements SymbolVisitor {

    private SymbolVisitor nextVisitor;

    @Override
    public String getSymbol() {
        return "#";
    }

    @Override
    public void setNextVisitor(SymbolVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("[1-7]#[1-5]") && (filter == null || filter.contains("#"))) {
            String[] args = text.split("#", 2);
            int dayOfWeek = Integer.parseInt(args[0]);
            int weekOfMonth = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheDayOfWeekInMonth) {
                    return ((TheDayOfWeekInMonth) cronExpression).and(weekOfMonth, dayOfWeek);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).dayOfWeek(weekOfMonth, dayOfWeek);
                }
            } else {
                return CronExpressionUtils.everyMonth().dayOfWeek(weekOfMonth, dayOfWeek);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        throw new UnsupportedSymbolException(text);
    }

}
