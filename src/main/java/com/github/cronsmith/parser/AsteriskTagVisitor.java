package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;

/**
 * 
 * Match and parse asterisk tag '*'
 * 
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public class AsteriskTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public void setNextVisitor(TagVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public String getTag() {
        return "*";
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if ("*".equals(text) && (filter == null || filter.contains("*"))) {
            if (context.getCronExpression() != null) {
                return ((Minute) context.getCronExpression()).everySecond();
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everySecond();
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitSecond(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if ("*".equals(text) && (filter == null || filter.contains("*"))) {
            if (context.getCronExpression() != null) {
                return ((Hour) context.getCronExpression()).everyMinute();
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyHour();
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if ("*".equals(text) && (filter == null || filter.contains("*"))) {
            if (context.getCronExpression() != null) {
                return ((Day) context.getCronExpression()).everyHour();
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyHour();
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if ("*".equals(text) && (filter == null || filter.contains("*"))) {
            if (context.getCronExpression() != null) {
                return ((Month) context.getCronExpression()).everyDay();
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyDay();
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        if ("*".equals(text) && (filter == null || filter.contains("*"))) {
            if (context.getCronExpression() != null) {
                return ((Year) context.getCronExpression()).everyMonth();
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyMonth();
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }


    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if ("*".equals(text) && (filter == null || filter.contains("*"))) {
            if (context.getCronExpression() != null) {
                return ((Week) context.getCronExpression()).everyDay();
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyWeek().everyDay();
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if ("*".equals(text) && (filter == null || filter.contains("*"))) {
            return new CronBuilder().setZoneId(context.getZoneId()).everyYear();
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
