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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * 
 * @Description: Era
 * @Author: Fred Feng
 * @Date: 02/03/2025
 * @Version 1.0.0
 */
public final class Era implements CronExpression, Serializable {

    private static final long serialVersionUID = -400537561864611508L;

    public Era(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    private final TimeZone timeZone;

    @Override
    public LocalDateTime getTime() {
        return LocalDate.now(timeZone.toZoneId()).atStartOfDay();
    }

    public Year everyYear() {
        return everyYear(1);
    }

    public Year everyYear(int interval) {
        return everyYear(getTime().getYear(), Year.MAX_YEAR, interval);
    }

    public Year everyYear(int fromYear, int toYear, int interval) {
        return everyYear(e -> fromYear, e -> toYear, interval);
    }

    public Year everyYear(IntFunction<Era> from, IntFunction<Era> to, int interval) {
        return new EveryYear(this, from, to, interval);
    }

    public TheYear year() {
        return year(getTime().getYear());
    }

    public TheYear year(int year) {
        return new ThisYear(this, year);
    }

    @Override
    public CronExpression getParent() {
        return null;
    }

    public static Era getInstance() {
        return getInstance(TimeZone.getDefault());
    }

    public static Era getInstance(TimeZone timeZone) {
        return new Era(timeZone);
    }

}
