package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: CommaVisitor
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public class CommaVisitor implements SymbolVisitor {

    private SymbolVisitor nextVisitor;

    @Override
    public String getSymbol() {
        return ",";
    }

    @Override
    public void setNextVisitor(SymbolVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (text.contains(",") && (filter == null || filter.contains(","))) {
            String[] segments = text.split(",");
            for (String segment : segments) {
                context.setCronExpression(
                        context.getSymbolVisitor().visitSecond(segment, "-/", context));
            }
            return context.getCronExpression();
        } else if (nextVisitor != null) {
            return nextVisitor.visitSecond(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if (text.contains(",") && (filter == null || filter.contains(","))) {
            String[] segments = text.split(",");
            for (String segment : segments) {
                context.setCronExpression(
                        context.getSymbolVisitor().visitMinute(segment, "-/", context));
            }
            return context.getCronExpression();
        } else if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if (text.contains(",") && (filter == null || filter.contains(","))) {
            String[] segments = text.split(",");
            for (String segment : segments) {
                context.setCronExpression(
                        context.getSymbolVisitor().visitHour(segment, "-/", context));
            }
            return context.getCronExpression();
        } else if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if (text.contains(",") && (filter == null || filter.contains(","))) {
            String[] segments = text.split(",");
            for (String segment : segments) {
                context.setCronExpression(
                        context.getSymbolVisitor().visitDayOfMonth(segment, "-/LW", context));
            }
            return context.getCronExpression();
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        if (text.contains(",") && (filter == null || filter.contains(","))) {
            String[] segments = text.split(",");
            for (String segment : segments) {
                context.setCronExpression(
                        context.getSymbolVisitor().visitMonth(segment, "-/", context));
            }
            return context.getCronExpression();
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.contains(",") && (filter == null || filter.contains(","))) {
            String[] segments = text.split(",");
            for (String segment : segments) {
                context.setCronExpression(
                        context.getSymbolVisitor().visitDayOfWeek(segment, "-/L#", context));
            }
            return context.getCronExpression();
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (text.contains(",") && (filter == null || filter.contains(","))) {
            String[] segments = text.split(",");
            for (String segment : segments) {
                context.setCronExpression(
                        context.getSymbolVisitor().visitYear(segment, "-/", context));
            }
            return context.getCronExpression();
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }
}
