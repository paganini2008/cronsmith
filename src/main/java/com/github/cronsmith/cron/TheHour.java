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
 * @Description: TheHour
 * @Author: Fred Feng
 * @Date: 02/03/2025
 * @Version 1.0.0
 */
public interface TheHour extends Hour {

    TheHour andHour(int hourOfDay);

    default TheHour toHour(int hourOfDay) {
        return toHour(hourOfDay, 1);
    }

    TheHour toHour(int hourOfDay, int interval);

}
