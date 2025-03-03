package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDayOfWeek;
import com.github.cronsmith.cron.TheMonth;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;

/**
 * 
 * @Description: AbbreviationHyphenVisitor
 * @Author: Fred Feng
 * @Date: 02/03/2025
 * @Version 1.0.0
 */
public class AbbreviationHyphenVisitor implements SymbolVisitor {

    @Override
    public String getSymbol() {
        return "-";
    }

    private SymbolVisitor nextVisitor;

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitSecond(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        if (text.contains("-") && (filter == null || filter.contains("-"))) {
            String[] args = text.split("\\-", 2);
            int from = Utils.getMonthValue(args[0]);
            int to = Utils.getMonthValue(args[1]);
            if (from != -1 && to != -1) {
                CronExpression cronExpression = context.getCronExpression();
                if (cronExpression != null) {
                    if (cronExpression instanceof TheMonth) {
                        return ((TheMonth) cronExpression).andMonth(from).toMonth(to);
                    } else if (cronExpression instanceof Year) {
                        return ((Year) cronExpression).month(from).toMonth(to);
                    }
                } else {
                    return CronExpressionUtils.everyYear().month(from).toMonth(to);
                }
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.contains("-") && (filter == null || filter.contains("-"))) {
            String[] args = text.split("\\-", 2);
            int from = Utils.getDayOfWeekValue(args[0]);
            int to = Utils.getDayOfWeekValue(args[1]);
            if (from != -1 && to != -1) {
                CronExpression cronExpression = context.getCronExpression();
                if (cronExpression != null) {
                    if (cronExpression instanceof TheDayOfWeek) {
                        return ((TheDayOfWeek) cronExpression).andDay(from).toDay(to);
                    } else if (cronExpression instanceof Week) {
                        return ((Week) cronExpression).day(from).toDay(to);
                    } else if (cronExpression instanceof Month) {
                        return ((Month) cronExpression).day(from).toDay(to);
                    }
                } else {
                    return CronExpressionUtils.everyWeek().day(from).toDay(to);
                }
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public void setNextVisitor(SymbolVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

}
