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

package com.github.cronsmith.cron;

/**
 * 
 * @Description: TheYear
 * @Author: Fred Feng
 * @Date: 02/03/2024
 * @Version 1.0.0
 */
public interface TheYear extends Year {

    TheYear andYear(int year);

    default TheYear toYear(int year) {
        return toYear(year, 1);
    }

    TheYear toYear(int year, int interval);

    default TheYear toEnd(int interval) {
        return toYear(MAX_YEAR, interval);
    }

}
