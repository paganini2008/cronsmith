// Generated from CronExpression.g4 by ANTLR 4.9.3

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
	 * Enter a parse tree produced by {@link CronExpressionParser#secondField}.
	 * @param ctx the parse tree
	 */
	void enterSecondField(CronExpressionParser.SecondFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#secondField}.
	 * @param ctx the parse tree
	 */
	void exitSecondField(CronExpressionParser.SecondFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#minuteField}.
	 * @param ctx the parse tree
	 */
	void enterMinuteField(CronExpressionParser.MinuteFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#minuteField}.
	 * @param ctx the parse tree
	 */
	void exitMinuteField(CronExpressionParser.MinuteFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#hourField}.
	 * @param ctx the parse tree
	 */
	void enterHourField(CronExpressionParser.HourFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#hourField}.
	 * @param ctx the parse tree
	 */
	void exitHourField(CronExpressionParser.HourFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#dayOfMonthField}.
	 * @param ctx the parse tree
	 */
	void enterDayOfMonthField(CronExpressionParser.DayOfMonthFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#dayOfMonthField}.
	 * @param ctx the parse tree
	 */
	void exitDayOfMonthField(CronExpressionParser.DayOfMonthFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#monthField}.
	 * @param ctx the parse tree
	 */
	void enterMonthField(CronExpressionParser.MonthFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#monthField}.
	 * @param ctx the parse tree
	 */
	void exitMonthField(CronExpressionParser.MonthFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#dayOfWeekField}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeekField(CronExpressionParser.DayOfWeekFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#dayOfWeekField}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeekField(CronExpressionParser.DayOfWeekFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#yearField}.
	 * @param ctx the parse tree
	 */
	void enterYearField(CronExpressionParser.YearFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#yearField}.
	 * @param ctx the parse tree
	 */
	void exitYearField(CronExpressionParser.YearFieldContext ctx);
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
	 * Enter a parse tree produced by {@link CronExpressionParser#yearRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void enterYearRangeWithStep(CronExpressionParser.YearRangeWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#yearRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void exitYearRangeWithStep(CronExpressionParser.YearRangeWithStepContext ctx);
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
	 * Enter a parse tree produced by {@link CronExpressionParser#yearRange}.
	 * @param ctx the parse tree
	 */
	void enterYearRange(CronExpressionParser.YearRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#yearRange}.
	 * @param ctx the parse tree
	 */
	void exitYearRange(CronExpressionParser.YearRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#weekdayRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void enterWeekdayRangeWithStep(CronExpressionParser.WeekdayRangeWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#weekdayRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void exitWeekdayRangeWithStep(CronExpressionParser.WeekdayRangeWithStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#monthRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void enterMonthRangeWithStep(CronExpressionParser.MonthRangeWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#monthRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void exitMonthRangeWithStep(CronExpressionParser.MonthRangeWithStepContext ctx);
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
	 * Enter a parse tree produced by {@link CronExpressionParser#monthRange}.
	 * @param ctx the parse tree
	 */
	void enterMonthRange(CronExpressionParser.MonthRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#monthRange}.
	 * @param ctx the parse tree
	 */
	void exitMonthRange(CronExpressionParser.MonthRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#dayOfWeekName}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeekName(CronExpressionParser.DayOfWeekNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#dayOfWeekName}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeekName(CronExpressionParser.DayOfWeekNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link CronExpressionParser#monthName}.
	 * @param ctx the parse tree
	 */
	void enterMonthName(CronExpressionParser.MonthNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link CronExpressionParser#monthName}.
	 * @param ctx the parse tree
	 */
	void exitMonthName(CronExpressionParser.MonthNameContext ctx);
}