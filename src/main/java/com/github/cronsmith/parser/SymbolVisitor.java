package com.github.cronsmith.parser;

import com.github.cronsmith.cron.CronExpression;

/**
 * 
 * @Description: SymbolVisitor
 * @Author: Fred Feng
 * @Date: 28/02/2025
 * @Version 1.0.0
 */
public interface SymbolVisitor {

    String getSymbol();

    CronExpression visitSecond(String text, String filter, CronExpressionContext context);

    CronExpression visitMinute(String text, String filter, CronExpressionContext context);

    CronExpression visitHour(String text, String filter, CronExpressionContext context);

    CronExpression visitDayOfMonth(String text, String filter, CronExpressionContext context);

    CronExpression visitMonth(String text, String filter, CronExpressionContext context);

    CronExpression visitDayOfWeek(String text, String filter, CronExpressionContext context);

    CronExpression visitYear(String text, String filter, CronExpressionContext context);

    void setNextVisitor(SymbolVisitor nextVisitor);

}
