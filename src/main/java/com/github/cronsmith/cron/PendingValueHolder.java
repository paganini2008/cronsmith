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
 * Interval based expressions hold their current value back after being repositioned, so that the
 * very next call to {@code next()} hands that value out instead of stepping over it.
 * <p>
 * When a nested expression catches up by taking its parent's current value over directly, it has to
 * say so - otherwise the parent still believes the value is owed to somebody and swallows its next
 * step, leaving the schedule stuck on the same unit.
 *
 * @Description: PendingValueHolder
 * @Author: Fred Feng
 * @Date: 17/08/2026
 * @Version 1.0.0
 */
interface PendingValueHolder {

    /** Marks the value this expression currently sits on as already handed out. */
    void takePendingValue();

}
