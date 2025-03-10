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
            boolean every = false;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
                every = true;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Minute) {
                    return every ? ((Minute) cronExpression).everySecond(from, interval)
                            : ((Minute) cronExpression).second(from).toSecond(59, interval);
                } else if (cronExpression instanceof TheSecond) {
                    return ((TheSecond) cronExpression).andSecond(from).toSecond(59, interval);
                }
            }
            CronBuilder cronBuilder = new CronBuilder().setZoneId(context.getZoneId());
            return every ? cronBuilder.everySecond(from, interval)
                    : cronBuilder.everyMinute().second(from).toSecond(59, interval);
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
            boolean every = false;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
                every = true;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Hour) {
                    return every ? ((Hour) cronExpression).everyMinute(from, interval)
                            : ((Hour) cronExpression).minute(from).toMinute(59, interval);
                } else if (cronExpression instanceof TheMinute) {
                    return ((TheMinute) cronExpression).andMinute(from).toMinute(59, interval);
                }
            }
            CronBuilder cronBuilder = new CronBuilder().setZoneId(context.getZoneId());
            return every ? cronBuilder.everyMinute(from, interval)
                    : cronBuilder.minute(from).toMinute(59, interval);
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
            boolean every = false;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
                every = true;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Day) {
                    return every ? ((Day) cronExpression).everyHour(from, interval)
                            : ((Day) cronExpression).hour(from).toHour(23, interval);
                } else if (cronExpression instanceof TheHour) {
                    return ((TheHour) cronExpression).andHour(from).toHour(23, interval);
                }
            }
            CronBuilder cronBuilder = new CronBuilder().setZoneId(context.getZoneId());
            return every ? cronBuilder.everyHour(from, interval)
                    : cronBuilder.hour(from).toHour(23, interval);
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
            boolean every = false;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
                every = true;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof Month) {
                    return every ? ((Month) cronExpression).everyDay(from, interval)
                            : ((Month) cronExpression).day(from).toLastDay(interval);
                } else if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andDay(from).toLastDay(interval);
                }
            }
            CronBuilder cronBuilder = new CronBuilder().setZoneId(context.getZoneId());
            return every ? cronBuilder.everyMonth().everyDay(from, interval)
                    : cronBuilder.everyMonth().day(from).toLastDay(interval);
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
            boolean every = false;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                if ("*".equals(args[0])) {
                    from = 1;
                    every = true;
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
                    return every ? ((Year) cronExpression).everyMonth(from, interval)
                            : ((Year) cronExpression).month(from).toMonth(12, interval);
                } else if (cronExpression instanceof TheMonth) {
                    return ((TheMonth) cronExpression).andMonth(from).toMonth(12, interval);
                }
            }
            CronBuilder cronBuilder =
                    new CronBuilder().setZoneId(context.getZoneId()).setUseMonthAsNumber(useNumber);
            return every ? cronBuilder.everyMonth(from, interval)
                    : cronBuilder.everyYear().month(from).toMonth(12, interval);
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
            boolean every = false;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                if ("*".equals(args[0])) {
                    from = 1;
                    every = true;
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
                    return every ? ((Month) cronExpression).everyWeek().everyDay(from, interval)
                            : ((Month) cronExpression).everyWeek().day(from).toDay(7, interval);
                } else if (cronExpression instanceof TheDayOfWeek) {
                    return ((TheDayOfWeek) cronExpression).andDay(from).toDay(7, interval);
                }
            }
            CronBuilder cronBuilder = new CronBuilder().setZoneId(context.getZoneId())
                    .setUseDayOfWeekAsNumber(useNumber);
            return every ? cronBuilder.everyWeek().everyDay(from, interval)
                    : cronBuilder.everyWeek().day(from).toDay(7, interval);
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (text.matches("((\\*)|(\\d{4}))\\/(\\d+)") && (filter == null || filter.contains("/"))) {
            String[] args = text.split("\\/");
            boolean every = false;;
            int from;
            try {
                from = Integer.parseInt(args[0]);
            } catch (RuntimeException e) {
                from = 0;
                every = true;
            }
            int interval = Integer.parseInt(args[1]);
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (from == 0) {
                    from = cronExpression.getBuilder().getStartTime().getYear();
                }
                if (cronExpression instanceof TheYear) {
                    return ((TheYear) cronExpression).andYear(from).toYear(Year.MAX_YEAR, interval);
                }
            }
            CronBuilder cronBuilder = new CronBuilder().setZoneId(context.getZoneId());
            if (from == 0) {
                from = cronBuilder.getStartTime().getYear();
            }
            return every ? cronBuilder.everyYear(from, interval)
                    : cronBuilder.year(from).toEnd(interval);
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
