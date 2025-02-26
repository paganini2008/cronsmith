package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * TagVisitor to match and parse various tag like '/','L','W','#','-',',' and so on
 * 
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public interface TagVisitor {

    String getTag();

    CronExpression visitSecond(String text, String filter, CronExpressionContext context);

    CronExpression visitMinute(String text, String filter, CronExpressionContext context);

    CronExpression visitHour(String text, String filter, CronExpressionContext context);

    CronExpression visitDayOfMonth(String text, String filter, CronExpressionContext context);

    CronExpression visitMonth(String text, String filter, CronExpressionContext context);

    CronExpression visitDayOfWeek(String text, String filter, CronExpressionContext context);

    CronExpression visitYear(String text, String filter, CronExpressionContext context);

    void setNextVisitor(TagVisitor nextVisitor);

    int getOrder();

}
