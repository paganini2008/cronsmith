// Generated from CronExpression.g4 by ANTLR 4.13.0

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
	 * Visit a parse tree produced by {@link CronExpressionParser#fieldList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldList(CronExpressionParser.FieldListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(CronExpressionParser.FieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#range}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange(CronExpressionParser.RangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#rangeWithStep}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRangeWithStep(CronExpressionParser.RangeWithStepContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#weekdayRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekdayRange(CronExpressionParser.WeekdayRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link CronExpressionParser#weekdayWithHash}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeekdayWithHash(CronExpressionParser.WeekdayWithHashContext ctx);
}