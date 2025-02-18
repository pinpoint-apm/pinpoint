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
import com.navercorp.pinpoint.collector.receiver.grpc.flow.RateLimitClientStreamServerInterceptor;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.SpanService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StreamCloseOnError;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import io.github.bucket4j.Bandwidth;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;


/**
 * @author emeroad
 */
@Configuration
public class GrpcSpanReceiverConfiguration {

    public GrpcSpanReceiverConfiguration() {
    }

    @Configuration
    @ConditionalOnProperty(name = "collector.receiver.grpc.span.stream.flow-control.type", havingValue = "rate-limit", matchIfMissing = true)
    public static class RateLimitServerInterceptorConfiguration {
        @Bean
        public Bandwidth spanBandwidth(@Value("${collector.receiver.grpc.span.stream.flow-control.rate-limit.capacity:5000}") long capacity,
                                       @Value("${collector.receiver.grpc.span.stream.flow-control.rate-limit.refill-greedy:1000}") long refillTokens) {
            return Bandwidth
                    .builder()
                    .capacity(capacity)
                    .refillGreedy(refillTokens, Duration.ofSeconds(1))
                    .build();
        }

        @Bean
        public ServerInterceptor spanStreamExecutorInterceptor(@Qualifier("grpcSpanWorkerExecutor")
                                                               Executor executor,
                                                               @Qualifier("spanBandwidth")
                                                               Bandwidth bandwidth,
                                                               @Qualifier("grpcSpanStreamProperties")
                                                               GrpcStreamProperties properties) {
            return new RateLimitClientStreamServerInterceptor("SpanStream", executor, bandwidth, properties.getThrottledLoggerRatio());
        }
    }

    @Bean
    public ServerServiceDefinition spanServerServiceDefinition(@Qualifier("grpcSpanDispatchHandlerFactoryBean")
                                                               DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                                                               @Qualifier("spanStreamExecutorInterceptor")
                                                               ServerInterceptor serverInterceptor,
                                                               ServerRequestFactory serverRequestFactory,
                                                               StreamCloseOnError streamCloseOnError) {
        BindableService spanService = new SpanService(dispatchHandler, serverRequestFactory, streamCloseOnError);
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
                                         @Qualifier("monitoredByteBufAllocator") ByteBufAllocator byteBufAllocator,
                                         IgnoreAddressFilter addressFilter,
                                         @Qualifier("spanServiceList")
                                         List<ServerServiceDefinition> spanServiceList,
                                         @Qualifier("spanInterceptor")
                                         List<ServerInterceptor> spanInterceptorList,
                                         @Qualifier("serverTransportFilterList")
                                         List<ServerTransportFilter> serverTransportFilterList,
                                         ChannelzRegistry channelzRegistry,
                                         @Qualifier("grpcSpanServerExecutor")
                                         Executor grpcSpanExecutor,
                                         Monitor monitor) {
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
        grpcReceiver.setByteBufAllocator(byteBufAllocator);
        grpcReceiver.setMonitor(monitor);
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
            HandlerManager handlerManager) {
        DispatchHandlerFactoryBean<GeneratedMessageV3, GeneratedMessageV3> bean = new DispatchHandlerFactoryBean<>();
        bean.setDispatchHandler(dispatchHandler);
        bean.setHandlerManager(handlerManager);
        return bean;
    }

    @Bean
    @Qualifier("spanInterceptor")
    public ServerInterceptor spanInterceptorList() {
        return ServerInterceptorFactory.headerReader("span");
    }

}