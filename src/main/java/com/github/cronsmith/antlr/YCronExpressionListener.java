// Generated from YCronExpression.g4 by ANTLR 4.13.1

package com.github.cronsmith.antlr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link YCronExpressionParser}.
 */
public interface YCronExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#ycron}.
	 * @param ctx the parse tree
	 */
	void enterYcron(YCronExpressionParser.YcronContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#ycron}.
	 * @param ctx the parse tree
	 */
	void exitYcron(YCronExpressionParser.YcronContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#second}.
	 * @param ctx the parse tree
	 */
	void enterSecond(YCronExpressionParser.SecondContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#second}.
	 * @param ctx the parse tree
	 */
	void exitSecond(YCronExpressionParser.SecondContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#minute}.
	 * @param ctx the parse tree
	 */
	void enterMinute(YCronExpressionParser.MinuteContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#minute}.
	 * @param ctx the parse tree
	 */
	void exitMinute(YCronExpressionParser.MinuteContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#hour}.
	 * @param ctx the parse tree
	 */
	void enterHour(YCronExpressionParser.HourContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#hour}.
	 * @param ctx the parse tree
	 */
	void exitHour(YCronExpressionParser.HourContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#dayOfWeek}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeek(YCronExpressionParser.DayOfWeekContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#dayOfWeek}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeek(YCronExpressionParser.DayOfWeekContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#weekOfYear}.
	 * @param ctx the parse tree
	 */
	void enterWeekOfYear(YCronExpressionParser.WeekOfYearContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#weekOfYear}.
	 * @param ctx the parse tree
	 */
	void exitWeekOfYear(YCronExpressionParser.WeekOfYearContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#dayOfYear}.
	 * @param ctx the parse tree
	 */
	void enterDayOfYear(YCronExpressionParser.DayOfYearContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#dayOfYear}.
	 * @param ctx the parse tree
	 */
	void exitDayOfYear(YCronExpressionParser.DayOfYearContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#year}.
	 * @param ctx the parse tree
	 */
	void enterYear(YCronExpressionParser.YearContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#year}.
	 * @param ctx the parse tree
	 */
	void exitYear(YCronExpressionParser.YearContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#secondField}.
	 * @param ctx the parse tree
	 */
	void enterSecondField(YCronExpressionParser.SecondFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#secondField}.
	 * @param ctx the parse tree
	 */
	void exitSecondField(YCronExpressionParser.SecondFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#minuteField}.
	 * @param ctx the parse tree
	 */
	void enterMinuteField(YCronExpressionParser.MinuteFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#minuteField}.
	 * @param ctx the parse tree
	 */
	void exitMinuteField(YCronExpressionParser.MinuteFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#hourField}.
	 * @param ctx the parse tree
	 */
	void enterHourField(YCronExpressionParser.HourFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#hourField}.
	 * @param ctx the parse tree
	 */
	void exitHourField(YCronExpressionParser.HourFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#dayOfWeekField}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeekField(YCronExpressionParser.DayOfWeekFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#dayOfWeekField}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeekField(YCronExpressionParser.DayOfWeekFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#weekOfYearField}.
	 * @param ctx the parse tree
	 */
	void enterWeekOfYearField(YCronExpressionParser.WeekOfYearFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#weekOfYearField}.
	 * @param ctx the parse tree
	 */
	void exitWeekOfYearField(YCronExpressionParser.WeekOfYearFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#dayOfYearField}.
	 * @param ctx the parse tree
	 */
	void enterDayOfYearField(YCronExpressionParser.DayOfYearFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#dayOfYearField}.
	 * @param ctx the parse tree
	 */
	void exitDayOfYearField(YCronExpressionParser.DayOfYearFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#yearField}.
	 * @param ctx the parse tree
	 */
	void enterYearField(YCronExpressionParser.YearFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#yearField}.
	 * @param ctx the parse tree
	 */
	void exitYearField(YCronExpressionParser.YearFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#rangeWithStep}.
	 * @param ctx the parse tree
	 */
	void enterRangeWithStep(YCronExpressionParser.RangeWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#rangeWithStep}.
	 * @param ctx the parse tree
	 */
	void exitRangeWithStep(YCronExpressionParser.RangeWithStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#yearRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void enterYearRangeWithStep(YCronExpressionParser.YearRangeWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#yearRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void exitYearRangeWithStep(YCronExpressionParser.YearRangeWithStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#range}.
	 * @param ctx the parse tree
	 */
	void enterRange(YCronExpressionParser.RangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#range}.
	 * @param ctx the parse tree
	 */
	void exitRange(YCronExpressionParser.RangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#yearRange}.
	 * @param ctx the parse tree
	 */
	void enterYearRange(YCronExpressionParser.YearRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#yearRange}.
	 * @param ctx the parse tree
	 */
	void exitYearRange(YCronExpressionParser.YearRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#weekdayRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void enterWeekdayRangeWithStep(YCronExpressionParser.WeekdayRangeWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#weekdayRangeWithStep}.
	 * @param ctx the parse tree
	 */
	void exitWeekdayRangeWithStep(YCronExpressionParser.WeekdayRangeWithStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#dayOfWeekNameLast}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeekNameLast(YCronExpressionParser.DayOfWeekNameLastContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#dayOfWeekNameLast}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeekNameLast(YCronExpressionParser.DayOfWeekNameLastContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#dayOfWeekNameWithStep}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeekNameWithStep(YCronExpressionParser.DayOfWeekNameWithStepContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#dayOfWeekNameWithStep}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeekNameWithStep(YCronExpressionParser.DayOfWeekNameWithStepContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#weekdayRange}.
	 * @param ctx the parse tree
	 */
	void enterWeekdayRange(YCronExpressionParser.WeekdayRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#weekdayRange}.
	 * @param ctx the parse tree
	 */
	void exitWeekdayRange(YCronExpressionParser.WeekdayRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link YCronExpressionParser#dayOfWeekName}.
	 * @param ctx the parse tree
	 */
	void enterDayOfWeekName(YCronExpressionParser.DayOfWeekNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link YCronExpressionParser#dayOfWeekName}.
	 * @param ctx the parse tree
	 */
	void exitDayOfWeekName(YCronExpressionParser.DayOfWeekNameContext ctx);
}