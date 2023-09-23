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
public class GrpcAgentDataReceiverConfiguration {

    public GrpcAgentDataReceiverConfiguration() {
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.agent.bindaddress")
    public BindAddress.Builder grpcAgentBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9991);
        return builder;
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.agent.server.executor")
    public MonitoringExecutorProperties grpcAgentServerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.agent.server-call.executor")
    public MonitoringExecutorProperties grpcAgentServerCallExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.agent.worker.executor")
    public MonitoringExecutorProperties grpcAgentWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @ConfigurationProperties("collector.receiver.grpc.agent")
    public GrpcPropertiesServerOptionBuilder grpcAgentServerOption() {
        // Server option
        return new GrpcPropertiesServerOptionBuilder();
    }

    @Bean
    public GrpcReceiverProperties grpcAgentReceiverProperties(Environment environment) {
        boolean enable = environment.getProperty("collector.receiver.grpc.agent.enable", boolean.class, false);

        ServerOption serverOption = grpcAgentServerOption().build();
        BindAddress bindAddress = grpcAgentBindAddressBuilder().build();

        return new GrpcReceiverProperties(enable, bindAddress, serverOption);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcAgentWorkerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcAgentWorkerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }


    @Bean
    public FactoryBean<ExecutorService> grpcAgentServerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcAgentServerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcAgentServerCallExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcAgentServerCallExecutorProperties();
        properties.setLogRate(1);
        return executors.newExecutorFactoryBean(properties, beanName);
    }
}