/*
 * Copyright 2018 NAVER Corp.
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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class CountingRejectedExecutionHandler implements RejectedExecutionHandler {

    private final Meter rejected;

    public CountingRejectedExecutionHandler(String executorName, MetricRegistry registry) {
        Objects.requireNonNull(executorName, "executorName");
        Objects.requireNonNull(registry, "registry");

        this.rejected = registry.meter(MetricRegistry.name(executorName, "rejected"));
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        rejected.mark();
    }
}
