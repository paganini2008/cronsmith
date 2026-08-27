package com.github.cronsmith.parser.ycron;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Reports the first syntax error in a YCRON line instead of letting ANTLR recover by inventing
 * tokens. A dedicated copy of the traditional listener, so the two parsers share nothing.
 *
 * @Author: Fred Feng
 * @Date: 27/08/2026
 * @Version 1.0.0
 */
public class YCronSyntaxErrorListener extends BaseErrorListener {

    public static final YCronSyntaxErrorListener INSTANCE = new YCronSyntaxErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
            int charPositionInLine, String msg, RecognitionException e) {
        throw new YCronParserException(
                "Invalid ycron expression at position " + charPositionInLine + ": " + msg, e);
    }

}
