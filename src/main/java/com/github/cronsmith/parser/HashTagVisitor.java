package com.github.cronsmith.parser;

import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDayOfWeekInMonth;

/**
 * 
 * Match and parse hash tag '#'
 * 
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public class HashTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getTag() {
        return "#";
    }

    @Override
    public void setNextVisitor(TagVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitSecond(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("((SUN|MON|TUE|WED|THU|FRI|SAT)|[1-7])(#[1-5])")
                && (filter == null || filter.contains("#"))) {
            String[] args = text.split("#", 2);
            int dayOfWeek;
            boolean useNumber = true;
            try {
                dayOfWeek = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                dayOfWeek = AbbreviationUtils.getDayOfWeekValue(args[0]);
                useNumber = false;
            }
            int weekOfMonth = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                cronExpression.getBuilder().setUseDayOfWeekAsNumber(useNumber);
                if (cronExpression instanceof TheDayOfWeekInMonth) {
                    return ((TheDayOfWeekInMonth) cronExpression).and(weekOfMonth, dayOfWeek);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).dayOfWeek(weekOfMonth, dayOfWeek);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId())
                        .setUseDayOfWeekAsNumber(useNumber).everyMonth()
                        .dayOfWeek(weekOfMonth, dayOfWeek);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public int getOrder() {
        return 8;
    }

}
