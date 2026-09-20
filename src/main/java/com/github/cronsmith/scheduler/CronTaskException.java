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
package com.github.cronsmith.scheduler;

/**
 * 
 * @Description: CronTaskException
 * @Author: Fred Feng
 * @Date: 08/04/2025
 * @Version 1.0.0
 */
public class CronTaskException extends RuntimeException {

    private static final long serialVersionUID = 3594026796604900019L;

    public CronTaskException(String msg) {
        super(msg);
    }

    public CronTaskException(String msg, Throwable e) {
        super(msg, e);
    }

}
