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

import java.util.List;

/**
 *
 * Implemented by the weeks that sit inside a month. A day-of-week expression asks its parent which
 * occurrence it is currently on - the second one, the last one - and resolves its own date from
 * that, which is how {@code TUE#2} and {@code FRIL} come about.
 * <p>
 * Weeks of a <em>year</em> deliberately do not implement this: they are ISO calendar weeks and have
 * no cron field to be rendered into.
 *
 * @Description: WeekOrdinal
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
interface WeekOrdinal {

    /** The occurrence this week currently stands for: 1..5 or {@link WeekOfMonthUtils#LAST}. */
    int currentOrdinal();

    /**
     * Every occurrence this week stands for, which is what rendering needs: iteration only ever
     * sees one of them at a time, but {@code MON#1,MON#3,MON#5} has to name all three.
     */
    List<Integer> ordinals();

    /** Whether every occurrence is meant, in which case no {@code #} suffix is rendered. */
    default boolean isEveryOrdinal() {
        return false;
    }

}
