package com.github.cronsmith.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * 
 * Replaces ANTLR's default listener, which prints to the console and then lets the parser recover
 * by inventing the tokens it was missing. A cron expression that does not parse is simply invalid,
 * so the first syntax error is reported to the caller instead of being patched over.
 * 
 * @Description: CronSyntaxErrorListener
 * @Author: Fred Feng
 * @Date: 18/08/2026
 * @Version 1.0.0
 */
public class CronSyntaxErrorListener extends BaseErrorListener {

    public static final CronSyntaxErrorListener INSTANCE = new CronSyntaxErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
            int charPositionInLine, String msg, RecognitionException e) {
        throw new CronParserException(
                "Invalid cron expression at position " + charPositionInLine + ": " + msg, e);
    }

}
