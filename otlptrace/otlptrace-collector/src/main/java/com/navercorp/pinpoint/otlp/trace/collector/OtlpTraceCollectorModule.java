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

import com.navercorp.pinpoint.collector.applicationmap.config.ApplicationMapModule;
import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.grpc.config.GrpcReceiverProperties;
import com.navercorp.pinpoint.collector.grpc.config.ServerServiceDefinitions;
import com.navercorp.pinpoint.collector.heatmap.HeatmapCollectorModule;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.BasicMonitor;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.common.server.uid.ObjectNameVersion;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.otlp.trace.collector.service.GrpcOtlpTraceService;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.grpc.MetricCollectingServerInterceptor;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.Executor;

@Configuration
@Import({
        ApplicationMapModule.class,
        TypeLoaderConfiguration.class,

        HeatmapCollectorModule.class,

        OtlpTraceCollectorPropertySources.class,
        OtlpTraceCollectorHbaseModule.class,
        OtlpTraceCollectorGrpcModule.class,
        OtlpTraceCollectorGrpcSslModule.class,
        OtlpTraceCollectorHttpModule.class
})
@ComponentScan({
        "com.navercorp.pinpoint.otlp.trace.collector.mapper",
        "com.navercorp.pinpoint.otlp.trace.collector.service",
        "com.navercorp.pinpoint.otlp.trace.collector.controller"
})
@ConditionalOnProperty(name = "pinpoint.modules.collector.otlptrace.enabled", havingValue = "true")
public class OtlpTraceCollectorModule {

    @Bean
    public ObjectNameVersion serverNameVersion(@Value(ObjectNameVersion.VALUE_KEY) String version) {
        return ObjectNameVersion.getVersion(version);
    }

    // Shared, thread-safe, bounded dedup of already-persisted agentIds. Injected into the single
    // OtlpTraceExportService so gRPC and HTTP transports share one cache (dedup is effective across
    // both) instead of the former per-instance, non-thread-safe LRUCache.
    @Bean
    public Cache<String, Boolean> otlpAgentIdCache(
            @Value("${pinpoint.collector.otlptrace.agent-id-cache.max-size:10000}") int maxSize) {
        return Caffeine.newBuilder().maximumSize(maxSize).build();
    }

    @Bean
    public ServerServiceDefinition serverServiceDefinition(OtlpTraceExportService exportService,
                                                           @Qualifier("grpcOtlpTraceWorkerExecutor") Executor workerExecutor,
                                                           @Value("${pinpoint.collector.otlptrace.admission.max-in-flight-bytes:268435456}") int maxInFlightBytes,
                                                           MeterRegistry meterRegistry) {
        BindableService spanService = new GrpcOtlpTraceService(exportService, workerExecutor, maxInFlightBytes);
        // gRPC server metrics (request count / latency / status code) for the OTLP trace endpoint,
        // tagged service=otlptrace to match the agent/stat/span receivers' metrics.
        final ServerInterceptor metricInterceptor = new MetricCollectingServerInterceptor(meterRegistry,
                (Counter.Builder builder) -> builder.tag("service", "otlptrace"),
                (Timer.Builder builder) -> builder.tag("service", "otlptrace"));
        return ServerInterceptors.intercept(spanService, metricInterceptor);
    }

    @Bean
    public ServerServiceDefinitions serviceList(@Qualifier("serverServiceDefinition") ServerServiceDefinition serviceDefinition) {
        return ServerServiceDefinitions.of(serviceDefinition);
    }

    @Bean
    public CollectorProperties collectorProperties() {
        return new CollectorProperties();
    }

    @Bean
    public IgnoreAddressFilter ignoreAddressFilter(CollectorProperties properties) {
        return new IgnoreAddressFilter(properties.getL4IpList());
    }

    @Bean
    public Monitor grpcReceiverMonitor(@Value("${collector.receiver.grpc.monitor.enable:true}") boolean enable) {
        if (enable) {
            return new BasicMonitor("GrpcReceiverMonitor");
        } else {
            return Monitor.NONE;
        }
    }

    @Bean
    public GrpcReceiver grpcSpanReceiver(@Qualifier("grpcOtlpTraceReceiverProperties")
                                         GrpcReceiverProperties properties,
                                         @Qualifier("monitoredByteBufAllocator") ByteBufAllocator byteBufAllocator,
                                         IgnoreAddressFilter addressFilter,
                                         @Qualifier("serviceList")
                                         ServerServiceDefinitions spanServices,
                                         ChannelzRegistry channelzRegistry,
                                         @Qualifier("grpcOtlpTraceServerExecutor")
                                         Executor grpcSpanExecutor,
                                         Monitor monitor) {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBindAddress(properties.getBindAddress());
        grpcReceiver.setAddressFilter(addressFilter);
        grpcReceiver.setBindableServiceList(spanServices.getDefinitions());
        grpcReceiver.setChannelzRegistry(channelzRegistry);
        grpcReceiver.setExecutor(grpcSpanExecutor);
        grpcReceiver.setEnable(properties.isEnable());
        grpcReceiver.setServerOption(properties.getServerOption());
        grpcReceiver.setByteBufAllocator(byteBufAllocator);
        grpcReceiver.setMonitor(monitor);
        return grpcReceiver;
    }
}
