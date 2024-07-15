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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.DispatchHandlerFactoryBean;
import com.navercorp.pinpoint.collector.receiver.SpanDispatchHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.ServerInterceptorFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.SpanService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StreamExecutorServerInterceptorFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;


/**
 * @author emeroad
 */
@Configuration
public class GrpcSpanReceiverConfiguration {

    public GrpcSpanReceiverConfiguration() {
    }

    @Bean
    public FactoryBean<ScheduledExecutorService> grpcSpanStreamScheduler(@Qualifier("grpcSpanStreamProperties")
                                                                         GrpcStreamProperties properties) {
        ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
        bean.setPoolSize(properties.getSchedulerThreadSize());
        bean.setThreadNamePrefix("Pinpoint-GrpcSpan-StreamExecutor-Scheduler-");
        bean.setDaemon(true);
        bean.setWaitForTasksToCompleteOnShutdown(true);
        bean.setAwaitTerminationSeconds(10);
        return bean;
    }

    @Bean
    public FactoryBean<ServerInterceptor> spanStreamExecutorInterceptor(@Qualifier("grpcSpanWorkerExecutor")
                                                                        Executor executor,
                                                                        @Qualifier("grpcSpanStreamScheduler")
                                                                        ScheduledExecutorService scheduledExecutorService,
                                                                        @Qualifier("grpcSpanStreamProperties")
                                                                        GrpcStreamProperties properties) {
        return new StreamExecutorServerInterceptorFactory(executor, scheduledExecutorService, properties);
    }


    @Bean
    public ServerServiceDefinition spanServerServiceDefinition(@Qualifier("grpcSpanDispatchHandlerFactoryBean")
                                                               DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                                                               @Qualifier("spanStreamExecutorInterceptor")
                                                               ServerInterceptor serverInterceptor,
                                                               ServerRequestFactory serverRequestFactory) {
        BindableService spanService = new SpanService(dispatchHandler, serverRequestFactory);
        if (serverInterceptor == null) {
            return spanService.bindService();
        }
        return ServerInterceptors.intercept(spanService, serverInterceptor);
    }

    @Bean
    public List<ServerServiceDefinition> spanServiceList(@Qualifier("spanServerServiceDefinition")
                                                         ServerServiceDefinition serviceDefinition) {
        return List.of(serviceDefinition);
    }

    @Bean
    public GrpcReceiver grpcSpanReceiver(@Qualifier("grpcSpanReceiverProperties")
                                         GrpcReceiverProperties properties,
                                         IgnoreAddressFilter addressFilter,
                                         @Qualifier("spanServiceList")
                                         List<ServerServiceDefinition> spanServiceList,
                                         @Qualifier("spanInterceptorList")
                                         List<ServerInterceptor> spanInterceptorList,
                                         @Qualifier("serverTransportFilterList")
                                         List<ServerTransportFilter> serverTransportFilterList,
                                         ChannelzRegistry channelzRegistry,
                                         @Qualifier("grpcSpanServerExecutor")
                                         Executor grpcSpanExecutor) {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBindAddress(properties.getBindAddress());
        grpcReceiver.setAddressFilter(addressFilter);
        grpcReceiver.setBindableServiceList(spanServiceList);
        grpcReceiver.setServerInterceptorList(spanInterceptorList);
        grpcReceiver.setTransportFilterList(serverTransportFilterList);
        grpcReceiver.setChannelzRegistry(channelzRegistry);
        grpcReceiver.setExecutor(grpcSpanExecutor);
        grpcReceiver.setEnable(properties.isEnable());
        grpcReceiver.setServerOption(properties.getServerOption());
        return grpcReceiver;
    }


    @Bean
    public SpanDispatchHandler<GeneratedMessageV3, GeneratedMessageV3> grpcSpanDispatchHandler(
            @Qualifier("grpcSpanHandler")
            SimpleHandler<GeneratedMessageV3> spanDataHandler,
            @Qualifier("grpcSpanChunkHandler")
            SimpleHandler<GeneratedMessageV3> spanChunkHandler) {
        return new SpanDispatchHandler<>(spanDataHandler, spanChunkHandler);
    }

    @Bean
    public FactoryBean<DispatchHandler<GeneratedMessageV3, GeneratedMessageV3>> grpcSpanDispatchHandlerFactoryBean(
            SpanDispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
            AcceptedTimeService acceptedTimeService,
            HandlerManager handlerManager) {
        DispatchHandlerFactoryBean<GeneratedMessageV3, GeneratedMessageV3> bean = new DispatchHandlerFactoryBean<>();
        bean.setDispatchHandler(dispatchHandler);
        bean.setAcceptedTimeService(acceptedTimeService);
        bean.setHandlerManager(handlerManager);
        return bean;
    }

    @Bean
    public List<ServerInterceptor> spanInterceptorList() {
        return List.of(ServerInterceptorFactory.headerReader("span"));
    }

}