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
public class GrpcStatReceiverConfiguration {

    public GrpcStatReceiverConfiguration() {
    }

    @Bean
    @ConfigurationProperties("collector.receiver.grpc.stat.bindaddress")
    public BindAddress.Builder grpcStatBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9992);
        return builder;
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.stat.server.executor")
    public MonitoringExecutorProperties grpcStatServerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.stat.worker.executor")
    public MonitoringExecutorProperties grpcStatWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @ConfigurationProperties("collector.receiver.grpc.stat.stream")
    public GrpcStreamProperties.Builder grpcStatStreamConfigurationBuilder() {
        return GrpcStreamProperties.newBuilder();
    }

    @Bean
    @ConfigurationProperties("collector.receiver.grpc.stat")
    public GrpcPropertiesServerOptionBuilder grpcStatServerOption() {
        // Server option
        return new GrpcPropertiesServerOptionBuilder();
    }


    @Bean
    public GrpcStatReceiverProperties grpcStatReceiverProperties(
            Environment environment) {

        boolean enable = environment.getProperty("collector.receiver.grpc.stat.enable", boolean.class, false);

        ServerOption serverOption = grpcStatServerOption().build();

        BindAddress bindAddress = grpcStatBindAddressBuilder().build();

        GrpcStreamProperties streamConfiguration = grpcStatStreamConfigurationBuilder().build();
        return new GrpcStatReceiverProperties(enable, bindAddress, serverOption, streamConfiguration);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcStatWorkerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcStatWorkerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcStatServerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcStatServerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

}