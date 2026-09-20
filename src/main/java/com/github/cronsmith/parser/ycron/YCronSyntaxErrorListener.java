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
