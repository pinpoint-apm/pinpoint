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
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.BasicMonitor;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.config.TypeLoaderConfiguration;
import com.navercorp.pinpoint.common.server.uid.ObjectNameVersion;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapper;
import com.navercorp.pinpoint.otlp.trace.collector.service.GrpcOtlpTraceService;
import com.navercorp.pinpoint.otlp.trace.collector.service.HbaseOtlpAgentInfoService;
import com.navercorp.pinpoint.otlp.trace.collector.service.HbaseOtlpApplicationIndexV2Service;
import io.grpc.BindableService;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
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

        OtlpTraceCollectorPropertySources.class,
        OtlpTraceCollectorHbaseModule.class,
        OtlpTraceCollectorGrpcModule.class
})
@ComponentScan({
        "com.navercorp.pinpoint.otlp.trace.collector.mapper",
        "com.navercorp.pinpoint.otlp.trace.collector.service"
})
@ConditionalOnProperty(name = "pinpoint.modules.collector.otlptrace.enabled", havingValue = "true")
public class OtlpTraceCollectorModule {

    @Bean
    public ObjectNameVersion serverNameVersion(@Value(ObjectNameVersion.VALUE_KEY) String version) {
        return ObjectNameVersion.getVersion(version);
    }

    @Bean
    public ServerServiceDefinition serverServiceDefinition(@Qualifier("hbaseOtlpTraceService") TraceService traceService, @Qualifier("hbaseOtlpAgentInfoService") HbaseOtlpAgentInfoService agentInfoService, @Qualifier("hbaseOtlpApplicationIndexV2Service") HbaseOtlpApplicationIndexV2Service applicationIndexV2Service, OtlpTraceMapper mapper) {
        BindableService spanService = new GrpcOtlpTraceService(traceService, agentInfoService, applicationIndexV2Service, mapper);
        return ServerInterceptors.intercept(spanService);
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
