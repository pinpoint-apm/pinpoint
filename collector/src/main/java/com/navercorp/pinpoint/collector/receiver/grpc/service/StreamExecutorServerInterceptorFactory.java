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

import com.navercorp.pinpoint.grpc.server.StreamExecutorServerInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

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

    public StreamExecutorServerInterceptorFactory(Executor executor, int initRequestCount, ScheduledExecutorService scheduledExecutorService, int periodMillis, int recoveryMessagesCount) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.initRequestCount = initRequestCount;
        this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService, "scheduledExecutorService");
        this.periodMillis = periodMillis;
        this.recoveryMessagesCount = recoveryMessagesCount;
    }

    @Override
    public ServerInterceptor getObject() throws Exception {
        return new StreamExecutorServerInterceptor(this.beanName, this.executor, initRequestCount, this.scheduledExecutorService, this.periodMillis, recoveryMessagesCount);
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
