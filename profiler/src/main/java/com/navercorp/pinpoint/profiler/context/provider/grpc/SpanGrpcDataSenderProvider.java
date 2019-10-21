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

import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.grpc.client.ClientOption;

import com.navercorp.pinpoint.profiler.context.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.profiler.context.module.SpanConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.DiscardClientInterceptor;
import com.navercorp.pinpoint.profiler.sender.grpc.DiscardEventListener;
import com.navercorp.pinpoint.profiler.sender.grpc.LoggingDiscardEventListener;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import com.navercorp.pinpoint.profiler.sender.grpc.SpanGrpcDataSender;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanGrpcDataSenderProvider implements Provider<DataSender<Object>> {
    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;
    private final Provider<ReconnectExecutor> reconnectExecutor;
    private final NameResolverProvider nameResolverProvider;

    @Inject
    public SpanGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                      @SpanConverter MessageConverter<GeneratedMessageV3> messageConverter,
                                      HeaderFactory headerFactory,
                                      Provider<ReconnectExecutor> reconnectExecutor,
                                      NameResolverProvider nameResolverProvider) {
        this.grpcTransportConfig = Assert.requireNonNull(grpcTransportConfig, "grpcTransportConfig");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory");

        this.reconnectExecutor = Assert.requireNonNull(reconnectExecutor, "reconnectExecutor");

        this.nameResolverProvider = Assert.requireNonNull(nameResolverProvider, "nameResolverProvider");
    }

    @Override
    public DataSender<Object> get() {
        final String collectorIp = grpcTransportConfig.getSpanCollectorIp();
        final int collectorPort = grpcTransportConfig.getSpanCollectorPort();
        final int senderExecutorQueueSize = grpcTransportConfig.getSpanSenderExecutorQueueSize();
        final ChannelFactoryBuilder channelFactoryBuilder = newChannelFactoryBuilder();
        final ChannelFactory channelFactory = channelFactoryBuilder.build();

        final ReconnectExecutor reconnectExecutor = this.reconnectExecutor.get();
        return new SpanGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter, reconnectExecutor, channelFactory);
    }

    protected ChannelFactoryBuilder newChannelFactoryBuilder() {
        final int channelExecutorQueueSize = grpcTransportConfig.getSpanChannelExecutorQueueSize();
        final ClientOption clientOption = grpcTransportConfig.getSpanClientOption();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("SpanGrpcDataSender");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setNameResolverProvider(nameResolverProvider);

        final ClientInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(grpcTransportConfig.getSpanRequestTimeout());
        channelFactoryBuilder.addClientInterceptor(unaryCallDeadlineInterceptor);
        final ClientInterceptor discardClientInterceptor = newDiscardClientInterceptor();
        channelFactoryBuilder.addClientInterceptor(discardClientInterceptor);

        channelFactoryBuilder.setExecutorQueueSize(channelExecutorQueueSize);
        channelFactoryBuilder.setClientOption(clientOption);
        return channelFactoryBuilder;
    }

    private ClientInterceptor newDiscardClientInterceptor() {
        final int spanDiscardLogRateLimit = grpcTransportConfig.getSpanDiscardLogRateLimit();
        final long spanDiscardMaxPendingThreshold = grpcTransportConfig.getSpanDiscardMaxPendingThreshold();
        final DiscardEventListener<?> discardEventListener = new LoggingDiscardEventListener(SpanGrpcDataSender.class.getName(), spanDiscardLogRateLimit);
        return new DiscardClientInterceptor(discardEventListener, spanDiscardMaxPendingThreshold);
    }

}