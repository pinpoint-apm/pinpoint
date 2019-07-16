/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.grpc.client.ChannelFactoryOption;
import com.navercorp.pinpoint.grpc.client.ClientOption;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.module.MetadataConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.MetadataGrpcDataSender;
import io.grpc.NameResolverProvider;

/**
 * @author jaehong.kim
 */
public class MetadataGrpcDataSenderProvider implements Provider<EnhancedDataSender<Object>> {
    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;
    private final NameResolverProvider nameResolverProvider;

    @Inject
    public MetadataGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                          @MetadataConverter MessageConverter<GeneratedMessageV3> messageConverter,
                                          HeaderFactory headerFactory,
                                          NameResolverProvider nameResolverProvider) {
        this.grpcTransportConfig = Assert.requireNonNull(grpcTransportConfig, "grpcTransportConfig must not be null");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter must not be null");
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory must not be null");
        this.nameResolverProvider = Assert.requireNonNull(nameResolverProvider, "nameResolverProvider must not be null");
    }

    @Override
    public EnhancedDataSender<Object> get() {
        final String collectorIp = grpcTransportConfig.getMetadataCollectorIp();
        final int collectorPort = grpcTransportConfig.getMetadataCollectorPort();
        final int senderExecutorQueueSize = grpcTransportConfig.getMetadataSenderExecutorQueueSize();
        final int channelExecutorQueueSize = grpcTransportConfig.getMetadataChannelExecutorQueueSize();
        final UnaryCallDeadlineInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(grpcTransportConfig.getMetadataRequestTimeout());
        final ClientOption clientOption = grpcTransportConfig.getMetadataClientOption();

        final ChannelFactoryOption.Builder channelFactoryOptionBuilder = ChannelFactoryOption.newBuilder();
        channelFactoryOptionBuilder.setName("MetadataGrpcDataSender");
        channelFactoryOptionBuilder.setHeaderFactory(headerFactory);
        channelFactoryOptionBuilder.setNameResolverProvider(nameResolverProvider);
        channelFactoryOptionBuilder.addClientInterceptor(unaryCallDeadlineInterceptor);
        channelFactoryOptionBuilder.setExecutorQueueSize(channelExecutorQueueSize);
        channelFactoryOptionBuilder.setClientOption(clientOption);

        final int retryMaxCount = grpcTransportConfig.getMetadataRetryMaxCount();
        final int retryDelayMillis = grpcTransportConfig.getMetadataRetryDelayMillis();

        return new MetadataGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter, channelFactoryOptionBuilder.build(), retryMaxCount, retryDelayMillis);
    }
}