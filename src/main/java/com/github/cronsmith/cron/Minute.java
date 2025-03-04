/**
 * Copyright 2017-2025 Fred Feng (paganini.fy@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.cronsmith.cron;

import java.util.Iterator;

/**
 * 
 * @Description: Minute
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
public interface Minute extends Iterator<Minute>, CronExpression {

    int getYear();

    int getMonth();

    int getDay();

    int getHour();

    int getMinute();

    default Second everySecond() {
        return everySecond(1);
    }

    default Second everySecond(int interval) {
        return everySecond(0, 59, interval);
    }

    default Second everySecond(int from, int to, int interval) {
        return everySecond(m -> from, m -> to, interval);
    }

    TheSecond second(int second);

    Second everySecond(IntFunction<Minute> from, IntFunction<Minute> to, int interval);

}
