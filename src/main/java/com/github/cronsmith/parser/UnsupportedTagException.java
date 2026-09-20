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

/**
 * 
 * Raising an UnsupportedTagException represents an unsupported tag while resolving a cron
 * expression string
 * 
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public class UnsupportedTagException extends CronParserException {

    private static final long serialVersionUID = -7517956571829299373L;

    public UnsupportedTagException(String msg) {
        super(msg);
    }
}
