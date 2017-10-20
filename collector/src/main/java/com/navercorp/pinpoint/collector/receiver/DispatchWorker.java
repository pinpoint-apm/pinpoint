/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.monitor.MonitoredExecutorService;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ExecutorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Taejin Koo
 */
public class DispatchWorker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DispatchWorkerOption option;
    private final AtomicInteger rejectedCount = new AtomicInteger(0);

    private MetricRegistry metricRegistry;

    private ExecutorService worker;

    public DispatchWorker(DispatchWorkerOption option) {
        Assert.requireNonNull(option, "option may not be null");

        this.option = option;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void start() {
        logger.info("{} start.", getName());
        ExecutorService worker = createWorker(getName(), getThreadSize(), getQueueSize());
        if (option.isEnableCollectMetric()) {
            if (metricRegistry == null) {
                logger.warn("metricRegistry not autowired. Can't enable monitoring.");
                this.worker = worker;
            } else {
                this.worker = new MonitoredExecutorService(worker, metricRegistry, getName());
            }
        } else {
            this.worker = worker;
        }
    }

    private ExecutorService createWorker(String workerName, int threadSize, int queueSize) {
        return ExecutorFactory.newFixedThreadPool(threadSize, queueSize, workerName, true);
    }

    public void shutdown() {
        logger.info("{] shutdown.", getName());
        worker.shutdown();
        try {
            worker.awaitTermination(1000 * 10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.info("{}.shutdown() Interrupted", getName(), e);
            Thread.currentThread().interrupt();
        }
    }

    public void execute(Runnable runnable) {
        execute(runnable, false);
    }

    public void execute(Runnable runnable, boolean throwWhenExceptionOccurs) {
        try {
            worker.execute(runnable);
        } catch (RejectedExecutionException exception) {
            handleRejectedExecutionException(exception, throwWhenExceptionOccurs);
        }
    }

    private void handleRejectedExecutionException(RejectedExecutionException exception, boolean throwWhenExceptionOccurs) {
        final int error = rejectedCount.incrementAndGet();
        if (throwWhenExceptionOccurs) {
            throw exception;
        } else {
            if ((error % option.getRecordLogRate()) == 0) {
                logger.warn("RejectedExecutionCount={}", error);
            }
        }
    }

    public String getName() {
        return option.getName();
    }

    public int getThreadSize() {
        return option.getThreadSize();
    }

    public int getQueueSize() {
        return option.getQueueSize();
    }

}
