package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDay;
import com.github.cronsmith.cron.TheDayOfWeekInMonth;

/**
 * 
 * Match and parse letter 'L' and relevant
 * 
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public class LastTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getTag() {
        return "L";
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
        if (text.matches("L|LW|L\\-\\d{1,}") && (filter == null || filter.contains("L"))) {
            CronExpression cronExpression =
                    context.getCronExpression() != null ? context.getCronExpression()
                            : new CronBuilder().setZoneId(context.getZoneId()).everyMonth();
            if ("L".equals(text)) {
                if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andLastDay();
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).lastDay();
                }
            } else if ("LW".equals(text)) {
                if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andLastWeekday();
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).lastWeekday();
                }
            } else if (text.matches("L\\-\\d{1,}")) {
                int n = Integer.parseInt(text.substring(text.indexOf("-") + 1));
                if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andLastDay(n);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).lastDay(n);
                }
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
        if (text.matches("[1-7]L") && (filter == null || filter.contains("L"))) {
            CronExpression cronExpression =
                    context.getCronExpression() != null ? context.getCronExpression()
                            : new CronBuilder().setZoneId(context.getZoneId()).everyMonth();
            int dayOfWeek = Integer.parseInt(text.replace("L", ""));
            if (cronExpression instanceof TheDayOfWeekInMonth) {
                return ((TheDayOfWeekInMonth) cronExpression).andLast(dayOfWeek);
            } else if (cronExpression instanceof Month) {
                return ((Month) cronExpression).lastDayOfWeek(dayOfWeek);
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
        return 6;
    }

}
