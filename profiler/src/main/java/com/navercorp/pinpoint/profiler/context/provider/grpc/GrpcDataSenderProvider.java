/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.bootstrap.config.GrpcTransportConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.module.SpanConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.GrpcDataSender;
import io.grpc.NameResolverProvider;
import io.grpc.internal.PinpointDnsNameResolverProvider;

import java.util.concurrent.ExecutorService;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcDataSenderProvider implements Provider<DataSender<Object>> {
    private final ProfilerConfig profilerConfig;
    private final MessageConverter<GeneratedMessageV3> messageConverter;
    private final AgentInformation agentInformation;
    private final Provider<ExecutorService> dnsExecutorService;

    @Inject
    public GrpcDataSenderProvider(ProfilerConfig profilerConfig,
                                  @SpanConverter MessageConverter<GeneratedMessageV3> messageConverter, AgentInformation agentInformation,
                                  Provider<ExecutorService> dnsExecutorService) {
        this.profilerConfig = Assert.requireNonNull(profilerConfig, "profilerConfig must not be null");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter must not be null");
        this.agentInformation = Assert.requireNonNull(agentInformation, "agentInformation must not be null");
        this.dnsExecutorService = Assert.requireNonNull(dnsExecutorService, "dnsExecutorService must not be null");
    }

    @Override
    public DataSender<Object> get() {
        GrpcTransportConfig grpcTransportConfig = profilerConfig.getGrpcTransportConfig();
        String collectorTcpServerIp = grpcTransportConfig.getCollectorSpanServerIp();
        int collectorTcpServerPort = grpcTransportConfig.getCollectorSpanServerPort();
        HeaderFactory<AgentHeaderFactory.Header> headerHeaderFactory = newAgentHeaderFactory();

        ExecutorService executorService = dnsExecutorService.get();
        NameResolverProvider nameResolverProvider = new PinpointDnsNameResolverProvider("pinpoint-dns", executorService);
        return new GrpcDataSender("Default", collectorTcpServerIp, collectorTcpServerPort,  messageConverter, headerHeaderFactory, nameResolverProvider);
    }

    private HeaderFactory<AgentHeaderFactory.Header> newAgentHeaderFactory() {
        AgentHeaderFactory.Header header = new AgentHeaderFactory.Header(agentInformation.getAgentId(), agentInformation.getApplicationName(), agentInformation.getStartTime());
        return new AgentHeaderFactory(header);
    }

}
