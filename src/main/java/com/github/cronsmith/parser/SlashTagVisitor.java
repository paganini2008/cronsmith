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
public class SlashTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getSymbol() {
        return "/";
    }

    @Override
    public void setNextVisitor(TagVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (text.matches("\\d+\\/\\d+") && (filter == null || filter.contains("/"))) {
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
        if (text.matches("\\d+\\/\\d+") && (filter == null || filter.contains("/"))) {
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
        if (text.matches("\\d+\\/\\d+") && (filter == null || filter.contains("/"))) {
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
        if (text.matches("\\d+\\/\\d+") && (filter == null || filter.contains("/"))) {
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
        if (text.matches("((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)|(\\d+))\\/(\\d+)")
                && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = java.time.Month.valueOf(args[0]).getValue();
            }
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
        if (text.matches("((SUN|MON|TUE|WED|THU|FRI|SAT)|[1-7])(\\/\\d+)")
                && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = java.time.Month.valueOf(args[0]).getValue();
            }
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
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (text.matches("\\d{4}\\/\\d+") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            return CronExpressionUtils.everyYear(from, Year.MAX_YEAR, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedSymbolException(text);
    }

    @Override
    public int getOrder() {
        return 5;
    }

}
