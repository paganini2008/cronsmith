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
package com.github.cronsmith.scheduler;

/**
 * 
 * @Description: DebugCronSchedulerListener
 * @Author: Fred Feng
 * @Date: 09/03/2025
 * @Version 1.0.0
 */
public class DebugCronSchedulerListener implements CronSchedulerListener {

    @Override
    public void onTaskScheduled(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskPaused(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskResumed(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskCancelled(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskRemoved(CronScheduledEvent event) {
        System.out.println(event);
    }

    @Override
    public void onTaskFailed(CronScheduledEvent event) {
        System.err.println(event);
        if (event.getReason() != null) {
            event.getReason().printStackTrace();
        }
    }

    @Override
    public void onTaskFinished(CronScheduledEvent event) {
        if (event.getReason() != null) {
            System.err.println(event);
            event.getReason().printStackTrace();
        } else {
            System.out.println(event);
        }
    }

}
