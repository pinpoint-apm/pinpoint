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
import com.navercorp.pinpoint.collector.receiver.StatDispatchHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.ServerInterceptorBuilder;
import com.navercorp.pinpoint.collector.receiver.grpc.flow.RateLimitClientStreamServerInterceptor;
import com.navercorp.pinpoint.collector.receiver.grpc.keepalive.KeepAliveRegistry;
import com.navercorp.pinpoint.collector.receiver.grpc.keepalive.StreamKeepAliveInterceptor;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StatService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StreamExecutorServerInterceptorFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import io.github.bucket4j.Bandwidth;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;


/**
 * @author emeroad
 */
@Configuration
public class GrpcStatReceiverConfiguration {

    private static final Logger logger = LogManager.getLogger(GrpcStatReceiverConfiguration.class);

    public GrpcStatReceiverConfiguration() {
    }

    @Deprecated
    @Configuration
    @ConditionalOnProperty(name = "collector.receiver.grpc.stat.stream.flow-control.type", havingValue = "legacy")
    public static class LegacySpanInterceptorConfiguration {

        @Bean
        public FactoryBean<ScheduledExecutorService> grpcStatStreamScheduler(@Qualifier("grpcStatStreamProperties")
                                                                             GrpcStreamProperties properties) {
            ScheduledExecutorFactoryBean bean = new ScheduledExecutorFactoryBean();
            bean.setPoolSize(properties.getSchedulerThreadSize());
            bean.setThreadNamePrefix("Pinpoint-GrpcStat-StreamExecutor-Scheduler-");
            bean.setDaemon(true);
            bean.setWaitForTasksToCompleteOnShutdown(true);
            bean.setAwaitTerminationSeconds(10);
            return bean;
        }

        @Bean
        public FactoryBean<ServerInterceptor> statStreamExecutorInterceptor(@Qualifier("grpcStatWorkerExecutor")
                                                                            Executor executor,
                                                                            @Qualifier("grpcStatStreamScheduler")
                                                                            ScheduledExecutorService scheduledExecutorService,
                                                                            @Qualifier("grpcStatStreamProperties")
                                                                            GrpcStreamProperties properties) {
            return new StreamExecutorServerInterceptorFactory(executor, scheduledExecutorService, properties);
        }
    }



    @Configuration
    @ConditionalOnProperty(name = "collector.receiver.grpc.stat.stream.flow-control.type", havingValue = "rate-limit", matchIfMissing = true)
    public static class RateLimitServerInterceptorConfiguration {

        @Bean
        public Bandwidth statBandwidth(@Value("${collector.receiver.grpc.stat.stream.flow-control.rate-limit.capacity:1000}") long capacity,
                                            @Value("${collector.receiver.grpc.stat.stream.flow-control.rate-limit.refill-greedy:200}") long refillTokens) {
            return Bandwidth
                    .builder()
                    .capacity(capacity)
                    .refillGreedy(refillTokens, Duration.ofSeconds(1))
                    .build();
        }

        @Bean
        public ServerInterceptor statStreamExecutorInterceptor(@Qualifier("grpcStatWorkerExecutor")
                                                               Executor executor,
                                                               @Qualifier("statBandwidth")
                                                               Bandwidth bandwidth,
                                                               @Qualifier("grpcStatStreamProperties")
                                                               GrpcStreamProperties properties) {
            return new RateLimitClientStreamServerInterceptor("StatStream", executor, bandwidth, properties.getThrottledLoggerRatio());
        }
    }


    @Bean
    public ServerServiceDefinition statServerServiceDefinition(@Qualifier("grpcStatDispatchHandlerFactoryBean")
                                                               DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
                                                               @Qualifier("statStreamExecutorInterceptor")
                                                               ServerInterceptor serverInterceptor,
                                                               ServerRequestFactory serverRequestFactory) {
        BindableService spanService = new StatService(dispatchHandler, serverRequestFactory);
        return ServerInterceptors.intercept(spanService, serverInterceptor);
    }

    @Bean
    public List<ServerServiceDefinition> statServiceList(@Qualifier("statServerServiceDefinition")
                                                         ServerServiceDefinition serviceDefinition) {
        return List.of(serviceDefinition);
    }


    @Bean
    public GrpcReceiver grpcStatReceiver(@Qualifier("grpcStatReceiverProperties")
                                         GrpcReceiverProperties properties,
                                         IgnoreAddressFilter addressFilter,
                                         @Qualifier("statServiceList")
                                         List<ServerServiceDefinition> spanServiceList,
                                         @Qualifier("statInterceptorList")
                                         List<ServerInterceptor> spanInterceptorList,
                                         @Qualifier("serverTransportFilterList")
                                         List<ServerTransportFilter> serverTransportFilterList,
                                         ChannelzRegistry channelzRegistry,
                                         @Qualifier("grpcStatServerExecutor")
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
        grpcReceiver.setMonitor(monitor);
        return grpcReceiver;
    }


    @Bean
    public StatDispatchHandler<GeneratedMessageV3, GeneratedMessageV3> grpcStatDispatchHandler(
            @Qualifier("grpcAgentStatHandlerV2")
            SimpleHandler<GeneratedMessageV3> spanDataHandler,
            @Qualifier("grpcAgentEventHandler")
            SimpleHandler<GeneratedMessageV3> spanChunkHandler) {
        return new StatDispatchHandler<>(spanDataHandler, spanChunkHandler);
    }

    @Bean
    public FactoryBean<DispatchHandler<GeneratedMessageV3, GeneratedMessageV3>> grpcStatDispatchHandlerFactoryBean(
            StatDispatchHandler<GeneratedMessageV3, GeneratedMessageV3> dispatchHandler,
            AcceptedTimeService acceptedTimeService,
            HandlerManager handlerManager) {
        DispatchHandlerFactoryBean<GeneratedMessageV3, GeneratedMessageV3> bean = new DispatchHandlerFactoryBean<>();
        bean.setDispatchHandler(dispatchHandler);
        bean.setAcceptedTimeService(acceptedTimeService);
        bean.setHandlerManager(handlerManager);
        return bean;
    }

    /**
     *
     * @param keepAliveTimeout default:6hours
     */
    @Bean
    public ServerInterceptor statStreamKeepaliveInterceptor(@Value("${collector.receiver.grpc.stat.stream.keepalive.timeout:21600000}") long keepAliveTimeout) {
        if (keepAliveTimeout <= 0) {
            return null;
        }
        KeepAliveRegistry keepAliveRegistry = new KeepAliveRegistry(keepAliveTimeout, keepAliveTimeout);
        return new StreamKeepAliveInterceptor("StatStream", keepAliveRegistry);
    }

    @Bean
    public List<ServerInterceptor> statInterceptorList(@Qualifier("statStreamKeepaliveInterceptor")
                                                       Optional<ServerInterceptor> statKeepalive) {

        ServerInterceptorBuilder builder = new ServerInterceptorBuilder();
        builder.addHeaderReaderInterceptor("span");

        if (statKeepalive.isPresent()) {
            logger.info("Add StatStreamKeepaliveInterceptor");
            builder.addServerInterceptor(statKeepalive.get());
        }

        return builder.build();
    }
}