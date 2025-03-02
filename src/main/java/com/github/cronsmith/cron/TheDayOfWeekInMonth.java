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

import java.time.DayOfWeek;

/**
 * 
 * @Description: TheDayOfWeekInMonth
 * @Author: Fred Feng
 * @Date: 01/03/2025
 * @Version 1.0.0
 */
public interface TheDayOfWeekInMonth extends Day {

    TheDayOfWeekInMonth and(int weekOfMonth, int dayOfWeek);

    TheDayOfWeekInMonth andLast(int dayOfWeek);

    default TheDayOfWeekInMonth andLastSun() {
        return andLast(DayOfWeek.SUNDAY.getValue());
    }

    default TheDayOfWeekInMonth andLastSat() {
        return andLast(DayOfWeek.SATURDAY.getValue());
    }

    default TheDayOfWeekInMonth andLastFri() {
        return andLast(DayOfWeek.FRIDAY.getValue());
    }

    default TheDayOfWeekInMonth andLastThur() {
        return andLast(DayOfWeek.THURSDAY.getValue());
    }

    default TheDayOfWeekInMonth andLastWed() {
        return andLast(DayOfWeek.WEDNESDAY.getValue());
    }

    default TheDayOfWeekInMonth andLastTues() {
        return andLast(DayOfWeek.TUESDAY.getValue());
    }

    default TheDayOfWeekInMonth andLastMon() {
        return andLast(DayOfWeek.MONDAY.getValue());
    }
}
