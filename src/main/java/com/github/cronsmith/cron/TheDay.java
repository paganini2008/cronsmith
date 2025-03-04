/**
 * Copyright 2017-2022 Fred Feng (paganini.fy@gmail.com)
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

/**
 * 
 * @Description: TheDay
 * @Author: Fred Feng
 * @Date: 02/03/2025
 * @Version 1.0.0
 */
public interface TheDay extends Day {

    TheDay andDay(int dayOfMonth);

    default TheDay andLastDay() {
        return andLastDay(0);
    }

    TheDay andLastDay(int n);

    TheDay andLastWeekday();

    default TheDay toLastDay() {
        return toLastDay(1);
    }

    TheDay toLastDay(int interval);

    default TheDay toDay(int dayOfMonth) {
        return toDay(dayOfMonth, 1);
    }

    TheDay toDay(int dayOfMonth, int interval);

}
