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
package com.github.cronsmith.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @Description: ExecutorUtils
 * @Author: Fred Feng
 * @Date: 06/04/2025
 * @Version 1.0.0
 */
public abstract class ExecutorUtils {

    public static boolean isShutdown(Executor executor) {
        if (executor instanceof ExecutorService) {
            if (((ExecutorService) executor).isShutdown()) {
                return true;
            }
        }
        return false;
    }

    public static void gracefulShutdown(Executor executor, final long timeout) {
        if (!(executor instanceof ExecutorService) || isShutdown(executor)) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        try {
            es.shutdown();
        } catch (RuntimeException ex) {
            return;
        }
        if (!isShutdown(es)) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        es.shutdownNow();
                    } catch (RuntimeException ex) {
                        return;
                    }
                    try {
                        es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }).start();
        }
    }

}
