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
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.grpc.client.config.ClientOption;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.sender.grpc.MetadataGrpcDataSender;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;
import io.netty.handler.ssl.SslContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class MetadataGrpcDataSenderProvider implements Provider<EnhancedDataSender<MetaDataType, ResponseMessage>> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;
    private final NameResolverProvider nameResolverProvider;
    private List<ClientInterceptor> clientInterceptorList;
    private final Provider<SslContext> sslContextProvider;

    @Inject
    public MetadataGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                          @MetadataDataSender MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter,
                                          HeaderFactory headerFactory,
                                          NameResolverProvider nameResolverProvider,
                                          Provider<SslContext> sslContextProvider) {
        this.grpcTransportConfig = Objects.requireNonNull(grpcTransportConfig, "grpcTransportConfig");
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
        this.headerFactory = Objects.requireNonNull(headerFactory, "headerFactory");
        this.nameResolverProvider = Objects.requireNonNull(nameResolverProvider, "nameResolverProvider");
        this.sslContextProvider = Objects.requireNonNull(sslContextProvider, "sslContextProvider");
    }

    @Inject(optional = true)
    public void setClientInterceptor(@MetadataDataSender List<ClientInterceptor> clientInterceptorList) {
//    public void setClientInterceptor(@Named(SET_CLIENT_INTERCEPTOR) List<ClientInterceptor> clientInterceptorList) {
        this.clientInterceptorList = Objects.requireNonNull(clientInterceptorList, "clientInterceptorList");
    }

    @Override
    public EnhancedDataSender<MetaDataType, ResponseMessage> get() {
        final String collectorIp = grpcTransportConfig.getMetadataCollectorIp();
        final int collectorPort = grpcTransportConfig.getMetadataCollectorPort();
        final boolean sslEnable = grpcTransportConfig.isMetadataSslEnable();
        final int senderExecutorQueueSize = grpcTransportConfig.getMetadataSenderExecutorQueueSize();

        final ChannelFactoryBuilder channelFactoryBuilder = newChannelFactoryBuilder(sslEnable);
        final ChannelFactory channelFactory = channelFactoryBuilder.build();

        final int retryMaxCount = grpcTransportConfig.getMetadataRetryMaxCount();
        final int retryDelayMillis = grpcTransportConfig.getMetadataRetryDelayMillis();

        return new MetadataGrpcDataSender<>(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter, channelFactory, retryMaxCount, retryDelayMillis);
    }

    protected ChannelFactoryBuilder newChannelFactoryBuilder(boolean sslEnable) {
        final int channelExecutorQueueSize = grpcTransportConfig.getMetadataChannelExecutorQueueSize();
        final UnaryCallDeadlineInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(grpcTransportConfig.getMetadataRequestTimeout());
        final ClientOption clientOption = grpcTransportConfig.getMetadataClientOption();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("MetadataGrpcDataSender");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setNameResolverProvider(nameResolverProvider);
        channelFactoryBuilder.addClientInterceptor(unaryCallDeadlineInterceptor);
        if (clientInterceptorList != null) {
            for (ClientInterceptor clientInterceptor : clientInterceptorList) {
                logger.info("addClientInterceptor:{}", clientInterceptor);
                channelFactoryBuilder.addClientInterceptor(clientInterceptor);
            }
        }
        channelFactoryBuilder.setExecutorQueueSize(channelExecutorQueueSize);
        channelFactoryBuilder.setClientOption(clientOption);

        if (sslEnable) {
            SslContext sslContext = sslContextProvider.get();
            channelFactoryBuilder.setSslContext(sslContext);
        }

        return channelFactoryBuilder;
    }
}