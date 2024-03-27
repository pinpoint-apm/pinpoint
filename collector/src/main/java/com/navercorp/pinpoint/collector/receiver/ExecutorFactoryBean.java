/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactory;
import jakarta.annotation.Nonnull;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ExecutorFactoryBean extends ThreadPoolExecutorFactoryBean {

    private MonitoredThreadPoolExecutorFactory executorFactory;

    public ExecutorFactoryBean() {
    }

    public void setExecutorFactory(MonitoredThreadPoolExecutorFactory executorFactory) {
        this.executorFactory = executorFactory;
    }

    @Override
    @Nonnull
    protected ThreadPoolExecutor createExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds,
                                                @Nonnull BlockingQueue<Runnable> queue,
                                                @Nonnull ThreadFactory threadFactory,
                                                @Nonnull RejectedExecutionHandler rejectedExecutionHandler) {
        if (executorFactory != null && executorFactory.isEnable()) {
            return executorFactory.createExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, queue, threadFactory, rejectedExecutionHandler);
        }

        return super.createExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, queue, threadFactory, rejectedExecutionHandler);
    }

}
