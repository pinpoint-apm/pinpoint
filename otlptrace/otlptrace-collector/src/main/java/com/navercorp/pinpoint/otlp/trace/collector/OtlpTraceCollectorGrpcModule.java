/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector;

import com.navercorp.pinpoint.collector.grpc.channelz.ChannelzConfiguration;
import com.navercorp.pinpoint.collector.grpc.config.GrpcPropertiesServerOptionBuilder;
import com.navercorp.pinpoint.collector.grpc.config.GrpcReceiverProperties;
import com.navercorp.pinpoint.collector.grpc.config.GrpcStreamProperties;
import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactoryProvider;
import com.navercorp.pinpoint.collector.monitor.MonitoringExecutors;
import com.navercorp.pinpoint.collector.monitor.config.MicrometerConfiguration;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.common.server.executor.ExecutorCustomizer;
import com.navercorp.pinpoint.common.server.executor.ThreadPoolExecutorCustomizer;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;

@Configuration
@EnableScheduling
@Import({
        MicrometerConfiguration.class,
        ChannelzConfiguration.class
})
public class OtlpTraceCollectorGrpcModule {

    public OtlpTraceCollectorGrpcModule() {
    }

    @Bean
    public MonitoringExecutors otlpMonitoringExecutors(
            MonitoredThreadPoolExecutorFactoryProvider monitoredThreadPoolExecutorFactoryProvider) {
        ExecutorCustomizer<ThreadPoolExecutorFactoryBean> customizer = new ThreadPoolExecutorCustomizer();
        return new MonitoringExecutors(customizer, monitoredThreadPoolExecutorFactoryProvider);
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.otlp.trace.bindaddress")
    public BindAddress.Builder grpcOtlpTraceBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9998);
        return builder;
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.otlp.trace.server.executor")
    public MonitoringExecutorProperties grpcOtlpTraceServerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.otlp.trace.server-call.executor")
    public MonitoringExecutorProperties grpcOtlpTraceServerCallExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.otlp.trace.worker.executor")
    public MonitoringExecutorProperties grpcOtlpTraceWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @Validated
    @ConfigurationProperties("collector.receiver.grpc.otlp.trace.stream")
    public GrpcStreamProperties grpcOtlpTraceStreamProperties() {
        return new GrpcStreamProperties();
    }

    @Bean
    @ConfigurationProperties("collector.receiver.grpc.otlp.trace")
    public GrpcPropertiesServerOptionBuilder grpcOtlpTraceServerOption() {
        // Server option
        return new GrpcPropertiesServerOptionBuilder();
    }

    @Bean
    public GrpcReceiverProperties grpcOtlpTraceReceiverProperties(Environment environment) {
        boolean enable = environment.getProperty("collector.receiver.grpc.otlp.trace.enable", boolean.class, true);
        ServerOption serverOption = grpcOtlpTraceServerOption().build();
        BindAddress bindAddress = grpcOtlpTraceBindAddressBuilder().build();
        return new GrpcReceiverProperties(enable, bindAddress, serverOption);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcOtlpTraceWorkerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcOtlpTraceWorkerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean
    public FactoryBean<ExecutorService> grpcOtlpTraceServerExecutor(MonitoringExecutors executors) {
        String beanName = CallerUtils.getCallerMethodName();
        MonitoringExecutorProperties properties = grpcOtlpTraceServerExecutorProperties();
        return executors.newExecutorFactoryBean(properties, beanName);
    }
}
