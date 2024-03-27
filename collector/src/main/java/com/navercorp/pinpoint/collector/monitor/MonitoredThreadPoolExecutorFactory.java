/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.monitor;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MonitoredThreadPoolExecutorFactory implements Serializable {

    private final String name;
    private final transient MetricRegistry registry;
    private final int logRate;

    public MonitoredThreadPoolExecutorFactory(String name, MetricRegistry registry, int logRate) {
        this.name = Objects.requireNonNull(name, "name");
        this.registry = registry;
        this.logRate = logRate;
    }

    public boolean isEnable() {
        return registry != null;
    }

    public ThreadPoolExecutor createExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, BlockingQueue<Runnable> queue,
                                             ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        rejectedExecutionHandler = wrapHandlerChain(rejectedExecutionHandler);

        RunnableDecorator runnableDecorator = new BypassRunnableDecorator(name);

        MonitoredThreadPoolExecutor monitoredThreadPoolExecutor = new MonitoredThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.MILLISECONDS,
                queue, threadFactory, rejectedExecutionHandler, runnableDecorator);

        Gauge<Long> submitGauge = () -> (long) monitoredThreadPoolExecutor.getSubmitCount();
        this.registry.register(MetricRegistry.name(name, "submitted"), submitGauge);

        Gauge<Long> runningGauge = () -> (long) monitoredThreadPoolExecutor.getActiveCount();
        this.registry.register(MetricRegistry.name(name, "running"), runningGauge);

        Gauge<Long> completedTaskGauge = () -> (long) monitoredThreadPoolExecutor.getCompletedTaskCount();
        this.registry.register(MetricRegistry.name(name, "completed"), completedTaskGauge);

        return monitoredThreadPoolExecutor;
    }

    private RejectedExecutionHandler wrapHandlerChain(RejectedExecutionHandler rejectedExecutionHandler) {

        RejectedExecutionHandlerChain.Builder builder = new RejectedExecutionHandlerChain.Builder();
        if (registry != null) {
            Meter rejected = registry.meter(MetricRegistry.name(name, "rejected"));
            RejectedExecutionHandler countingHandler = new CountingRejectedExecutionHandler(rejected);
            builder.addRejectHandler(countingHandler);
        }

        if (logRate > -1) {
            RejectedExecutionHandler loggingHandler = new LoggingRejectedExecutionHandler(name, logRate);
            builder.addRejectHandler(loggingHandler);
        }

        // original exception policy
        builder.addRejectHandler(rejectedExecutionHandler);

        return builder.build();
    }
}
