/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.log.collector.grpc;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.config.ExecutorProperties;
import com.navercorp.pinpoint.collector.receiver.ExecutorFactoryBean;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.SimpleServerCallExecutorSupplier;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.log.LogGrpc;
import com.navercorp.pinpoint.log.collector.grpc.context.LogAgentHeader;
import com.navercorp.pinpoint.log.collector.grpc.context.LogAgentHeaderReader;
import com.navercorp.pinpoint.log.collector.grpc.context.LogHeaderPropagationInterceptor;
import com.navercorp.pinpoint.log.collector.service.LogProviderService;
import com.navercorp.pinpoint.log.collector.service.LogServiceConfig;
import io.grpc.ServerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({ GrpcLogReceiverPropertiesConfig.class, LogServiceConfig.class })
public class LogCollectorGrpcServerConfig {

    @Bean("abortPolicy")
    ThreadPoolExecutor.AbortPolicy abortPolicy() {
        return new ThreadPoolExecutor.AbortPolicy();
    }

    @Bean("grpcLogServerExecutor")
    ExecutorFactoryBean grpcLogServerExecutor(
            ThreadPoolExecutor.AbortPolicy abortPolicy,
            @Qualifier("grpcLogReceiverConfig") GrpcLogReceiverProperties receiverConfig
    ) {
        final ExecutorProperties config = receiverConfig.getServerExecutor();
        final ExecutorFactoryBean factory = new ExecutorFactoryBean();
        factory.setRejectedExecutionHandler(abortPolicy);
        factory.setDaemon(true);
        factory.setWaitForTasksToCompleteOnShutdown(true);
        factory.setAwaitTerminationSeconds(10);
        factory.setPreStartAllCoreThreads(true);
        factory.setLogRate(100);
        factory.setExecutorProperties(config);
        factory.setThreadNamePrefix("Pinpoint-GrpcLog-Server-");
        return factory;
    }

    @Bean("grpcLogServerCallExecutor")
    ExecutorFactoryBean grpcLogServerCallExecutor(
            ThreadPoolExecutor.AbortPolicy abortPolicy,
            @Qualifier("grpcLogReceiverConfig") GrpcLogReceiverProperties receiverConfig,
            @Autowired(required = false) MetricRegistry metricRegistry
    ) {
        final ExecutorProperties config = receiverConfig.getServerCallExecutor();
        final ExecutorFactoryBean factory = new ExecutorFactoryBean();
        factory.setRejectedExecutionHandler(abortPolicy);
        factory.setDaemon(true);
        factory.setWaitForTasksToCompleteOnShutdown(true);
        factory.setAwaitTerminationSeconds(10);
        factory.setPreStartAllCoreThreads(true);
        factory.setExecutorProperties(config);
        factory.setThreadNamePrefix("Pinpoint-GrpcLog-Server-");
        factory.setLogRate(1);

        if (metricRegistry != null) {
            factory.setRegistry(metricRegistry);
        }

        return factory;
    }

    @Bean("logInterceptorList")
    List<ServerInterceptor> logInterceptorList() {
        final HeaderReader<LogAgentHeader> headerReader = new LogAgentHeaderReader();
        final ServerInterceptor interceptor = new LogHeaderPropagationInterceptor(headerReader);
        return List.of(interceptor);
    }

    @Bean("addressFilter")
    @ConditionalOnMissingBean(name = "addressFilter")
    AddressFilter allAddressFilter() {
        return AddressFilter.ALL;
    }

    @Bean
    LogGrpc.LogImplBase logService(LogProviderService service) {
        return new LogGrpcService(service);
    }

    @Bean
    GrpcReceiver grpcLogReceiver(
            @Qualifier("grpcLogReceiverConfig") GrpcLogReceiverProperties receiverConfig,
            @Qualifier("grpcLogServerExecutor") Executor serverExecutor,
            @Qualifier("grpcLogServerCallExecutor") Executor serverCallExecutor,
            @Qualifier("addressFilter") AddressFilter addressFilter,
            @Qualifier("logInterceptorList") List<ServerInterceptor> logInterceptorList,
            LogGrpc.LogImplBase logService
    ) {
        final GrpcReceiver receiver = new GrpcReceiver();
        receiver.setBindAddress(receiverConfig.getBindAddress());
        receiver.setExecutor(serverExecutor);
        receiver.setServerCallExecutorSupplier(new SimpleServerCallExecutorSupplier(serverCallExecutor));
        receiver.setAddressFilter(addressFilter);
        receiver.setBindableServiceList(List.of(logService));
        receiver.setServerInterceptorList(logInterceptorList);
        receiver.setEnable(receiverConfig.isEnable());
        receiver.setServerOption(receiverConfig.getServerOption());

        return receiver;
    }

}
