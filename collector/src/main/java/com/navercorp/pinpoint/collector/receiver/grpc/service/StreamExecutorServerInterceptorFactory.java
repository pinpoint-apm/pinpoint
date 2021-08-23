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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.grpc.config.GrpcStreamConfiguration;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.server.flowcontrol.IdleTimeoutFactory;
import com.navercorp.pinpoint.grpc.server.flowcontrol.RejectedExecutionListenerFactory;
import com.navercorp.pinpoint.grpc.server.flowcontrol.ScheduledExecutor;
import com.navercorp.pinpoint.grpc.server.flowcontrol.StreamExecutorServerInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StreamExecutorServerInterceptorFactory implements FactoryBean<ServerInterceptor>, BeanNameAware {
    private String beanName;
    private final Executor executor;
    private final int initRequestCount;
    private final ScheduledExecutorService scheduledExecutorService;
    private final int periodMillis;
    private final int recoveryMessagesCount;
    private final long idleTimeout;

    public StreamExecutorServerInterceptorFactory(Executor executor,
                                                  ScheduledExecutorService scheduledExecutorService,
                                                  GrpcStreamConfiguration streamConfiguration) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService, "scheduledExecutorService");

        Objects.requireNonNull(streamConfiguration, "streamConfiguration");
        this.initRequestCount = streamConfiguration.getCallInitRequestCount();

        this.periodMillis = streamConfiguration.getSchedulerPeriodMillis();
        Assert.isTrue(periodMillis > 0, "periodMillis must be positive");
        this.recoveryMessagesCount = streamConfiguration.getSchedulerRecoveryMessageCount();
        this.idleTimeout = streamConfiguration.getIdleTimeout();
    }

    @Override
    public ServerInterceptor getObject() throws Exception {
        ScheduledExecutor scheduledExecutor = new ScheduledExecutor() {
            @Override
            public Future<?> schedule(Runnable command) {
                return scheduledExecutorService.scheduleAtFixedRate(command, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
            }
        };
        IdleTimeoutFactory idleTimeoutFactory = new IdleTimeoutFactory(this.idleTimeout);
        RejectedExecutionListenerFactory listenerFactory = new RejectedExecutionListenerFactory(this.beanName, recoveryMessagesCount, idleTimeoutFactory);

        return new StreamExecutorServerInterceptor(this.beanName, this.executor, initRequestCount,
                scheduledExecutor, listenerFactory);
    }

    @Override
    public Class<ServerInterceptor> getObjectType() {
        return ServerInterceptor.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
