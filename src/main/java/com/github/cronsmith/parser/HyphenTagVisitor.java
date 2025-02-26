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
import com.github.cronsmith.cron.Week;
import com.github.cronsmith.cron.Year;

/**
 * 
 * Match and parse hyphen tag '-'
 * 
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public class HyphenTagVisitor implements TagVisitor {

    private TagVisitor nextVisitor;

    @Override
    public String getTag() {
        return "-";
    }

    @Override
    public void setNextVisitor(TagVisitor nextVisitor) {
        this.nextVisitor = nextVisitor;
    }

    @Override
    public CronExpression visitSecond(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\d+\\-\\d+)(\\/\\d+)?") && (filter == null || filter.contains("-"))) {
            String[] hyphenArgs = text.split("\\-", 2);
            int interval = 1;
            int from = Integer.parseInt(hyphenArgs[0]);
            int to;
            if (hyphenArgs[1].contains("/")) {
                String[] slashArgs = hyphenArgs[1].split("\\/");
                to = Integer.parseInt(slashArgs[0]);
                interval = Integer.parseInt(slashArgs[1]);
            } else {
                to = Integer.parseInt(hyphenArgs[1]);
            }
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheSecond) {
                    return ((TheSecond) cronExpression).andSecond(from).toSecond(to, interval);
                } else if (cronExpression instanceof Minute) {
                    return ((Minute) cronExpression).second(from).toSecond(to, interval);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyMinute().second(from)
                        .toSecond(to, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitSecond(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMinute(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\d+\\-\\d+)(\\/\\d+)?") && (filter == null || filter.contains("-"))) {
            String[] hyphenArgs = text.split("\\-", 2);
            int interval = 1;
            int from = Integer.parseInt(hyphenArgs[0]);
            int to;
            if (hyphenArgs[1].contains("/")) {
                String[] slashArgs = hyphenArgs[1].split("\\/");
                to = Integer.parseInt(slashArgs[0]);
                interval = Integer.parseInt(slashArgs[1]);
            } else {
                to = Integer.parseInt(hyphenArgs[1]);
            }
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheMinute) {
                    return ((TheMinute) cronExpression).andMinute(from).toMinute(to, interval);
                } else if (cronExpression instanceof Hour) {
                    return ((Hour) cronExpression).minute(from).toMinute(to, interval);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyHour().minute(from)
                        .toMinute(to, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMinute(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitHour(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\d+\\-\\d+)(\\/\\d+)?") && (filter == null || filter.contains("-"))) {
            String[] hyphenArgs = text.split("\\-", 2);
            int interval = 1;
            int from = Integer.parseInt(hyphenArgs[0]);
            int to;
            if (hyphenArgs[1].contains("/")) {
                String[] slashArgs = hyphenArgs[1].split("\\/");
                to = Integer.parseInt(slashArgs[0]);
                interval = Integer.parseInt(slashArgs[1]);
            } else {
                to = Integer.parseInt(hyphenArgs[1]);
            }
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheHour) {
                    return ((TheHour) cronExpression).andHour(from).toHour(to, interval);
                } else if (cronExpression instanceof Day) {
                    return ((Day) cronExpression).hour(from).toHour(to, interval);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyDay().hour(from)
                        .toHour(to, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitHour(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfMonth(String text, String filter,
            CronExpressionContext context) {
        if (text.matches("(\\d+\\-\\d+)(\\/\\d+)?") && (filter == null || filter.contains("-"))) {
            String[] hyphenArgs = text.split("\\-", 2);
            int interval = 1;
            int from = Integer.parseInt(hyphenArgs[0]);
            int to;
            if (hyphenArgs[1].contains("/")) {
                String[] slashArgs = hyphenArgs[1].split("\\/");
                to = Integer.parseInt(slashArgs[0]);
                interval = Integer.parseInt(slashArgs[1]);
            } else {
                to = Integer.parseInt(hyphenArgs[1]);
            }
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheDay) {
                    return ((TheDay) cronExpression).andDay(from).toDay(to, interval);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).day(from).toDay(to, interval);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).everyMonth().day(from)
                        .toDay(to, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitMonth(String text, String filter, CronExpressionContext context) {
        String monthRegex =
                "((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\\-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))(\\/\\d+)?";
        String numberRegex = "(\\d+\\-\\d+)(\\/\\d+)?";
        if ((text.matches(monthRegex) || text.matches(numberRegex))
                && (filter == null || filter.contains("-"))) {
            String[] hyphenArgs = text.split("\\-", 2);
            int interval = 1;
            int from;
            boolean useNumber = true;
            try {
                from = Integer.parseInt(hyphenArgs[0]);
            } catch (RuntimeException e) {
                from = AbbreviationUtils.getMonthValue(hyphenArgs[0]);
            }
            int to;
            if (hyphenArgs[1].contains("/")) {
                String[] slashArgs = hyphenArgs[1].split("\\/");
                interval = Integer.parseInt(slashArgs[1]);
                try {
                    to = Integer.parseInt(slashArgs[0]);
                } catch (RuntimeException e) {
                    to = AbbreviationUtils.getMonthValue(slashArgs[0]);
                    useNumber = false;
                }

            } else {
                try {
                    to = Integer.parseInt(hyphenArgs[1]);
                } catch (RuntimeException e) {
                    to = AbbreviationUtils.getMonthValue(hyphenArgs[1]);
                    useNumber = false;
                }
            }
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                cronExpression.getBuilder().setUseMonthAsNumber(useNumber);
                if (cronExpression instanceof TheMonth) {
                    return ((TheMonth) cronExpression).andMonth(from).toMonth(to, interval);
                } else if (cronExpression instanceof Year) {
                    return ((Year) cronExpression).month(from).toMonth(to, interval);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId())
                        .setUseMonthAsNumber(useNumber).everyYear().month(from)
                        .toMonth(to, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitMonth(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitDayOfWeek(String text, String filter,
            CronExpressionContext context) {
        if (text.matches(
                "(((SUN|MON|TUE|WED|THU|FRI|SAT)\\-(SUN|MON|TUE|WED|THU|FRI|SAT))|([1-7]\\-[1-7]))(\\/\\d+)?")
                && (filter == null || filter.contains("-"))) {
            String[] hyphenArgs = text.split("\\-", 2);
            int interval = 1;
            int from;
            boolean useNumber = true;
            try {
                from = Integer.parseInt(hyphenArgs[0]);
            } catch (RuntimeException e) {
                from = AbbreviationUtils.getDayOfWeekValue(hyphenArgs[0]);
                useNumber = false;
            }
            int to;
            if (hyphenArgs[1].contains("/")) {
                String[] slashArgs = hyphenArgs[1].split("\\/");
                try {
                    to = Integer.parseInt(slashArgs[0]);
                } catch (RuntimeException e) {
                    to = AbbreviationUtils.getDayOfWeekValue(slashArgs[0]);
                    useNumber = false;
                }
                interval = Integer.parseInt(slashArgs[1]);
            } else {
                try {
                    to = Integer.parseInt(hyphenArgs[1]);
                } catch (RuntimeException e) {
                    to = AbbreviationUtils.getDayOfWeekValue(hyphenArgs[1]);
                    useNumber = false;
                }
            }
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                cronExpression.getBuilder().setUseDayOfWeekAsNumber(useNumber);
                if (cronExpression instanceof TheDayOfWeek) {
                    return ((TheDayOfWeek) cronExpression).andDay(from).toDay(to, interval);
                } else if (cronExpression instanceof Week) {
                    return ((Week) cronExpression).day(from).toDay(to, interval);
                } else if (cronExpression instanceof Month) {
                    return ((Month) cronExpression).everyWeek().day(from).toDay(to, interval);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId())
                        .setUseDayOfWeekAsNumber(useNumber).everyWeek().day(from)
                        .toDay(to, interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitDayOfWeek(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public CronExpression visitYear(String text, String filter, CronExpressionContext context) {
        if (text.matches("(\\d{4}\\-\\d{4})(\\/\\d+)?")
                && (filter == null || filter.contains("-"))) {
            String[] hyphenArgs = text.split("\\-", 2);
            int interval = 1;
            int from = Integer.parseInt(hyphenArgs[0]);
            int to;
            if (hyphenArgs[1].contains("/")) {
                String[] slashArgs = hyphenArgs[1].split("\\/");
                to = Integer.parseInt(slashArgs[0]);
                interval = Integer.parseInt(slashArgs[1]);
            } else {
                to = Integer.parseInt(hyphenArgs[1]);
            }
            CronExpression cronExpression = context.getCronExpression();
            if (cronExpression != null) {
                if (cronExpression instanceof TheYear) {
                    return ((TheYear) cronExpression).andYear(from).toYear(to, interval);
                }
            } else {
                return new CronBuilder().setZoneId(context.getZoneId()).year(from).toYear(to,
                        interval);
            }
        } else if (nextVisitor != null) {
            return nextVisitor.visitYear(text, filter, context);
        }
        throw new UnsupportedTagException(text);
    }

    @Override
    public int getOrder() {
        return 4;
    }

}
