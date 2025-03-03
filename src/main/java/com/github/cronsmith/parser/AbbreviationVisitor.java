package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;
import com.github.cronsmith.cron.TheMonth;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;

/**
 * 
 * @Description: AbbreviationVisitor
 * @Author: Fred Feng
 * @Date: 01/03/2025
 * @Version 1.0.0
 */
public class AbbreviationVisitor implements SymbolVisitor {

    private SymbolVisitor nextVisitor;

    @Override
    public String getSymbol() {
        return "";
    }

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
        int monthValue;
        if ((monthValue = Utils.getMonthValue(text)) != -1) {
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheMonth) {
                    return ((TheMonth) cronExpression).andMonth(monthValue);
                } else if (cronExpression instanceof Year) {
                    return ((Year) cronExpression).month(monthValue);
                }
            } else {
                return CronExpressionUtils.everyYear().month(monthValue);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        int dayOfWeek;
        if ((dayOfWeek = Utils.getDayOfWeekValue(text)) != -1) {
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Week) {
                    return ((Week) cronExpression).day(dayOfWeek);
                } else if (cronExpression instanceof com.github.cronsmith.cron.Month) {
                    return ((com.github.cronsmith.cron.Month) cronExpression).everyWeek()
                            .day(dayOfWeek);
                }
            } else {
                return CronExpressionUtils.everyWeek().day(dayOfWeek);
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
