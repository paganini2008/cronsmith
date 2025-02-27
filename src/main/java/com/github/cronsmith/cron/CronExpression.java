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

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.function.Consumer;
import com.github.cronsmith.CollectionUtils;
import com.github.cronsmith.SerializationUtils;

/**
 * 
 * @Description: CronExpression
 * @Author: Fred Feng
 * @Date: 26/02/2025
 * @Version 1.0.0
 */
@SuppressWarnings("unchecked")
public interface CronExpression extends CronStringBuilder {

    LocalDateTime getTime();

    /**
     * copy this
     * 
     * @return
     */
    default CronExpression copy() {
        return SerializationUtils.copy(this);
    }

    default void forEach(final Consumer<LocalDateTime> consumer, final int n) {
        forEach(consumer, LocalDateTime.now(), n);
    }

    default void forEach(final Consumer<LocalDateTime> consumer, final LocalDateTime baseline,
            final int n) {
        if (!(this instanceof Iterator)) {
            throw new UnsupportedOperationException();
        }
        int i = 0;
        LocalDateTime dateTime;
        for (CronExpression cronExpression : CollectionUtils
                .forEach((Iterator<CronExpression>) copy())) {
            dateTime = cronExpression.getTime();
            if (dateTime.compareTo(baseline) < 0) {
                continue;
            }
            if (n < 0 || i++ < n) {
                consumer.accept(dateTime);
            } else {
                break;
            }
        }
    }

}
