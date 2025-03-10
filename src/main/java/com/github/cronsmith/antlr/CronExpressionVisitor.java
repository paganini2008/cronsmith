// Generated from CronExpression.g4 by ANTLR 4.9.3

package com.github.cronsmith.antlr;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CronExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CronExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#cron}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCron(CronExpressionParser.CronContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#second}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecond(CronExpressionParser.SecondContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#minute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinute(CronExpressionParser.MinuteContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#hour}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHour(CronExpressionParser.HourContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#dayOfMonth}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfMonth(CronExpressionParser.DayOfMonthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#month}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMonth(CronExpressionParser.MonthContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#dayOfWeek}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeek(CronExpressionParser.DayOfWeekContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#year}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYear(CronExpressionParser.YearContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#secondField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSecondField(CronExpressionParser.SecondFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#minuteField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinuteField(CronExpressionParser.MinuteFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#hourField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHourField(CronExpressionParser.HourFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#dayOfMonthField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfMonthField(CronExpressionParser.DayOfMonthFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#monthField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMonthField(CronExpressionParser.MonthFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#dayOfWeekField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeekField(CronExpressionParser.DayOfWeekFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#yearField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearField(CronExpressionParser.YearFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#rangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangeWithStep(CronExpressionParser.RangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#yearRangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearRangeWithStep(CronExpressionParser.YearRangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#range}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange(CronExpressionParser.RangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#yearRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearRange(CronExpressionParser.YearRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#weekdayRangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekdayRangeWithStep(CronExpressionParser.WeekdayRangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#monthRangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMonthRangeWithStep(CronExpressionParser.MonthRangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#weekdayRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekdayRange(CronExpressionParser.WeekdayRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#monthRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMonthRange(CronExpressionParser.MonthRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#dayOfWeekName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayOfWeekName(CronExpressionParser.DayOfWeekNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#monthName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMonthName(CronExpressionParser.MonthNameContext ctx);
}