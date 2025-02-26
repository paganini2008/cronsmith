package com.github.cronsmith.parser;

import com.github.cronsmith.AbbreviationUtils;
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
import com.github.cronsmith.cron.Year;

/**
 * 
 * Match and parse slash tag '/'
 * 
 * @Author: Fred Feng
 * @Date: 25/02/2025
 * @Version 1.0.0
 */
public class SlashTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getTag() {
        return "/";
    }

    @Override
    public void setNextVisitor(TagVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\*|\\d+)\\/(\\d+)") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Minute) {
                    return ((Minute) cronExpression).everySecond(from, 59, interval);
                } else if (cronExpression instanceof TheSecond) {
                    return ((TheSecond) cronExpression).andSecond(from).toSecond(59, interval);
                }
            }
            return new CronBuilder().setZoneId(context.getZoneId()).everySecond(from, 59, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitSecond(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\*|\\d+)\\/(\\d+)") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Hour) {
                    return ((Hour) cronExpression).everyMinute(from, 59, interval);
                } else if (cronExpression instanceof TheMinute) {
                    return ((TheMinute) cronExpression).andMinute(from).toMinute(59, interval);
                }
            }
            return new CronBuilder().setZoneId(context.getZoneId()).everyMinute(from, 59, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\*|\\d+)\\/(\\d+)") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Day) {
                    return ((Day) cronExpression).everyHour(from, 23, interval);
                } else if (cronExpression instanceof TheHour) {
                    return ((TheHour) cronExpression).andHour(from).toHour(23, interval);
                }
            }
            return new CronBuilder().setZoneId(context.getZoneId()).everyHour(from, 23, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("(\\*|\\d+)\\/(\\d+)") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).everyDay(from, interval);
                } else if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andDay(from).toLastDay(interval);
                }
            }
            return new CronBuilder().setZoneId(context.getZoneId()).everyMonth().everyDay(from,
                    interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        if (text.matches(
                "((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)|(\\*)|(\\d+))\\/(\\d+)")
                && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            boolean useNumber = true;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                if ("*".equals(args[0])) {
                    from = 1;
                } else {
                    from = AbbreviationUtils.getMonthValue(args[0]);
                }
                useNumber = false;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                cronExpression.getBuilder().setUseMonthAsNumber(useNumber);
                if (cronExpression instanceof Year) {
                    return ((Year) cronExpression).everyMonth(from, 12, interval);
                } else if (cronExpression instanceof TheMonth) {
                    return ((TheMonth) cronExpression).andMonth(from).toMonth(12, interval);
                }
            }
            return new CronBuilder().setZoneId(context.getZoneId()).setUseMonthAsNumber(useNumber)
                    .everyMonth(from, 12, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("((MON|TUE|WED|THU|FRI|SAT|SUN)|(\\*)|[1-7])\\/(\\d+)")
                && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from;
            boolean useNumber = true;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                if ("*".equals(args[0])) {
                    from = 1;
                } else {
                    from = AbbreviationUtils.getDayOfWeekValue(args[0]);
                }
                useNumber = false;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            cronExpression.getBuilder().setUseDayOfWeekAsNumber(useNumber);
            if (cronExpression != null) {
                if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).everyWeek().everyDay(from, 7, interval);
                } else if (cronExpression instanceof TheDayOfWeek) {
                    return ((TheDayOfWeek) cronExpression).andDay(from).toDay(7, interval);
                }
            }
            return new CronBuilder().setZoneId(context.getZoneId())
                    .setUseDayOfWeekAsNumber(useNumber).everyWeek().everyDay(from, 7, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\d{4})\\/(\\d+)") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            int from = Integer.parseInt(args[0]);
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheYear) {
                    return ((TheYear) cronExpression).andYear(from).toYear(Year.MAX_YEAR, interval);
                }
            }
            return new CronBuilder().setZoneId(context.getZoneId()).everyYear(from, Year.MAX_YEAR,
                    interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public int getOrder() {
        return 5;
    }

}
