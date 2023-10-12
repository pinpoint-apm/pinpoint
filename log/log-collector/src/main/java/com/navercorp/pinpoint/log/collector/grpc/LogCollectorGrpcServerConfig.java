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

import com.navercorp.pinpoint.collector.monitor.MonitoringExecutors;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.SimpleServerCallExecutorSupplier;
import com.navercorp.pinpoint.common.server.executor.ThreadPoolExecutorCustomizer;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.server.util.CallerUtils;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.log.LogGrpc;
import com.navercorp.pinpoint.log.collector.grpc.context.LogAgentHeader;
import com.navercorp.pinpoint.log.collector.grpc.context.LogAgentHeaderReader;
import com.navercorp.pinpoint.log.collector.grpc.context.LogHeaderPropagationInterceptor;
import com.navercorp.pinpoint.log.collector.service.LogProviderService;
import com.navercorp.pinpoint.log.collector.service.LogServiceConfig;
import io.grpc.ServerInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author youngjin.kim2
 */
@Configuration
@Import({GrpcLogReceiverPropertiesConfig.class, LogServiceConfig.class})
public class LogCollectorGrpcServerConfig {

    @Bean
    public MonitoringExecutors monitoringExecutors() {
        return new MonitoringExecutors(new ThreadPoolExecutorCustomizer(), null);
    }

    @Bean("grpcLogServerExecutor")
    public FactoryBean<ExecutorService> grpcLogServerExecutor(MonitoringExecutors executors,
                                                              @Qualifier("grpcLogServerExecutorProperties")
                                                              MonitoringExecutorProperties properties) {
        String beanName = CallerUtils.getCallerMethodName();
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean("grpcLogServerCallExecutor")
    public FactoryBean<ExecutorService> grpcLogServerCallExecutor(MonitoringExecutors executors,
                                                                  @Qualifier("grpcLogServerCallExecutorProperties")
                                                                  MonitoringExecutorProperties properties) {
        String beanName = CallerUtils.getCallerMethodName();
        properties.setLogRate(1);
        return executors.newExecutorFactoryBean(properties, beanName);
    }

    @Bean("logInterceptorList")
    public List<ServerInterceptor> logInterceptorList() {
        final HeaderReader<LogAgentHeader> headerReader = new LogAgentHeaderReader();
        final ServerInterceptor interceptor = new LogHeaderPropagationInterceptor(headerReader);
        return List.of(interceptor);
    }

    @Bean("addressFilter")
    @ConditionalOnMissingBean(name = "addressFilter")
    public AddressFilter allAddressFilter() {
        return AddressFilter.ALL;
    }

    @Bean
    public LogGrpc.LogImplBase logService(LogProviderService service) {
        return new LogGrpcService(service);
    }

    @Bean
    public GrpcReceiver grpcLogReceiver(
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
