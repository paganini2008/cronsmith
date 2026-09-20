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
 * @Description: TheDayOfWeek
 * @Author: Fred Feng
 * @Date: 22/02/2025
 * @Version 1.0.0
 */
public interface TheDayOfWeek extends DayOfWeek {

    TheDayOfWeek andDay(int dayOfWeek);

    default TheDayOfWeek toDay(int dayOfWeek) {
        return toDay(dayOfWeek, 1);
    }

    default TheDayOfWeek toDay(java.time.DayOfWeek dayOfWeek) {
        return toDay(dayOfWeek, 1);
    }

    default TheDayOfWeek toDay(java.time.DayOfWeek dayOfWeek, int interval) {
        return toDay(dayOfWeek.getValue(), interval);
    }

    TheDayOfWeek toDay(int dayOfWeek, int interval);

    default TheDayOfWeek toWed() {
        return toDay(3);
    }

    default TheDayOfWeek toThur() {
        return toDay(4);
    }

    default TheDayOfWeek toFri() {
        return toDay(5);
    }

    default TheDayOfWeek toSat() {
        return toDay(6);
    }

    default TheDayOfWeek toSun() {
        return toDay(7);
    }

    default TheDayOfWeek andMon() {
        return andDay(1);
    }

    default TheDayOfWeek andTues() {
        return andDay(2);
    }

    default TheDayOfWeek andWed() {
        return andDay(3);
    }

    default TheDayOfWeek andThur() {
        return andDay(4);
    }

    default TheDayOfWeek andFri() {
        return andDay(5);
    }

    default TheDayOfWeek andSat() {
        return andDay(6);
    }

    default TheDayOfWeek andSun() {
        return andDay(7);
    }
}
