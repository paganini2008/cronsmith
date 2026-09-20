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

import com.github.cronsmith.parser.CronParserException;

/**
 * Thrown when a YCRON (year-based) expression cannot be parsed. Kept separate from the traditional
 * {@code CronParserException} so the two parsing paths stay fully independent.
 *
 * @Author: Fred Feng
 * @Date: 27/08/2026
 * @Version 1.0.0
 */
public class YCronParserException extends CronParserException {

    private static final long serialVersionUID = 7710035179031510632L;

    public YCronParserException(String msg) {
        super(msg);
    }

    public YCronParserException(String msg, Throwable e) {
        super(msg, e);
    }
}
