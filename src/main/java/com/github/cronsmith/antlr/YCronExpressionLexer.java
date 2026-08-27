// Generated from YCronExpression.g4 by ANTLR 4.13.1

package com.github.cronsmith.antlr;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class YCronExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, INT_YEAR=19, INT=20, INT_L=21, SPACE=22;
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
			"T__17", "INT_YEAR", "INT", "INT_L", "SPACE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','", "'*/'", "'/'", "'*'", "'#'", "'?'", "'L'", "'-'", "'W'", 
			"'LW'", "'L-'", "'SUN'", "'MON'", "'TUE'", "'WED'", "'THU'", "'FRI'", 
			"'SAT'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, "INT_YEAR", "INT", "INT_L", 
			"SPACE"
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


	public YCronExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "YCronExpression.g4"; }

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
		"\u0004\u0000\u0016x\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002"+
		"\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002"+
		"\u0015\u0007\u0015\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0004\u0013i\b\u0013\u000b\u0013\f\u0013j\u0001\u0014"+
		"\u0004\u0014n\b\u0014\u000b\u0014\f\u0014o\u0001\u0014\u0001\u0014\u0001"+
		"\u0015\u0004\u0015u\b\u0015\u000b\u0015\f\u0015v\u0000\u0000\u0016\u0001"+
		"\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007"+
		"\u000f\b\u0011\t\u0013\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d"+
		"\u000f\u001f\u0010!\u0011#\u0012%\u0013\'\u0014)\u0015+\u0016\u0001\u0000"+
		"\u0003\u0001\u000022\u0001\u000009\u0002\u0000\t\t  z\u0000\u0001\u0001"+
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
		"\u0000\u0000\u0001-\u0001\u0000\u0000\u0000\u0003/\u0001\u0000\u0000\u0000"+
		"\u00052\u0001\u0000\u0000\u0000\u00074\u0001\u0000\u0000\u0000\t6\u0001"+
		"\u0000\u0000\u0000\u000b8\u0001\u0000\u0000\u0000\r:\u0001\u0000\u0000"+
		"\u0000\u000f<\u0001\u0000\u0000\u0000\u0011>\u0001\u0000\u0000\u0000\u0013"+
		"@\u0001\u0000\u0000\u0000\u0015C\u0001\u0000\u0000\u0000\u0017F\u0001"+
		"\u0000\u0000\u0000\u0019J\u0001\u0000\u0000\u0000\u001bN\u0001\u0000\u0000"+
		"\u0000\u001dR\u0001\u0000\u0000\u0000\u001fV\u0001\u0000\u0000\u0000!"+
		"Z\u0001\u0000\u0000\u0000#^\u0001\u0000\u0000\u0000%b\u0001\u0000\u0000"+
		"\u0000\'h\u0001\u0000\u0000\u0000)m\u0001\u0000\u0000\u0000+t\u0001\u0000"+
		"\u0000\u0000-.\u0005,\u0000\u0000.\u0002\u0001\u0000\u0000\u0000/0\u0005"+
		"*\u0000\u000001\u0005/\u0000\u00001\u0004\u0001\u0000\u0000\u000023\u0005"+
		"/\u0000\u00003\u0006\u0001\u0000\u0000\u000045\u0005*\u0000\u00005\b\u0001"+
		"\u0000\u0000\u000067\u0005#\u0000\u00007\n\u0001\u0000\u0000\u000089\u0005"+
		"?\u0000\u00009\f\u0001\u0000\u0000\u0000:;\u0005L\u0000\u0000;\u000e\u0001"+
		"\u0000\u0000\u0000<=\u0005-\u0000\u0000=\u0010\u0001\u0000\u0000\u0000"+
		">?\u0005W\u0000\u0000?\u0012\u0001\u0000\u0000\u0000@A\u0005L\u0000\u0000"+
		"AB\u0005W\u0000\u0000B\u0014\u0001\u0000\u0000\u0000CD\u0005L\u0000\u0000"+
		"DE\u0005-\u0000\u0000E\u0016\u0001\u0000\u0000\u0000FG\u0005S\u0000\u0000"+
		"GH\u0005U\u0000\u0000HI\u0005N\u0000\u0000I\u0018\u0001\u0000\u0000\u0000"+
		"JK\u0005M\u0000\u0000KL\u0005O\u0000\u0000LM\u0005N\u0000\u0000M\u001a"+
		"\u0001\u0000\u0000\u0000NO\u0005T\u0000\u0000OP\u0005U\u0000\u0000PQ\u0005"+
		"E\u0000\u0000Q\u001c\u0001\u0000\u0000\u0000RS\u0005W\u0000\u0000ST\u0005"+
		"E\u0000\u0000TU\u0005D\u0000\u0000U\u001e\u0001\u0000\u0000\u0000VW\u0005"+
		"T\u0000\u0000WX\u0005H\u0000\u0000XY\u0005U\u0000\u0000Y \u0001\u0000"+
		"\u0000\u0000Z[\u0005F\u0000\u0000[\\\u0005R\u0000\u0000\\]\u0005I\u0000"+
		"\u0000]\"\u0001\u0000\u0000\u0000^_\u0005S\u0000\u0000_`\u0005A\u0000"+
		"\u0000`a\u0005T\u0000\u0000a$\u0001\u0000\u0000\u0000bc\u0007\u0000\u0000"+
		"\u0000cd\u0007\u0001\u0000\u0000de\u0007\u0001\u0000\u0000ef\u0007\u0001"+
		"\u0000\u0000f&\u0001\u0000\u0000\u0000gi\u0007\u0001\u0000\u0000hg\u0001"+
		"\u0000\u0000\u0000ij\u0001\u0000\u0000\u0000jh\u0001\u0000\u0000\u0000"+
		"jk\u0001\u0000\u0000\u0000k(\u0001\u0000\u0000\u0000ln\u0007\u0001\u0000"+
		"\u0000ml\u0001\u0000\u0000\u0000no\u0001\u0000\u0000\u0000om\u0001\u0000"+
		"\u0000\u0000op\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000qr\u0005"+
		"L\u0000\u0000r*\u0001\u0000\u0000\u0000su\u0007\u0002\u0000\u0000ts\u0001"+
		"\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000"+
		"vw\u0001\u0000\u0000\u0000w,\u0001\u0000\u0000\u0000\u0004\u0000jov\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}