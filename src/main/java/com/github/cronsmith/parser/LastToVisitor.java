package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDay;
import com.github.cronsmith.cron.TheDayOfWeekInMonth;

/**
 * 
 * @Description: LastToVisitor
 * @Author: Fred Feng
 * @Date: 01/03/2025
 * @Version 1.0.0
 */
public class LastToVisitor implements SymbolVisitor {

    private SymbolVisitor nextVisitor;

    @Override
    public String getSymbol() {
        return "L";
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
        if (text.contains("L") && (filter == null || filter.contains("L"))) {
            CronExpression cronExpression =
                    context.getCronExpression() != null ? context.getCronExpression()
                            : CronExpressionUtils.everyMonth();
            if ("L".equals(text)) {
                if (cronExpression instanceof TheDay) {
                    Month month = (Month) cronExpression.getParent();
                    return ((TheDay) cronExpression).andDay(month.getLastDay());
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).lastDay();
                }
            } else if ("LW".equals(text)) {
                if (cronExpression instanceof TheDay) {
                    Month month = (Month) cronExpression.getParent();
                    return ((TheDay) cronExpression).andDay(month.getLastWeekDay());
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).lastWeekday();
                }
            } else if (text.matches("L\\-[1-9]")) {
                int n = Integer.parseInt(text.substring(text.indexOf("-") + 1));
                if (cronExpression instanceof TheDay) {
                    Month month = (Month) cronExpression.getParent();
                    return ((TheDay) cronExpression).andDay(month.getLastDay(n));
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).lastDay(n);
                }
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("[1-7]L") && (filter == null || filter.contains("L"))) {
            CronExpression cronExpression =
                    context.getCronExpression() != null ? context.getCronExpression()
                            : CronExpressionUtils.everyMonth();
            int dayOfWeek = Integer.parseInt(text.replace("L", ""));
            if (cronExpression instanceof TheDayOfWeekInMonth) {
                return ((TheDayOfWeekInMonth) cronExpression).andLast(dayOfWeek);
            } else if (cronExpression instanceof Month) {
                return ((Month) cronExpression).lastDayOfWeek(dayOfWeek);
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
