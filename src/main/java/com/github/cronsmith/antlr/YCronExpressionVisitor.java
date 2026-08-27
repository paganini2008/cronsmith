// Generated from YCronExpression.g4 by ANTLR 4.13.1

package com.github.cronsmith.antlr;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link YCronExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface YCronExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#ycron}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYcron(YCronExpressionParser.YcronContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#second}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecond(YCronExpressionParser.SecondContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#minute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinute(YCronExpressionParser.MinuteContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#hour}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHour(YCronExpressionParser.HourContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#dayOfWeek}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeek(YCronExpressionParser.DayOfWeekContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#weekOfYear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekOfYear(YCronExpressionParser.WeekOfYearContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#dayOfYear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfYear(YCronExpressionParser.DayOfYearContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#year}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYear(YCronExpressionParser.YearContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#secondField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecondField(YCronExpressionParser.SecondFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#minuteField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinuteField(YCronExpressionParser.MinuteFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#hourField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHourField(YCronExpressionParser.HourFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#dayOfWeekField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeekField(YCronExpressionParser.DayOfWeekFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#weekOfYearField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekOfYearField(YCronExpressionParser.WeekOfYearFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#dayOfYearField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfYearField(YCronExpressionParser.DayOfYearFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#yearField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearField(YCronExpressionParser.YearFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#rangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangeWithStep(YCronExpressionParser.RangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#yearRangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearRangeWithStep(YCronExpressionParser.YearRangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#range}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange(YCronExpressionParser.RangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#yearRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearRange(YCronExpressionParser.YearRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#weekdayRangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekdayRangeWithStep(YCronExpressionParser.WeekdayRangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#dayOfWeekNameLast}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeekNameLast(YCronExpressionParser.DayOfWeekNameLastContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#dayOfWeekNameWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeekNameWithStep(YCronExpressionParser.DayOfWeekNameWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#weekdayRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekdayRange(YCronExpressionParser.WeekdayRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link YCronExpressionParser#dayOfWeekName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeekName(YCronExpressionParser.DayOfWeekNameContext ctx);
}