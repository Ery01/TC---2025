// Generated from com/compilador/Compilador.g4 by ANTLR 4.9.3
package com.compilador;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CompiladorLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PA=1, PC=2, CA=3, CC=4, LA=5, LC=6, PYC=7, COMA=8, IGUAL=9, MAYOR=10, 
		MAYOR_IGUAL=11, MENOR=12, MENOR_IGUAL=13, EQL=14, DISTINTO=15, SUM=16, 
		RES=17, MUL=18, DIV=19, MOD=20, OR=21, AND=22, NOT=23, FOR=24, WHILE=25, 
		BREAK=26, CONTINUE=27, IF=28, ELSE=29, INT=30, CHAR=31, DOUBLE=32, VOID=33, 
		TRUE=34, FALSE=35, BOOL=36, RETURN=37, ID=38, INTEGER=39, DECIMAL=40, 
		CHARACTER=41, STRING_LITERAL=42, COMENTARIO_LINEA=43, COMENTARIO_BLOQUE=44, 
		WS=45, OTRO=46;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"PA", "PC", "CA", "CC", "LA", "LC", "PYC", "COMA", "IGUAL", "MAYOR", 
			"MAYOR_IGUAL", "MENOR", "MENOR_IGUAL", "EQL", "DISTINTO", "SUM", "RES", 
			"MUL", "DIV", "MOD", "OR", "AND", "NOT", "FOR", "WHILE", "BREAK", "CONTINUE", 
			"IF", "ELSE", "INT", "CHAR", "DOUBLE", "VOID", "TRUE", "FALSE", "BOOL", 
			"RETURN", "ID", "INTEGER", "DECIMAL", "CHARACTER", "STRING_LITERAL", 
			"COMENTARIO_LINEA", "COMENTARIO_BLOQUE", "WS", "OTRO", "LETRA", "DIGITO"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'['", "']'", "'{'", "'}'", "';'", "','", "'='", 
			"'>'", "'>='", "'<'", "'<='", "'=='", "'!='", "'+'", "'-'", "'*'", "'/'", 
			"'%'", "'||'", "'&&'", "'!'", "'for'", "'while'", "'break'", "'continue'", 
			"'if'", "'else'", "'int'", "'char'", "'double'", "'void'", "'true'", 
			"'false'", "'bool'", "'return'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PA", "PC", "CA", "CC", "LA", "LC", "PYC", "COMA", "IGUAL", "MAYOR", 
			"MAYOR_IGUAL", "MENOR", "MENOR_IGUAL", "EQL", "DISTINTO", "SUM", "RES", 
			"MUL", "DIV", "MOD", "OR", "AND", "NOT", "FOR", "WHILE", "BREAK", "CONTINUE", 
			"IF", "ELSE", "INT", "CHAR", "DOUBLE", "VOID", "TRUE", "FALSE", "BOOL", 
			"RETURN", "ID", "INTEGER", "DECIMAL", "CHARACTER", "STRING_LITERAL", 
			"COMENTARIO_LINEA", "COMENTARIO_BLOQUE", "WS", "OTRO"
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


	public CompiladorLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Compilador.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\60\u012f\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\3\2\3\2\3\3\3\3\3\4\3\4"+
		"\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f"+
		"\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3"+
		"\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\27\3\30\3"+
		"\30\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3"+
		"\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3"+
		"\35\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3 \3 \3 \3 \3 \3!\3!"+
		"\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3#\3#\3#\3#\3#\3$\3$\3$\3$\3$\3$\3"+
		"%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3&\3&\3\'\3\'\5\'\u00e7\n\'\3\'\3\'\3\'\7"+
		"\'\u00ec\n\'\f\'\16\'\u00ef\13\'\3(\6(\u00f2\n(\r(\16(\u00f3\3)\3)\3)"+
		"\3)\3*\3*\3*\3*\5*\u00fe\n*\3*\3*\3+\3+\3+\3+\7+\u0106\n+\f+\16+\u0109"+
		"\13+\3+\3+\3,\3,\3,\3,\7,\u0111\n,\f,\16,\u0114\13,\3,\3,\3-\3-\3-\3-"+
		"\7-\u011c\n-\f-\16-\u011f\13-\3-\3-\3-\3-\3-\3.\3.\3.\3.\3/\3/\3\60\3"+
		"\60\3\61\3\61\3\u011d\2\62\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25"+
		"\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32"+
		"\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\2a\2"+
		"\3\2\b\5\2\f\f\17\17))\4\2$$^^\4\2\f\f\17\17\5\2\13\f\17\17\"\"\4\2C\\"+
		"c|\3\2\62;\2\u0136\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13"+
		"\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2"+
		"\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2"+
		"!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3"+
		"\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2"+
		"\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E"+
		"\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2"+
		"\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2"+
		"\3c\3\2\2\2\5e\3\2\2\2\7g\3\2\2\2\ti\3\2\2\2\13k\3\2\2\2\rm\3\2\2\2\17"+
		"o\3\2\2\2\21q\3\2\2\2\23s\3\2\2\2\25u\3\2\2\2\27w\3\2\2\2\31z\3\2\2\2"+
		"\33|\3\2\2\2\35\177\3\2\2\2\37\u0082\3\2\2\2!\u0085\3\2\2\2#\u0087\3\2"+
		"\2\2%\u0089\3\2\2\2\'\u008b\3\2\2\2)\u008d\3\2\2\2+\u008f\3\2\2\2-\u0092"+
		"\3\2\2\2/\u0095\3\2\2\2\61\u0097\3\2\2\2\63\u009b\3\2\2\2\65\u00a1\3\2"+
		"\2\2\67\u00a7\3\2\2\29\u00b0\3\2\2\2;\u00b3\3\2\2\2=\u00b8\3\2\2\2?\u00bc"+
		"\3\2\2\2A\u00c1\3\2\2\2C\u00c8\3\2\2\2E\u00cd\3\2\2\2G\u00d2\3\2\2\2I"+
		"\u00d8\3\2\2\2K\u00dd\3\2\2\2M\u00e6\3\2\2\2O\u00f1\3\2\2\2Q\u00f5\3\2"+
		"\2\2S\u00f9\3\2\2\2U\u0101\3\2\2\2W\u010c\3\2\2\2Y\u0117\3\2\2\2[\u0125"+
		"\3\2\2\2]\u0129\3\2\2\2_\u012b\3\2\2\2a\u012d\3\2\2\2cd\7*\2\2d\4\3\2"+
		"\2\2ef\7+\2\2f\6\3\2\2\2gh\7]\2\2h\b\3\2\2\2ij\7_\2\2j\n\3\2\2\2kl\7}"+
		"\2\2l\f\3\2\2\2mn\7\177\2\2n\16\3\2\2\2op\7=\2\2p\20\3\2\2\2qr\7.\2\2"+
		"r\22\3\2\2\2st\7?\2\2t\24\3\2\2\2uv\7@\2\2v\26\3\2\2\2wx\7@\2\2xy\7?\2"+
		"\2y\30\3\2\2\2z{\7>\2\2{\32\3\2\2\2|}\7>\2\2}~\7?\2\2~\34\3\2\2\2\177"+
		"\u0080\7?\2\2\u0080\u0081\7?\2\2\u0081\36\3\2\2\2\u0082\u0083\7#\2\2\u0083"+
		"\u0084\7?\2\2\u0084 \3\2\2\2\u0085\u0086\7-\2\2\u0086\"\3\2\2\2\u0087"+
		"\u0088\7/\2\2\u0088$\3\2\2\2\u0089\u008a\7,\2\2\u008a&\3\2\2\2\u008b\u008c"+
		"\7\61\2\2\u008c(\3\2\2\2\u008d\u008e\7\'\2\2\u008e*\3\2\2\2\u008f\u0090"+
		"\7~\2\2\u0090\u0091\7~\2\2\u0091,\3\2\2\2\u0092\u0093\7(\2\2\u0093\u0094"+
		"\7(\2\2\u0094.\3\2\2\2\u0095\u0096\7#\2\2\u0096\60\3\2\2\2\u0097\u0098"+
		"\7h\2\2\u0098\u0099\7q\2\2\u0099\u009a\7t\2\2\u009a\62\3\2\2\2\u009b\u009c"+
		"\7y\2\2\u009c\u009d\7j\2\2\u009d\u009e\7k\2\2\u009e\u009f\7n\2\2\u009f"+
		"\u00a0\7g\2\2\u00a0\64\3\2\2\2\u00a1\u00a2\7d\2\2\u00a2\u00a3\7t\2\2\u00a3"+
		"\u00a4\7g\2\2\u00a4\u00a5\7c\2\2\u00a5\u00a6\7m\2\2\u00a6\66\3\2\2\2\u00a7"+
		"\u00a8\7e\2\2\u00a8\u00a9\7q\2\2\u00a9\u00aa\7p\2\2\u00aa\u00ab\7v\2\2"+
		"\u00ab\u00ac\7k\2\2\u00ac\u00ad\7p\2\2\u00ad\u00ae\7w\2\2\u00ae\u00af"+
		"\7g\2\2\u00af8\3\2\2\2\u00b0\u00b1\7k\2\2\u00b1\u00b2\7h\2\2\u00b2:\3"+
		"\2\2\2\u00b3\u00b4\7g\2\2\u00b4\u00b5\7n\2\2\u00b5\u00b6\7u\2\2\u00b6"+
		"\u00b7\7g\2\2\u00b7<\3\2\2\2\u00b8\u00b9\7k\2\2\u00b9\u00ba\7p\2\2\u00ba"+
		"\u00bb\7v\2\2\u00bb>\3\2\2\2\u00bc\u00bd\7e\2\2\u00bd\u00be\7j\2\2\u00be"+
		"\u00bf\7c\2\2\u00bf\u00c0\7t\2\2\u00c0@\3\2\2\2\u00c1\u00c2\7f\2\2\u00c2"+
		"\u00c3\7q\2\2\u00c3\u00c4\7w\2\2\u00c4\u00c5\7d\2\2\u00c5\u00c6\7n\2\2"+
		"\u00c6\u00c7\7g\2\2\u00c7B\3\2\2\2\u00c8\u00c9\7x\2\2\u00c9\u00ca\7q\2"+
		"\2\u00ca\u00cb\7k\2\2\u00cb\u00cc\7f\2\2\u00ccD\3\2\2\2\u00cd\u00ce\7"+
		"v\2\2\u00ce\u00cf\7t\2\2\u00cf\u00d0\7w\2\2\u00d0\u00d1\7g\2\2\u00d1F"+
		"\3\2\2\2\u00d2\u00d3\7h\2\2\u00d3\u00d4\7c\2\2\u00d4\u00d5\7n\2\2\u00d5"+
		"\u00d6\7u\2\2\u00d6\u00d7\7g\2\2\u00d7H\3\2\2\2\u00d8\u00d9\7d\2\2\u00d9"+
		"\u00da\7q\2\2\u00da\u00db\7q\2\2\u00db\u00dc\7n\2\2\u00dcJ\3\2\2\2\u00dd"+
		"\u00de\7t\2\2\u00de\u00df\7g\2\2\u00df\u00e0\7v\2\2\u00e0\u00e1\7w\2\2"+
		"\u00e1\u00e2\7t\2\2\u00e2\u00e3\7p\2\2\u00e3L\3\2\2\2\u00e4\u00e7\5_\60"+
		"\2\u00e5\u00e7\7a\2\2\u00e6\u00e4\3\2\2\2\u00e6\u00e5\3\2\2\2\u00e7\u00ed"+
		"\3\2\2\2\u00e8\u00ec\5_\60\2\u00e9\u00ec\5a\61\2\u00ea\u00ec\7a\2\2\u00eb"+
		"\u00e8\3\2\2\2\u00eb\u00e9\3\2\2\2\u00eb\u00ea\3\2\2\2\u00ec\u00ef\3\2"+
		"\2\2\u00ed\u00eb\3\2\2\2\u00ed\u00ee\3\2\2\2\u00eeN\3\2\2\2\u00ef\u00ed"+
		"\3\2\2\2\u00f0\u00f2\5a\61\2\u00f1\u00f0\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3"+
		"\u00f1\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4P\3\2\2\2\u00f5\u00f6\5O(\2\u00f6"+
		"\u00f7\7\60\2\2\u00f7\u00f8\5O(\2\u00f8R\3\2\2\2\u00f9\u00fd\7)\2\2\u00fa"+
		"\u00fe\n\2\2\2\u00fb\u00fc\7^\2\2\u00fc\u00fe\13\2\2\2\u00fd\u00fa\3\2"+
		"\2\2\u00fd\u00fb\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0100\7)\2\2\u0100"+
		"T\3\2\2\2\u0101\u0107\7$\2\2\u0102\u0106\n\3\2\2\u0103\u0104\7^\2\2\u0104"+
		"\u0106\13\2\2\2\u0105\u0102\3\2\2\2\u0105\u0103\3\2\2\2\u0106\u0109\3"+
		"\2\2\2\u0107\u0105\3\2\2\2\u0107\u0108\3\2\2\2\u0108\u010a\3\2\2\2\u0109"+
		"\u0107\3\2\2\2\u010a\u010b\7$\2\2\u010bV\3\2\2\2\u010c\u010d\7\61\2\2"+
		"\u010d\u010e\7\61\2\2\u010e\u0112\3\2\2\2\u010f\u0111\n\4\2\2\u0110\u010f"+
		"\3\2\2\2\u0111\u0114\3\2\2\2\u0112\u0110\3\2\2\2\u0112\u0113\3\2\2\2\u0113"+
		"\u0115\3\2\2\2\u0114\u0112\3\2\2\2\u0115\u0116\b,\2\2\u0116X\3\2\2\2\u0117"+
		"\u0118\7\61\2\2\u0118\u0119\7,\2\2\u0119\u011d\3\2\2\2\u011a\u011c\13"+
		"\2\2\2\u011b\u011a\3\2\2\2\u011c\u011f\3\2\2\2\u011d\u011e\3\2\2\2\u011d"+
		"\u011b\3\2\2\2\u011e\u0120\3\2\2\2\u011f\u011d\3\2\2\2\u0120\u0121\7,"+
		"\2\2\u0121\u0122\7\61\2\2\u0122\u0123\3\2\2\2\u0123\u0124\b-\2\2\u0124"+
		"Z\3\2\2\2\u0125\u0126\t\5\2\2\u0126\u0127\3\2\2\2\u0127\u0128\b.\2\2\u0128"+
		"\\\3\2\2\2\u0129\u012a\13\2\2\2\u012a^\3\2\2\2\u012b\u012c\t\6\2\2\u012c"+
		"`\3\2\2\2\u012d\u012e\t\7\2\2\u012eb\3\2\2\2\f\2\u00e6\u00eb\u00ed\u00f3"+
		"\u00fd\u0105\u0107\u0112\u011d\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}