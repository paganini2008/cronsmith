// Generated from CronExpression.g4 by ANTLR 4.13.0

package com.github.cronsmith.antlr;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class CronExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, STAR_SLASH=9, 
		INT_SLASH=10, WEEKDAY=11, INT=12, INT_L=13, SPACE=14;
	public static final int
		RULE_cron = 0, RULE_second = 1, RULE_minute = 2, RULE_hour = 3, RULE_dayOfMonth = 4, 
		RULE_month = 5, RULE_dayOfWeek = 6, RULE_year = 7, RULE_fieldList = 8, 
		RULE_field = 9, RULE_range = 10, RULE_rangeWithStep = 11, RULE_weekdayRange = 12, 
		RULE_weekdayWithHash = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"cron", "second", "minute", "hour", "dayOfMonth", "month", "dayOfWeek", 
			"year", "fieldList", "field", "range", "rangeWithStep", "weekdayRange", 
			"weekdayWithHash"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'?'", "'L'", "'W'", "','", "'*'", "'-'", "'/'", "'#'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "STAR_SLASH", "INT_SLASH", 
			"WEEKDAY", "INT", "INT_L", "SPACE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CronExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CronExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CronContext extends ParserRuleContext {
		public SecondContext second() {
			return getRuleContext(SecondContext.class,0);
		}
		public List<TerminalNode> SPACE() { return getTokens(CronExpressionParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(CronExpressionParser.SPACE, i);
		}
		public MinuteContext minute() {
			return getRuleContext(MinuteContext.class,0);
		}
		public HourContext hour() {
			return getRuleContext(HourContext.class,0);
		}
		public DayOfMonthContext dayOfMonth() {
			return getRuleContext(DayOfMonthContext.class,0);
		}
		public MonthContext month() {
			return getRuleContext(MonthContext.class,0);
		}
		public DayOfWeekContext dayOfWeek() {
			return getRuleContext(DayOfWeekContext.class,0);
		}
		public TerminalNode EOF() { return getToken(CronExpressionParser.EOF, 0); }
		public YearContext year() {
			return getRuleContext(YearContext.class,0);
		}
		public CronContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cron; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterCron(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitCron(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitCron(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CronContext cron() throws RecognitionException {
		CronContext _localctx = new CronContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_cron);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			second();
			setState(29);
			match(SPACE);
			setState(30);
			minute();
			setState(31);
			match(SPACE);
			setState(32);
			hour();
			setState(33);
			match(SPACE);
			setState(34);
			dayOfMonth();
			setState(35);
			match(SPACE);
			setState(36);
			month();
			setState(37);
			match(SPACE);
			setState(38);
			dayOfWeek();
			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SPACE) {
				{
				setState(39);
				match(SPACE);
				}
			}

			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 7716L) != 0)) {
				{
				setState(42);
				year();
				}
			}

			setState(45);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SecondContext extends ParserRuleContext {
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public SecondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_second; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterSecond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitSecond(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitSecond(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SecondContext second() throws RecognitionException {
		SecondContext _localctx = new SecondContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_second);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			fieldList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MinuteContext extends ParserRuleContext {
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public MinuteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterMinute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitMinute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitMinute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MinuteContext minute() throws RecognitionException {
		MinuteContext _localctx = new MinuteContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_minute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(49);
			fieldList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HourContext extends ParserRuleContext {
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public HourContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hour; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterHour(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitHour(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitHour(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HourContext hour() throws RecognitionException {
		HourContext _localctx = new HourContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_hour);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			fieldList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DayOfMonthContext extends ParserRuleContext {
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public TerminalNode INT() { return getToken(CronExpressionParser.INT, 0); }
		public DayOfMonthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dayOfMonth; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterDayOfMonth(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitDayOfMonth(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitDayOfMonth(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DayOfMonthContext dayOfMonth() throws RecognitionException {
		DayOfMonthContext _localctx = new DayOfMonthContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_dayOfMonth);
		try {
			setState(58);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(53);
				fieldList();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(54);
				match(T__0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(55);
				match(T__1);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(56);
				match(INT);
				setState(57);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MonthContext extends ParserRuleContext {
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public MonthContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_month; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterMonth(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitMonth(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitMonth(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MonthContext month() throws RecognitionException {
		MonthContext _localctx = new MonthContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_month);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			fieldList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DayOfWeekContext extends ParserRuleContext {
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public WeekdayWithHashContext weekdayWithHash() {
			return getRuleContext(WeekdayWithHashContext.class,0);
		}
		public TerminalNode INT_L() { return getToken(CronExpressionParser.INT_L, 0); }
		public DayOfWeekContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dayOfWeek; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterDayOfWeek(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitDayOfWeek(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitDayOfWeek(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DayOfWeekContext dayOfWeek() throws RecognitionException {
		DayOfWeekContext _localctx = new DayOfWeekContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_dayOfWeek);
		try {
			setState(66);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(62);
				fieldList();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(63);
				match(T__0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(64);
				weekdayWithHash();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(65);
				match(INT_L);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class YearContext extends ParserRuleContext {
		public FieldListContext fieldList() {
			return getRuleContext(FieldListContext.class,0);
		}
		public YearContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_year; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterYear(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitYear(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitYear(this);
			else return visitor.visitChildren(this);
		}
	}

	public final YearContext year() throws RecognitionException {
		YearContext _localctx = new YearContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_year);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
			fieldList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldListContext extends ParserRuleContext {
		public List<FieldContext> field() {
			return getRuleContexts(FieldContext.class);
		}
		public FieldContext field(int i) {
			return getRuleContext(FieldContext.class,i);
		}
		public FieldListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterFieldList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitFieldList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitFieldList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldListContext fieldList() throws RecognitionException {
		FieldListContext _localctx = new FieldListContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_fieldList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			field();
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__3) {
				{
				{
				setState(71);
				match(T__3);
				setState(72);
				field();
				}
				}
				setState(77);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(CronExpressionParser.INT, 0); }
		public TerminalNode STAR_SLASH() { return getToken(CronExpressionParser.STAR_SLASH, 0); }
		public RangeContext range() {
			return getRuleContext(RangeContext.class,0);
		}
		public RangeWithStepContext rangeWithStep() {
			return getRuleContext(RangeWithStepContext.class,0);
		}
		public WeekdayRangeContext weekdayRange() {
			return getRuleContext(WeekdayRangeContext.class,0);
		}
		public TerminalNode INT_SLASH() { return getToken(CronExpressionParser.INT_SLASH, 0); }
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_field);
		try {
			setState(90);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(78);
				match(T__4);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(79);
				match(INT);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(80);
				match(STAR_SLASH);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(81);
				range();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(82);
				rangeWithStep();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(83);
				weekdayRange();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(84);
				match(INT_SLASH);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(85);
				match(T__1);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(86);
				match(INT);
				setState(87);
				match(T__1);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(88);
				match(INT);
				setState(89);
				match(T__2);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RangeContext extends ParserRuleContext {
		public List<TerminalNode> INT() { return getTokens(CronExpressionParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(CronExpressionParser.INT, i);
		}
		public RangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitRange(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeContext range() throws RecognitionException {
		RangeContext _localctx = new RangeContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_range);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			match(INT);
			setState(93);
			match(T__5);
			setState(94);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RangeWithStepContext extends ParserRuleContext {
		public RangeContext range() {
			return getRuleContext(RangeContext.class,0);
		}
		public TerminalNode INT() { return getToken(CronExpressionParser.INT, 0); }
		public RangeWithStepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rangeWithStep; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterRangeWithStep(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitRangeWithStep(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitRangeWithStep(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RangeWithStepContext rangeWithStep() throws RecognitionException {
		RangeWithStepContext _localctx = new RangeWithStepContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_rangeWithStep);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			range();
			setState(97);
			match(T__6);
			setState(98);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WeekdayRangeContext extends ParserRuleContext {
		public List<TerminalNode> WEEKDAY() { return getTokens(CronExpressionParser.WEEKDAY); }
		public TerminalNode WEEKDAY(int i) {
			return getToken(CronExpressionParser.WEEKDAY, i);
		}
		public WeekdayRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_weekdayRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterWeekdayRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitWeekdayRange(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitWeekdayRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WeekdayRangeContext weekdayRange() throws RecognitionException {
		WeekdayRangeContext _localctx = new WeekdayRangeContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_weekdayRange);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(WEEKDAY);
			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__5) {
				{
				setState(101);
				match(T__5);
				setState(102);
				match(WEEKDAY);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WeekdayWithHashContext extends ParserRuleContext {
		public TerminalNode WEEKDAY() { return getToken(CronExpressionParser.WEEKDAY, 0); }
		public TerminalNode INT() { return getToken(CronExpressionParser.INT, 0); }
		public WeekdayWithHashContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_weekdayWithHash; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).enterWeekdayWithHash(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CronExpressionListener ) ((CronExpressionListener)listener).exitWeekdayWithHash(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CronExpressionVisitor ) return ((CronExpressionVisitor<? extends T>)visitor).visitWeekdayWithHash(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WeekdayWithHashContext weekdayWithHash() throws RecognitionException {
		WeekdayWithHashContext _localctx = new WeekdayWithHashContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_weekdayWithHash);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			match(WEEKDAY);
			setState(106);
			match(T__7);
			setState(107);
			match(INT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u000en\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0003\u0000)\b\u0000\u0001\u0000\u0003\u0000"+
		",\b\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0003\u0004;\b\u0004\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006C\b\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0005\bJ\b\b\n\b\f\b"+
		"M\t\b\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t[\b\t\u0001\n\u0001\n\u0001\n"+
		"\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001"+
		"\f\u0001\f\u0003\fh\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0000"+
		"\u0000\u000e\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u0000\u0000r\u0000\u001c\u0001\u0000\u0000\u0000\u0002/\u0001"+
		"\u0000\u0000\u0000\u00041\u0001\u0000\u0000\u0000\u00063\u0001\u0000\u0000"+
		"\u0000\b:\u0001\u0000\u0000\u0000\n<\u0001\u0000\u0000\u0000\fB\u0001"+
		"\u0000\u0000\u0000\u000eD\u0001\u0000\u0000\u0000\u0010F\u0001\u0000\u0000"+
		"\u0000\u0012Z\u0001\u0000\u0000\u0000\u0014\\\u0001\u0000\u0000\u0000"+
		"\u0016`\u0001\u0000\u0000\u0000\u0018d\u0001\u0000\u0000\u0000\u001ai"+
		"\u0001\u0000\u0000\u0000\u001c\u001d\u0003\u0002\u0001\u0000\u001d\u001e"+
		"\u0005\u000e\u0000\u0000\u001e\u001f\u0003\u0004\u0002\u0000\u001f \u0005"+
		"\u000e\u0000\u0000 !\u0003\u0006\u0003\u0000!\"\u0005\u000e\u0000\u0000"+
		"\"#\u0003\b\u0004\u0000#$\u0005\u000e\u0000\u0000$%\u0003\n\u0005\u0000"+
		"%&\u0005\u000e\u0000\u0000&(\u0003\f\u0006\u0000\')\u0005\u000e\u0000"+
		"\u0000(\'\u0001\u0000\u0000\u0000()\u0001\u0000\u0000\u0000)+\u0001\u0000"+
		"\u0000\u0000*,\u0003\u000e\u0007\u0000+*\u0001\u0000\u0000\u0000+,\u0001"+
		"\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-.\u0005\u0000\u0000\u0001"+
		".\u0001\u0001\u0000\u0000\u0000/0\u0003\u0010\b\u00000\u0003\u0001\u0000"+
		"\u0000\u000012\u0003\u0010\b\u00002\u0005\u0001\u0000\u0000\u000034\u0003"+
		"\u0010\b\u00004\u0007\u0001\u0000\u0000\u00005;\u0003\u0010\b\u00006;"+
		"\u0005\u0001\u0000\u00007;\u0005\u0002\u0000\u000089\u0005\f\u0000\u0000"+
		"9;\u0005\u0003\u0000\u0000:5\u0001\u0000\u0000\u0000:6\u0001\u0000\u0000"+
		"\u0000:7\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000;\t\u0001\u0000"+
		"\u0000\u0000<=\u0003\u0010\b\u0000=\u000b\u0001\u0000\u0000\u0000>C\u0003"+
		"\u0010\b\u0000?C\u0005\u0001\u0000\u0000@C\u0003\u001a\r\u0000AC\u0005"+
		"\r\u0000\u0000B>\u0001\u0000\u0000\u0000B?\u0001\u0000\u0000\u0000B@\u0001"+
		"\u0000\u0000\u0000BA\u0001\u0000\u0000\u0000C\r\u0001\u0000\u0000\u0000"+
		"DE\u0003\u0010\b\u0000E\u000f\u0001\u0000\u0000\u0000FK\u0003\u0012\t"+
		"\u0000GH\u0005\u0004\u0000\u0000HJ\u0003\u0012\t\u0000IG\u0001\u0000\u0000"+
		"\u0000JM\u0001\u0000\u0000\u0000KI\u0001\u0000\u0000\u0000KL\u0001\u0000"+
		"\u0000\u0000L\u0011\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000"+
		"N[\u0005\u0005\u0000\u0000O[\u0005\f\u0000\u0000P[\u0005\t\u0000\u0000"+
		"Q[\u0003\u0014\n\u0000R[\u0003\u0016\u000b\u0000S[\u0003\u0018\f\u0000"+
		"T[\u0005\n\u0000\u0000U[\u0005\u0002\u0000\u0000VW\u0005\f\u0000\u0000"+
		"W[\u0005\u0002\u0000\u0000XY\u0005\f\u0000\u0000Y[\u0005\u0003\u0000\u0000"+
		"ZN\u0001\u0000\u0000\u0000ZO\u0001\u0000\u0000\u0000ZP\u0001\u0000\u0000"+
		"\u0000ZQ\u0001\u0000\u0000\u0000ZR\u0001\u0000\u0000\u0000ZS\u0001\u0000"+
		"\u0000\u0000ZT\u0001\u0000\u0000\u0000ZU\u0001\u0000\u0000\u0000ZV\u0001"+
		"\u0000\u0000\u0000ZX\u0001\u0000\u0000\u0000[\u0013\u0001\u0000\u0000"+
		"\u0000\\]\u0005\f\u0000\u0000]^\u0005\u0006\u0000\u0000^_\u0005\f\u0000"+
		"\u0000_\u0015\u0001\u0000\u0000\u0000`a\u0003\u0014\n\u0000ab\u0005\u0007"+
		"\u0000\u0000bc\u0005\f\u0000\u0000c\u0017\u0001\u0000\u0000\u0000dg\u0005"+
		"\u000b\u0000\u0000ef\u0005\u0006\u0000\u0000fh\u0005\u000b\u0000\u0000"+
		"ge\u0001\u0000\u0000\u0000gh\u0001\u0000\u0000\u0000h\u0019\u0001\u0000"+
		"\u0000\u0000ij\u0005\u000b\u0000\u0000jk\u0005\b\u0000\u0000kl\u0005\f"+
		"\u0000\u0000l\u001b\u0001\u0000\u0000\u0000\u0007(+:BKZg";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}