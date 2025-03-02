// Generated from CronExpression.g4 by ANTLR 4.13.0

package com.github.cronsmith.antlr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CronExpressionParser}.
 */
public interface CronExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#cron}.
	 * @param ctx the parse tree
	 */
	void enterCron(CronExpressionParser.CronContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#cron}.
	 * @param ctx the parse tree
	 */
	void exitCron(CronExpressionParser.CronContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#second}.
	 * @param ctx the parse tree
	 */
	void enterSecond(CronExpressionParser.SecondContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#second}.
	 * @param ctx the parse tree
	 */
	void exitSecond(CronExpressionParser.SecondContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#minute}.
	 * @param ctx the parse tree
	 */
	void enterMinute(CronExpressionParser.MinuteContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#minute}.
	 * @param ctx the parse tree
	 */
	void exitMinute(CronExpressionParser.MinuteContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#hour}.
	 * @param ctx the parse tree
	 */
	void enterHour(CronExpressionParser.HourContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#hour}.
	 * @param ctx the parse tree
	 */
	void exitHour(CronExpressionParser.HourContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#dayOfMonth}.
	 * @param ctx the parse tree
	 */
	void enterDayOfMonth(CronExpressionParser.DayOfMonthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#dayOfMonth}.
	 * @param ctx the parse tree
	 */
	void exitDayOfMonth(CronExpressionParser.DayOfMonthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#month}.
	 * @param ctx the parse tree
	 */
	void enterMonth(CronExpressionParser.MonthContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#month}.
	 * @param ctx the parse tree
	 */
	void exitMonth(CronExpressionParser.MonthContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#dayOfWeek}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeek(CronExpressionParser.DayOfWeekContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#dayOfWeek}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeek(CronExpressionParser.DayOfWeekContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#year}.
	 * @param ctx the parse tree
	 */
	void enterYear(CronExpressionParser.YearContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#year}.
	 * @param ctx the parse tree
	 */
	void exitYear(CronExpressionParser.YearContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void enterFieldList(CronExpressionParser.FieldListContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void exitFieldList(CronExpressionParser.FieldListContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#field}.
	 * @param ctx the parse tree
	 */
	void enterField(CronExpressionParser.FieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#field}.
	 * @param ctx the parse tree
	 */
	void exitField(CronExpressionParser.FieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#range}.
	 * @param ctx the parse tree
	 */
	void enterRange(CronExpressionParser.RangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#range}.
	 * @param ctx the parse tree
	 */
	void exitRange(CronExpressionParser.RangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#rangeWithStep}.
	 * @param ctx the parse tree
	 */
	void enterRangeWithStep(CronExpressionParser.RangeWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#rangeWithStep}.
	 * @param ctx the parse tree
	 */
	void exitRangeWithStep(CronExpressionParser.RangeWithStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#weekdayRange}.
	 * @param ctx the parse tree
	 */
	void enterWeekdayRange(CronExpressionParser.WeekdayRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#weekdayRange}.
	 * @param ctx the parse tree
	 */
	void exitWeekdayRange(CronExpressionParser.WeekdayRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#weekdayWithHash}.
	 * @param ctx the parse tree
	 */
	void enterWeekdayWithHash(CronExpressionParser.WeekdayWithHashContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#weekdayWithHash}.
	 * @param ctx the parse tree
	 */
	void exitWeekdayWithHash(CronExpressionParser.WeekdayWithHashContext ctx);
}