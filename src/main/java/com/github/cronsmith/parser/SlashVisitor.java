package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.CronExpressionUtils;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.Year;

/**
 * 
 * @Description: SlashVisitor
 * @Author: Fred Feng
 * @Date: 01/03/2025
 * @Version 1.0.0
 */
public class SlashVisitor implements SymbolVisitor {

    private SymbolVisitor nextVisitor;

    @Override
    public String getSymbol() {
        return "/";
    }

    @Override
    public void setNextVisitor(SymbolVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (text.matches("\\d{1,2}\\/\\d{1,2}") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Minute) {
                    return ((Minute) cronExpression).everySecond(from, 59, interval);
                }
            } else {
                return CronExpressionUtils.everySecond(from, 59, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if (text.matches("\\d{1,2}\\/\\d{1,2}") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Hour) {
                    return ((Hour) cronExpression).everyMinute(from, 59, interval);
                }
            } else {
                return CronExpressionUtils.everyMinute(from, 59, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if (text.matches("\\d{1,2}\\/\\d{1,2}") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Day) {
                    return ((Day) cronExpression).everyHour(from, 23, interval);
                }
            } else {
                return CronExpressionUtils.everyHour(from, 23, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("\\d{1,2}\\/\\d{1,2}") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).everyDay(from, interval);
                }
            } else {
                return CronExpressionUtils.everyMonth().everyDay(from, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        if (text.matches("\\d{1,2}\\/\\d{1,2}") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Year) {
                    return ((Year) cronExpression).everyMonth(from, 12, interval);
                }
            } else {
                return CronExpressionUtils.everyMonth(from, 12, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("[1-7]\\/[1-7]") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).everyWeek().everyDay(from, 7, interval);
                }
            } else {
                return CronExpressionUtils.everyWeek().everyDay(from, 7, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (text.matches("\\d{4}\\/\\d{1,4}") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            return CronExpressionUtils.everyYear(from, Year.MAX_YEAR, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

}
