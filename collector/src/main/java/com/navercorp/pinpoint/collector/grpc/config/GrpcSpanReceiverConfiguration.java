/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.collector.monitor.MonitoringExecutors;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;


/**
 * @author emeroad
 */
@Configuration
public class GrpcSpanReceiverConfiguration {

    public GrpcSpanReceiverConfiguration() {
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.span.bindaddress")
    public BindAddress.Builder grpcSpanBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9993);
        return builder;
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.span.server.executor")
    public MonitoringExecutorProperties grpcSpanServerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.span.server-call.executor")
    public MonitoringExecutorProperties grpcSpanServerCallExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.span.worker.executor")
    public MonitoringExecutorProperties grpcSpanWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.span.stream")
    public GrpcStreamProperties grpcSpanStreamProperties() {
        return new GrpcStreamProperties();
    }

    @Bean
    @ConfigurationProperties("collector.receiver.grpc.span")
    public GrpcPropertiesServerOptionBuilder grpcSpanServerOption() {
        // Server option
        return new GrpcPropertiesServerOptionBuilder();
    }

    @Bean
    public GrpcReceiverProperties grpcSpanReceiverProperties(Environment environment) {

        boolean enable = environment.getProperty("collector.receiver.grpc.span.enable", boolean.class, false);

        ServerOption serverOption = grpcSpanServerOption().build();

        BindAddress bindAddress = grpcSpanBindAddressBuilder().build();

        return new GrpcReceiverProperties(enable, bindAddress, serverOption);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcSpanWorkerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcSpanWorkerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcSpanServerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcSpanServerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

}