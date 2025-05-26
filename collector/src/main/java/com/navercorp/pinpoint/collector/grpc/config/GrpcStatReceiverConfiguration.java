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

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.ServerInterceptorFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.flow.RateLimitClientStreamServerInterceptor;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StatService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StreamCloseOnError;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PAgentUriStat;
import com.navercorp.pinpoint.io.request.UidFetcherStreamService;
import io.github.bucket4j.Bandwidth;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.netty.buffer.ByteBufAllocator;
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
public class GrpcStatReceiverConfiguration {

    public GrpcStatReceiverConfiguration() {
    }


    @Configuration
    @ConditionalOnProperty(name = "collector.receiver.grpc.stat.stream.flow-control.type", havingValue = "rate-limit", matchIfMissing = true)
    public static class RateLimitServerInterceptorConfiguration {

        @Bean
        public Bandwidth statBandwidth(@Value("${collector.receiver.grpc.stat.stream.flow-control.rate-limit.capacity:1000}") long capacity, @Value("${collector.receiver.grpc.stat.stream.flow-control.rate-limit.refill-greedy:200}") long refillTokens) {
            return Bandwidth.builder().capacity(capacity).refillGreedy(refillTokens, Duration.ofSeconds(1)).build();
        }

        @Bean
        public ServerInterceptor statStreamExecutorInterceptor(@Qualifier("grpcStatWorkerExecutor") Executor executor, @Qualifier("statBandwidth") Bandwidth bandwidth, @Qualifier("grpcStatStreamProperties") GrpcStreamProperties properties) {
            return new RateLimitClientStreamServerInterceptor("StatStream", executor, bandwidth, properties.getThrottledLoggerRatio());
        }
    }

    @Bean
    public UidFetcherStreamService fetcherStreamService(ApplicationUidService applicationUidCacheService) {
        return new UidFetcherStreamService(applicationUidCacheService);
    }

    @Bean
    public ServerServiceDefinition statServerServiceDefinition(SimpleHandler<PAgentStatBatch> statBatchHandler,
                                                               SimpleHandler<PAgentStat> statHandler,
                                                               SimpleHandler<PAgentUriStat> uriStatHandler,
                                                               UidFetcherStreamService uidFetcherStreamService,
                                                               @Qualifier("statStreamExecutorInterceptor") ServerInterceptor serverInterceptor,
                                                               ServerRequestFactory serverRequestFactory,
                                                               StreamCloseOnError streamCloseOnError) {

        BindableService spanService = new StatService(statBatchHandler, statHandler, uriStatHandler, uidFetcherStreamService, serverRequestFactory, streamCloseOnError);
        return ServerInterceptors.intercept(spanService, serverInterceptor);
    }

    @Bean
    public ServerServiceDefinitions statServiceList(@Qualifier("statServerServiceDefinition") ServerServiceDefinition serviceDefinition) {
        return ServerServiceDefinitions.of(serviceDefinition);
    }


    @Bean
    public GrpcReceiver grpcStatReceiver(@Qualifier("grpcStatReceiverProperties") GrpcReceiverProperties properties, @Qualifier("monitoredByteBufAllocator") ByteBufAllocator byteBufAllocator, IgnoreAddressFilter addressFilter, @Qualifier("statServiceList") ServerServiceDefinitions statServices, @Qualifier("statInterceptor") List<ServerInterceptor> spanInterceptorList, @Qualifier("serverTransportFilterList") List<ServerTransportFilter> serverTransportFilterList, ChannelzRegistry channelzRegistry, @Qualifier("grpcStatServerExecutor") Executor grpcSpanExecutor, Monitor monitor) {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBindAddress(properties.getBindAddress());
        grpcReceiver.setAddressFilter(addressFilter);
        grpcReceiver.setBindableServiceList(statServices.getDefinitions());
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
    @Qualifier("statInterceptor")
    public ServerInterceptor statInterceptorList() {
        return ServerInterceptorFactory.headerReader("stat");
    }
}