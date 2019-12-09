/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LoggingRejectedExecutionHandler implements RejectedExecutionHandler {
    private final AtomicLong rejectedCount = new AtomicLong(0);
    private final Logger logger;
    private final int logRate;

    public LoggingRejectedExecutionHandler(String executorName, int logRate) {
        Objects.requireNonNull(executorName, "executorName");

        this.logger = LoggerFactory.getLogger(executorName);
        this.logRate = logRate;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        final long error = rejectedCount.incrementAndGet();
        if ((error % logRate) == 0) {
            final int maxPoolSize = executor != null ? executor.getMaximumPoolSize() : -1;
            logger.warn("The executor uses finite bounds for both maximum threads and work queue capacity, and is saturated. Check the maxPoolSize, queueCapacity, and HBase options in the configuration. maxPoolSize={}, rejectedCount={}", maxPoolSize, error);
        }
    }
}
