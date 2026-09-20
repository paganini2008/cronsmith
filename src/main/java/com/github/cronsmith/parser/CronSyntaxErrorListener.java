/*
 * Copyright 2026 Fred Feng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
