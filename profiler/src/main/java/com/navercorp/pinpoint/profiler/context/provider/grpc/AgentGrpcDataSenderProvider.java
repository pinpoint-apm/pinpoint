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
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.ClientOption;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.module.MetadataConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.AgentGrpcDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import io.grpc.NameResolverProvider;

import java.util.concurrent.ScheduledExecutorService;


/**
 * @author jaehong.kim
 */
public class AgentGrpcDataSenderProvider implements Provider<EnhancedDataSender<Object>> {
    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;

    private final Provider<ReconnectExecutor> reconnectExecutorProvider;
    private final ScheduledExecutorService retransmissionExecutor;

    private final NameResolverProvider nameResolverProvider;
    private final ActiveTraceRepository activeTraceRepository;

    @Inject
    public AgentGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                       @MetadataConverter MessageConverter<GeneratedMessageV3> messageConverter,
                                       HeaderFactory headerFactory,
                                       Provider<ReconnectExecutor> reconnectExecutor,
                                       ScheduledExecutorService retransmissionExecutor,
                                       NameResolverProvider nameResolverProvider,
                                       ActiveTraceRepository activeTraceRepository) {
        this.grpcTransportConfig = Assert.requireNonNull(grpcTransportConfig, "grpcTransportConfig");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory");

        this.reconnectExecutorProvider = Assert.requireNonNull(reconnectExecutor, "reconnectExecutorProvider");
        this.retransmissionExecutor = Assert.requireNonNull(retransmissionExecutor, "retransmissionExecutor");


        this.nameResolverProvider = Assert.requireNonNull(nameResolverProvider, "nameResolverProvider");
        this.activeTraceRepository = Assert.requireNonNull(activeTraceRepository, "activeTraceRepository");
    }

    @Override
    public EnhancedDataSender get() {
        final String collectorIp = grpcTransportConfig.getAgentCollectorIp();
        final int collectorPort = grpcTransportConfig.getAgentCollectorPort();
        final int senderExecutorQueueSize = grpcTransportConfig.getAgentSenderExecutorQueueSize();

        final ChannelFactoryBuilder channelFactoryBuilder = newChannelFactoryBuilder();
        ChannelFactory channelFactory = channelFactoryBuilder.build();

        final  ReconnectExecutor reconnectExecutor = reconnectExecutorProvider.get();
        return new AgentGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter, reconnectExecutor, retransmissionExecutor, channelFactory, activeTraceRepository);
    }

    ChannelFactoryBuilder newChannelFactoryBuilder() {
        final int channelExecutorQueueSize = grpcTransportConfig.getAgentChannelExecutorQueueSize();
        final UnaryCallDeadlineInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(grpcTransportConfig.getAgentRequestTimeout());
        final ClientOption clientOption = grpcTransportConfig.getAgentClientOption();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("AgentGrpcDataSender");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setNameResolverProvider(nameResolverProvider);
        channelFactoryBuilder.addClientInterceptor(unaryCallDeadlineInterceptor);
        channelFactoryBuilder.setExecutorQueueSize(channelExecutorQueueSize);
        channelFactoryBuilder.setClientOption(clientOption);
        return channelFactoryBuilder;
    }
}