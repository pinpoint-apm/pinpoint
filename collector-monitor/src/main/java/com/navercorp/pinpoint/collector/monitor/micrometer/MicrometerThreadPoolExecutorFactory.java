/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.collector.monitor.micrometer;

import com.navercorp.pinpoint.collector.monitor.LoggingRejectedExecutionHandler;
import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutor;
import com.navercorp.pinpoint.collector.monitor.RejectedExecutionHandlerChain;
import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactory;
import com.navercorp.pinpoint.collector.monitor.BypassRunnableDecorator;
import com.navercorp.pinpoint.collector.monitor.RunnableDecorator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author intr3p1d
 */
public class MicrometerThreadPoolExecutorFactory implements Serializable, MonitoredThreadPoolExecutorFactory {

    private final String name;
    private final transient MeterRegistry registry;
    private final int logRate;

    public MicrometerThreadPoolExecutorFactory(String name, MeterRegistry registry, int logRate) {
        this.name = Objects.requireNonNull(name, "name");
        this.registry = registry;
        this.logRate = logRate;
    }

    @Override
    public boolean isEnable() {
        return registry != null;
    }

    @Override
    public ThreadPoolExecutor createExecutor(
            int corePoolSize, int maxPoolSize, int keepAliveSeconds, BlockingQueue<Runnable> queue,
            ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler
    ) {
        rejectedExecutionHandler = wrapHandlerChain(rejectedExecutionHandler);

        RunnableDecorator runnableDecorator = new BypassRunnableDecorator(name);

        MonitoredThreadPoolExecutor monitoredThreadPoolExecutor = new MonitoredThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.MILLISECONDS,
                queue, threadFactory, rejectedExecutionHandler, runnableDecorator);

        // Registering submitted tasks gauge
        Gauge.builder(name + ".submitted", monitoredThreadPoolExecutor, MonitoredThreadPoolExecutor::getSubmitCount)
                .description("Number of tasks submitted to the executor")
                .register(registry);

        // Registering active (running) tasks gauge
        Gauge.builder(name + ".running", monitoredThreadPoolExecutor, MonitoredThreadPoolExecutor::getActiveCount)
                .description("Number of tasks currently running in the executor")
                .register(registry);

        // Registering completed tasks gauge
        Gauge.builder(name + ".completed", monitoredThreadPoolExecutor, MonitoredThreadPoolExecutor::getCompletedTaskCount)
                .description("Number of tasks completed by the executor")
                .register(registry);

        return monitoredThreadPoolExecutor;
    }


    private RejectedExecutionHandler wrapHandlerChain(RejectedExecutionHandler rejectedExecutionHandler) {
        RejectedExecutionHandlerChain.Builder builder = new RejectedExecutionHandlerChain.Builder();
        if (isEnable()) {
            Counter rejected = Counter.builder(name + ".rejected")
                    .description("Number of tasks rejected by the executor")
                    .register(registry);
            RejectedExecutionHandler countingHandler = new CounterRejectedExecutionHandler(rejected);
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
