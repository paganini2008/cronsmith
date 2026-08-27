package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDay;

/**
 * 
 * Match weekday tag '#'
 * 
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public class WeekdayVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getTag() {
        return "W";
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
        boolean wantsW = filter == null || filter.contains("W");
        // '<from>-<to>W[/<step>]' - the weekday nearest each day, stepping from 'from' to 'to'.
        if (wantsW && text.matches("\\d+-\\d+W(/\\d+)?")) {
            int from = Integer.parseInt(text.substring(0, text.indexOf('-')));
            int to = Integer.parseInt(text.substring(text.indexOf('-') + 1, text.indexOf('W')));
            int interval = stepOf(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression instanceof TheDay) {
                return ((TheDay) cronExpression).andDay(from).toLatestWeekday(to, interval);
            } else if (cronExpression instanceof Month) {
                return ((Month) cronExpression).day(from).toLatestWeekday(to, interval);
            }
            return new CronBuilder().setZoneId(context.getZoneId()).everyMonth().day(from)
                    .toLatestWeekday(to, interval);
        }
        // '<from>-LW[/<step>]' - stepping from 'from' to the month's last weekday.
        if (wantsW && text.matches("\\d+-LW(/\\d+)?")) {
            int from = Integer.parseInt(text.substring(0, text.indexOf('-')));
            int interval = stepOf(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression instanceof TheDay) {
                return ((TheDay) cronExpression).andDay(from).toLastWeekday(interval);
            } else if (cronExpression instanceof Month) {
                return ((Month) cronExpression).day(from).toLastWeekday(interval);
            }
            return new CronBuilder().setZoneId(context.getZoneId()).everyMonth().day(from)
                    .toLastWeekday(interval);
        }
        if (text.matches("\\d{1,}W") && (filter == null || filter.contains("W"))) {
            int dayOfMonth = Integer.parseInt(text.replace("W", ""));
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andLatestWeekday(dayOfMonth);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).latestWeekday(dayOfMonth);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyMonth()
                        .latestWeekday(dayOfMonth);
            }
        } else if (nextVisitor != null) {
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
        if (nextVisitor != null) {
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

    private static int stepOf(String text) {
        int slash = text.indexOf('/');
        return slash < 0 ? 1 : Integer.parseInt(text.substring(slash + 1));
    }

    @Override
    public int getOrder() {
        return 7;
    }

}
