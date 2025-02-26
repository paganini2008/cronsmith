package com.github.cronsmith.parser;

import com.github.cronsmith.AbbreviationUtils;
import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDayOfWeek;
import com.github.cronsmith.cron.TheMonth;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;

/**
 * 
 * Abbreviation(month/day-of-week) match
 * 
 * @Author: Fred Feng
 * @Date: 23/02/2025
 * @Version 1.0.0
 */
public class TextTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getTag() {
        return "";
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
        int monthValue;
        if ((monthValue = AbbreviationUtils.getMonthValue(text)) != -1) {
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheMonth) {
                    return ((TheMonth) cronExpression).andMonth(monthValue);
                } else if (cronExpression instanceof Year) {
                    return ((Year) cronExpression).month(monthValue);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyYear()
                        .month(monthValue);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        int dayOfWeek;
        if ((dayOfWeek = AbbreviationUtils.getDayOfWeekValue(text)) != -1) {
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Week) {
                    return ((Week) cronExpression).day(dayOfWeek);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).everyWeek().day(dayOfWeek);
                } else if (cronExpression instanceof TheDayOfWeek) {
                    return ((TheDayOfWeek) cronExpression).andDay(dayOfWeek);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyWeek().day(dayOfWeek);
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
    public void setNextVisitor(TagVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public int getOrder() {
        return 3;
    }

}
