package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronBuilder;
import com.github.cronsmith.cron.CronExpression;
import com.github.cronsmith.cron.Day;
import com.github.cronsmith.cron.Hour;
import com.github.cronsmith.cron.Minute;
import com.github.cronsmith.cron.Month;
import com.github.cronsmith.cron.TheDay;
import com.github.cronsmith.cron.TheDayOfWeek;
import com.github.cronsmith.cron.TheHour;
import com.github.cronsmith.cron.TheMinute;
import com.github.cronsmith.cron.TheMonth;
import com.github.cronsmith.cron.TheSecond;
import com.github.cronsmith.cron.TheYear;
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;

/**
 * 
 * Number Match
 * 
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public class NumberTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getTag() {
        return "";
    }

    @Override
    public void setNextVisitor(TagVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (isInteger(text)) {
            int second = Integer.parseInt(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheSecond) {
                    return ((TheSecond) cronExpression).andSecond(second);
                } else if (cronExpression instanceof Minute) {
                    return ((Minute) cronExpression).second(second);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyMinute()
                        .second(second);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitSecond(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if (isInteger(text)) {
            int minute = Integer.parseInt(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheMinute) {
                    return ((TheMinute) cronExpression).andMinute(minute);
                } else if (cronExpression instanceof Hour) {
                    return ((Hour) cronExpression).minute(minute);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyHour().minute(minute);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if (isInteger(text)) {
            int hour = Integer.parseInt(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheHour) {
                    return ((TheHour) cronExpression).andHour(hour);
                } else if (cronExpression instanceof Day) {
                    return ((Day) cronExpression).hour(hour);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyDay().hour(hour);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if (isInteger(text)) {
            int dayOfMonth = Integer.parseInt(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andDay(dayOfMonth);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).day(dayOfMonth);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyMonth()
                        .day(dayOfMonth);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        if (isInteger(text)) {
            int month = Integer.parseInt(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheMonth) {
                    return ((TheMonth) cronExpression).andMonth(month);
                } else if (cronExpression instanceof Year) {
                    return ((Year) cronExpression).month(month);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyYear().month(month);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (isInteger(text)) {
            int dayOfWeek = Integer.parseInt(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheDayOfWeek) {
                    return ((TheDayOfWeek) cronExpression).andDay(dayOfWeek);
                } else if (cronExpression instanceof Week) {
                    return ((Week) cronExpression).day(dayOfWeek);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).everyWeek().day(dayOfWeek);
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
        if (isInteger(text)) {
            int year = Integer.parseInt(text);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheYear) {
                    return ((TheYear) cronExpression).andYear(year);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).year(year);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedTagException(text);

    }

    private boolean isInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
