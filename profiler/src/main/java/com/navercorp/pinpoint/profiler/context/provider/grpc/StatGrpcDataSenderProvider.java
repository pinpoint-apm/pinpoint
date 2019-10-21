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
import com.navercorp.pinpoint.profiler.context.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.module.StatConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import com.navercorp.pinpoint.profiler.sender.grpc.StatGrpcDataSender;
import io.grpc.NameResolverProvider;

/**
 * @author jaehong.kim
 */
public class StatGrpcDataSenderProvider implements Provider<DataSender<Object>> {
    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;
    private final Provider<ReconnectExecutor> reconnectExecutorProvider;
    private final NameResolverProvider nameResolverProvider;

    @Inject
    public StatGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                      @StatConverter MessageConverter<GeneratedMessageV3> messageConverter,
                                      HeaderFactory headerFactory,
                                      Provider<ReconnectExecutor> reconnectExecutor,
                                      NameResolverProvider nameResolverProvider) {
        this.grpcTransportConfig = Assert.requireNonNull(grpcTransportConfig, "profilerConfig");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");
        this.headerFactory = Assert.requireNonNull(headerFactory, "agentHeaderFactory");
        this.reconnectExecutorProvider = Assert.requireNonNull(reconnectExecutor, "reconnectExecutorProvider");
        this.nameResolverProvider = Assert.requireNonNull(nameResolverProvider, "nameResolverProvider");
    }

    @Override
    public DataSender<Object> get() {
        final String collectorIp = grpcTransportConfig.getStatCollectorIp();
        final int collectorPort = grpcTransportConfig.getStatCollectorPort();
        final int senderExecutorQueueSize = grpcTransportConfig.getStatSenderExecutorQueueSize();
        final ChannelFactoryBuilder channelFactoryBuilder = newChannelFactoryBuilder();
        final ChannelFactory channelFactory = channelFactoryBuilder.build();

        // not singleton
        ReconnectExecutor reconnectExecutor = reconnectExecutorProvider.get();
        return new StatGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter, reconnectExecutor, channelFactory);
    }

    private ChannelFactoryBuilder newChannelFactoryBuilder() {
        final int channelExecutorQueueSize = grpcTransportConfig.getStatChannelExecutorQueueSize();
        final UnaryCallDeadlineInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(grpcTransportConfig.getStatRequestTimeout());
        final ClientOption clientOption = grpcTransportConfig.getStatClientOption();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("StatGrpcDataSender");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setNameResolverProvider(nameResolverProvider);
        channelFactoryBuilder.addClientInterceptor(unaryCallDeadlineInterceptor);
        channelFactoryBuilder.setExecutorQueueSize(channelExecutorQueueSize);
        channelFactoryBuilder.setClientOption(clientOption);
        return channelFactoryBuilder;
    }
}