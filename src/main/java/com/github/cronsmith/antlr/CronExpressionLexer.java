// Generated from CronExpression.g4 by ANTLR 4.13.0

package com.github.cronsmith.antlr;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class CronExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, INT_YEAR=29, INT=30, INT_L=31, 
		SPACE=32;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
			"T__25", "T__26", "T__27", "INT_YEAR", "INT", "INT_L", "SPACE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','", "'/'", "'*'", "'?'", "'LW'", "'L'", "'W'", "'#'", "'-'", 
			"'SUN'", "'MON'", "'TUE'", "'WED'", "'THU'", "'FRI'", "'SAT'", "'JAN'", 
			"'FEB'", "'MAR'", "'APR'", "'MAY'", "'JUN'", "'JUL'", "'AUG'", "'SEP'", 
			"'OCT'", "'NOV'", "'DEC'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, "INT_YEAR", "INT", "INT_L", "SPACE"
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


	public CronExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CronExpression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000 \u00b8\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002"+
		"\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002"+
		"\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002"+
		"\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002"+
		"\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002"+
		"\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001d\u0004\u001d\u00a7\b\u001d\u000b\u001d"+
		"\f\u001d\u00a8\u0001\u001e\u0004\u001e\u00ac\b\u001e\u000b\u001e\f\u001e"+
		"\u00ad\u0001\u001e\u0001\u001e\u0001\u001f\u0004\u001f\u00b3\b\u001f\u000b"+
		"\u001f\f\u001f\u00b4\u0001\u001f\u0001\u001f\u0000\u0000 \u0001\u0001"+
		"\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f"+
		"\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f"+
		"\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017/\u0018"+
		"1\u00193\u001a5\u001b7\u001c9\u001d;\u001e=\u001f? \u0001\u0000\u0003"+
		"\u0001\u000022\u0001\u000009\u0002\u0000\t\t  \u00ba\u0000\u0001\u0001"+
		"\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001"+
		"\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000"+
		"\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000"+
		"\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000"+
		"\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000"+
		"\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000"+
		"\u0000\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000"+
		"\u0000\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000"+
		"\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'"+
		"\u0001\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000"+
		"\u0000\u0000\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000"+
		"\u00001\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00005"+
		"\u0001\u0000\u0000\u0000\u00007\u0001\u0000\u0000\u0000\u00009\u0001\u0000"+
		"\u0000\u0000\u0000;\u0001\u0000\u0000\u0000\u0000=\u0001\u0000\u0000\u0000"+
		"\u0000?\u0001\u0000\u0000\u0000\u0001A\u0001\u0000\u0000\u0000\u0003C"+
		"\u0001\u0000\u0000\u0000\u0005E\u0001\u0000\u0000\u0000\u0007G\u0001\u0000"+
		"\u0000\u0000\tI\u0001\u0000\u0000\u0000\u000bL\u0001\u0000\u0000\u0000"+
		"\rN\u0001\u0000\u0000\u0000\u000fP\u0001\u0000\u0000\u0000\u0011R\u0001"+
		"\u0000\u0000\u0000\u0013T\u0001\u0000\u0000\u0000\u0015X\u0001\u0000\u0000"+
		"\u0000\u0017\\\u0001\u0000\u0000\u0000\u0019`\u0001\u0000\u0000\u0000"+
		"\u001bd\u0001\u0000\u0000\u0000\u001dh\u0001\u0000\u0000\u0000\u001fl"+
		"\u0001\u0000\u0000\u0000!p\u0001\u0000\u0000\u0000#t\u0001\u0000\u0000"+
		"\u0000%x\u0001\u0000\u0000\u0000\'|\u0001\u0000\u0000\u0000)\u0080\u0001"+
		"\u0000\u0000\u0000+\u0084\u0001\u0000\u0000\u0000-\u0088\u0001\u0000\u0000"+
		"\u0000/\u008c\u0001\u0000\u0000\u00001\u0090\u0001\u0000\u0000\u00003"+
		"\u0094\u0001\u0000\u0000\u00005\u0098\u0001\u0000\u0000\u00007\u009c\u0001"+
		"\u0000\u0000\u00009\u00a0\u0001\u0000\u0000\u0000;\u00a6\u0001\u0000\u0000"+
		"\u0000=\u00ab\u0001\u0000\u0000\u0000?\u00b2\u0001\u0000\u0000\u0000A"+
		"B\u0005,\u0000\u0000B\u0002\u0001\u0000\u0000\u0000CD\u0005/\u0000\u0000"+
		"D\u0004\u0001\u0000\u0000\u0000EF\u0005*\u0000\u0000F\u0006\u0001\u0000"+
		"\u0000\u0000GH\u0005?\u0000\u0000H\b\u0001\u0000\u0000\u0000IJ\u0005L"+
		"\u0000\u0000JK\u0005W\u0000\u0000K\n\u0001\u0000\u0000\u0000LM\u0005L"+
		"\u0000\u0000M\f\u0001\u0000\u0000\u0000NO\u0005W\u0000\u0000O\u000e\u0001"+
		"\u0000\u0000\u0000PQ\u0005#\u0000\u0000Q\u0010\u0001\u0000\u0000\u0000"+
		"RS\u0005-\u0000\u0000S\u0012\u0001\u0000\u0000\u0000TU\u0005S\u0000\u0000"+
		"UV\u0005U\u0000\u0000VW\u0005N\u0000\u0000W\u0014\u0001\u0000\u0000\u0000"+
		"XY\u0005M\u0000\u0000YZ\u0005O\u0000\u0000Z[\u0005N\u0000\u0000[\u0016"+
		"\u0001\u0000\u0000\u0000\\]\u0005T\u0000\u0000]^\u0005U\u0000\u0000^_"+
		"\u0005E\u0000\u0000_\u0018\u0001\u0000\u0000\u0000`a\u0005W\u0000\u0000"+
		"ab\u0005E\u0000\u0000bc\u0005D\u0000\u0000c\u001a\u0001\u0000\u0000\u0000"+
		"de\u0005T\u0000\u0000ef\u0005H\u0000\u0000fg\u0005U\u0000\u0000g\u001c"+
		"\u0001\u0000\u0000\u0000hi\u0005F\u0000\u0000ij\u0005R\u0000\u0000jk\u0005"+
		"I\u0000\u0000k\u001e\u0001\u0000\u0000\u0000lm\u0005S\u0000\u0000mn\u0005"+
		"A\u0000\u0000no\u0005T\u0000\u0000o \u0001\u0000\u0000\u0000pq\u0005J"+
		"\u0000\u0000qr\u0005A\u0000\u0000rs\u0005N\u0000\u0000s\"\u0001\u0000"+
		"\u0000\u0000tu\u0005F\u0000\u0000uv\u0005E\u0000\u0000vw\u0005B\u0000"+
		"\u0000w$\u0001\u0000\u0000\u0000xy\u0005M\u0000\u0000yz\u0005A\u0000\u0000"+
		"z{\u0005R\u0000\u0000{&\u0001\u0000\u0000\u0000|}\u0005A\u0000\u0000}"+
		"~\u0005P\u0000\u0000~\u007f\u0005R\u0000\u0000\u007f(\u0001\u0000\u0000"+
		"\u0000\u0080\u0081\u0005M\u0000\u0000\u0081\u0082\u0005A\u0000\u0000\u0082"+
		"\u0083\u0005Y\u0000\u0000\u0083*\u0001\u0000\u0000\u0000\u0084\u0085\u0005"+
		"J\u0000\u0000\u0085\u0086\u0005U\u0000\u0000\u0086\u0087\u0005N\u0000"+
		"\u0000\u0087,\u0001\u0000\u0000\u0000\u0088\u0089\u0005J\u0000\u0000\u0089"+
		"\u008a\u0005U\u0000\u0000\u008a\u008b\u0005L\u0000\u0000\u008b.\u0001"+
		"\u0000\u0000\u0000\u008c\u008d\u0005A\u0000\u0000\u008d\u008e\u0005U\u0000"+
		"\u0000\u008e\u008f\u0005G\u0000\u0000\u008f0\u0001\u0000\u0000\u0000\u0090"+
		"\u0091\u0005S\u0000\u0000\u0091\u0092\u0005E\u0000\u0000\u0092\u0093\u0005"+
		"P\u0000\u0000\u00932\u0001\u0000\u0000\u0000\u0094\u0095\u0005O\u0000"+
		"\u0000\u0095\u0096\u0005C\u0000\u0000\u0096\u0097\u0005T\u0000\u0000\u0097"+
		"4\u0001\u0000\u0000\u0000\u0098\u0099\u0005N\u0000\u0000\u0099\u009a\u0005"+
		"O\u0000\u0000\u009a\u009b\u0005V\u0000\u0000\u009b6\u0001\u0000\u0000"+
		"\u0000\u009c\u009d\u0005D\u0000\u0000\u009d\u009e\u0005E\u0000\u0000\u009e"+
		"\u009f\u0005C\u0000\u0000\u009f8\u0001\u0000\u0000\u0000\u00a0\u00a1\u0007"+
		"\u0000\u0000\u0000\u00a1\u00a2\u0007\u0001\u0000\u0000\u00a2\u00a3\u0007"+
		"\u0001\u0000\u0000\u00a3\u00a4\u0007\u0001\u0000\u0000\u00a4:\u0001\u0000"+
		"\u0000\u0000\u00a5\u00a7\u0007\u0001\u0000\u0000\u00a6\u00a5\u0001\u0000"+
		"\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000\u0000\u00a8\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9<\u0001\u0000\u0000"+
		"\u0000\u00aa\u00ac\u0007\u0001\u0000\u0000\u00ab\u00aa\u0001\u0000\u0000"+
		"\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad\u00ab\u0001\u0000\u0000"+
		"\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u00af\u0001\u0000\u0000"+
		"\u0000\u00af\u00b0\u0005L\u0000\u0000\u00b0>\u0001\u0000\u0000\u0000\u00b1"+
		"\u00b3\u0007\u0002\u0000\u0000\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b3"+
		"\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000\u0000\u0000\u00b4"+
		"\u00b5\u0001\u0000\u0000\u0000\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6"+
		"\u00b7\u0006\u001f\u0000\u0000\u00b7@\u0001\u0000\u0000\u0000\u0004\u0000"+
		"\u00a8\u00ad\u00b4\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}