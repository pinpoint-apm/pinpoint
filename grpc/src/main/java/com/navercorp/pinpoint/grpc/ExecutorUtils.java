/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class ExecutorUtils {

    public static final long DEFAULT_SHUTDOWN_TIMEOUT = 3000;

    private static final Logger logger = LoggerFactory.getLogger(ExecutorUtils.class.getName());

    private ExecutorUtils() {
    }

    public static boolean shutdownExecutorService(String name, ExecutorService executorService) {
        return shutdownExecutorService(name, executorService, DEFAULT_SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static boolean shutdownExecutorService(String name, ExecutorService executorService, long timeout, TimeUnit unit) {
        if (executorService == null) {
            return false;
        }
        logger.debug("shutdown {}", name);
        executorService.shutdown();
        try {
            final boolean success = executorService.awaitTermination(timeout, unit);
            if (!success) {
                logger.warn("shutdown timeout {}", name);
            }
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
