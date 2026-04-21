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

package com.navercorp.pinpoint.profiler.context.provider.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.grpc.client.config.ClientOption;
import com.navercorp.pinpoint.profiler.context.SpanType;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.grpc.config.SpanBatchSenderConfig;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.SpanBatchGrpcDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.metric.ChannelzReporter;
import com.navercorp.pinpoint.profiler.sender.grpc.metric.ChannelzScheduledReporter;
import com.navercorp.pinpoint.profiler.sender.grpc.metric.DefaultChannelzReporter;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;
import io.netty.handler.ssl.SslContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 * @author emeroad
 */
public class SpanBatchGrpcDataSenderProvider implements Provider<DataSender<SpanType>> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<SpanType, GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;
    private final NameResolverProvider nameResolverProvider;
    private final ChannelzScheduledReporter reporter;
    private final Provider<SslContext> sslContextProvider;

    private List<ClientInterceptor> clientInterceptorList;

    public static final String SPAN_CHANNELZ = "com.navercorp.pinpoint.metric.SpanChannel";

    @Inject
    public SpanBatchGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                          @SpanDataSender MessageConverter<SpanType, GeneratedMessageV3> messageConverter,
                                          HeaderFactory headerFactory,
                                          NameResolverProvider nameResolverProvider,
                                          ChannelzScheduledReporter reporter,
                                          Provider<SslContext> sslContextProvider) {
        this.grpcTransportConfig = Objects.requireNonNull(grpcTransportConfig, "grpcTransportConfig");
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
        this.headerFactory = Objects.requireNonNull(headerFactory, "headerFactory");
        this.nameResolverProvider = Objects.requireNonNull(nameResolverProvider, "nameResolverProvider");
        this.reporter = Objects.requireNonNull(reporter, "reporter");
        this.sslContextProvider = Objects.requireNonNull(sslContextProvider, "sslContextProvider");
    }

    @Inject(optional = true)
    public void setClientInterceptor(@SpanDataSender List<ClientInterceptor> clientInterceptorList) {
        this.clientInterceptorList = Objects.requireNonNull(clientInterceptorList, "clientInterceptorList");
    }

    @Override
    public DataSender<SpanType> get() {
        final String collectorIp = grpcTransportConfig.getSpanCollectorIp();
        final int collectorPort = grpcTransportConfig.getSpanCollectorPort();
        final boolean sslEnable = grpcTransportConfig.isSpanSslEnable();
        final int senderExecutorQueueSize = grpcTransportConfig.getSpanSenderExecutorQueueSize();
        final SpanBatchSenderConfig batchConfig = grpcTransportConfig.getSpanBatchSenderConfig();
        final int batchSize = batchConfig.getSize();
        final long flushIntervalMillis = batchConfig.getFlushIntervalMillis();
        final long batchCollectDeadLineTimeMillis = batchConfig.getCollectDeadlineTimeMillis();
        final int maxConcurrentRequests = batchConfig.getMaxConcurrentRequests();

        final ChannelFactoryBuilder channelFactoryBuilder = newChannelFactoryBuilder(sslEnable);
        final ChannelFactory channelFactory = channelFactoryBuilder.build();

        final SpanBatchGrpcDataSender spanListGrpcDataSender = new SpanBatchGrpcDataSender(
                collectorIp, collectorPort,
                senderExecutorQueueSize, messageConverter,
                channelFactory, batchSize, flushIntervalMillis, batchCollectDeadLineTimeMillis,
                maxConcurrentRequests);

        logger.info("SpanBatchGrpcDataSender={}", spanListGrpcDataSender);

        if (grpcTransportConfig.isSpanEnableStatLogging()) {
            registerChannelzReporter(spanListGrpcDataSender);
        }

        return spanListGrpcDataSender;
    }

    private void registerChannelzReporter(SpanBatchGrpcDataSender sender) {
        final Logger statChannelLogger = LogManager.getLogger(SPAN_CHANNELZ);
        ChannelzReporter statReporter = new DefaultChannelzReporter(statChannelLogger);
        reporter.registerRootChannel(sender.getLogId(), statReporter);
    }

    private ChannelFactoryBuilder newChannelFactoryBuilder(boolean sslEnable) {
        final int channelExecutorQueueSize = grpcTransportConfig.getSpanChannelExecutorQueueSize();
        final ClientOption clientOption = grpcTransportConfig.getSpanClientOption();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("SpanBatchGrpcDataSender");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setNameResolverProvider(nameResolverProvider);

        final ClientInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(grpcTransportConfig.getSpanRequestTimeout());
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
