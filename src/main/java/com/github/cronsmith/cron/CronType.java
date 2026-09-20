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
 * Which cron family an expression belongs to.
 *
 * <p>
 * {@link #CRON} is the traditional, month-based syntax that every dialect understands
 * (cronsmith / quartz / aws / unix): {@code <sec> <min> <hour> <day-of-month> <month> <day-of-week>
 * [<year>]}.
 *
 * <p>
 * {@link #YCRON} is the year-based extension, understood by cronsmith only. It drops the month tier
 * and schedules directly on the year through week-of-year and day-of-year:
 * {@code <sec> <min> <hour> <day-of-week> <week-of-year> <day-of-year> [<year>]}. It exists because
 * "the 100th day of the year" or "ISO week 20" has no field to live in under traditional cron.
 *
 * @Author: Fred Feng
 * @Date: 27/08/2026
 * @Version 1.0.0
 */
public enum CronType {

    /** Traditional, month-based cron. Renderable by every {@link CronDialect}. */
    CRON,

    /** Year-based cron. A cronsmith-only extension; see {@link com.github.cronsmith.YCRON}. */
    YCRON;

    /**
     * The family named by a stored flag such as a {@code @Task(parser=...)} value: {@code "ycron"}
     * (case-insensitive) is {@link #YCRON}; anything else, including {@code null}, is {@link #CRON}.
     */
    public static CronType of(String name) {
        return name != null && name.trim().equalsIgnoreCase("ycron") ? YCRON : CRON;
    }

}
