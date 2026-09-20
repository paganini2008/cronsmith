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
 * @Description: TheDayOfWeekInMonth
 * @Author: Fred Feng
 * @Date: 21/02/2025
 * @Version 1.0.0
 */
public interface TheDayOfWeekInMonth extends DayOfWeek {

    default TheDayOfWeekInMonth and(int weekOfMonth, java.time.DayOfWeek dayOfWeek) {
        return and(weekOfMonth, dayOfWeek.getValue());
    }

    TheDayOfWeekInMonth and(int weekOfMonth, int dayOfWeek);

    default TheDayOfWeekInMonth andLast(java.time.DayOfWeek dayOfWeek) {
        return andLast(dayOfWeek.getValue());
    }

    TheDayOfWeekInMonth andLast(int dayOfWeek);

    default TheDayOfWeekInMonth andLastSun() {
        return andLast(7);
    }

    default TheDayOfWeekInMonth andLastSat() {
        return andLast(6);
    }

    default TheDayOfWeekInMonth andLastFri() {
        return andLast(5);
    }

    default TheDayOfWeekInMonth andLastThur() {
        return andLast(4);
    }

    default TheDayOfWeekInMonth andLastWed() {
        return andLast(3);
    }

    default TheDayOfWeekInMonth andLastTues() {
        return andLast(2);
    }

    default TheDayOfWeekInMonth andLastMon() {
        return andLast(1);
    }
}
