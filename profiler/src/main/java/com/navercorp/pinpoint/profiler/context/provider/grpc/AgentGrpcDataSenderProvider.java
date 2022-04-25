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
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.grpc.client.config.ClientOption;
import com.navercorp.pinpoint.grpc.client.config.SslOption;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.module.AgentDataSender;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandLocatorBuilder;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcActiveThreadCountService;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcActiveThreadDumpService;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcActiveThreadLightDumpService;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcEchoService;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcSamplingRateService;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.AgentGrpcDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;


/**
 * @author jaehong.kim
 */
public class AgentGrpcDataSenderProvider implements Provider<EnhancedDataSender<MetaDataType>> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;

    private final Provider<ReconnectExecutor> reconnectExecutorProvider;
    private final ScheduledExecutorService retransmissionExecutor;

    private final NameResolverProvider nameResolverProvider;
    private final ActiveTraceRepository activeTraceRepository;

    private List<ClientInterceptor> clientInterceptorList;
    private final Sampler sampler;

    @Inject
    public AgentGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                       @MetadataDataSender MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter,
                                       HeaderFactory headerFactory,
                                       Provider<ReconnectExecutor> reconnectExecutor,
                                       ScheduledExecutorService retransmissionExecutor,
                                       NameResolverProvider nameResolverProvider,
                                       ActiveTraceRepository activeTraceRepository,
                                       Provider<Sampler> samplerProvider) {
        this.grpcTransportConfig = Objects.requireNonNull(grpcTransportConfig, "grpcTransportConfig");
        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");
        this.headerFactory = Objects.requireNonNull(headerFactory, "headerFactory");

        this.reconnectExecutorProvider = Objects.requireNonNull(reconnectExecutor, "reconnectExecutorProvider");
        this.retransmissionExecutor = Objects.requireNonNull(retransmissionExecutor, "retransmissionExecutor");


        this.nameResolverProvider = Objects.requireNonNull(nameResolverProvider, "nameResolverProvider");
        this.activeTraceRepository = Objects.requireNonNull(activeTraceRepository, "activeTraceRepository");
        Objects.requireNonNull(samplerProvider, "samplerProvider");
        this.sampler = samplerProvider.get();
    }

    @Inject(optional = true)
    public void setClientInterceptor(@AgentDataSender List<ClientInterceptor> clientInterceptorList) {
        this.clientInterceptorList = Objects.requireNonNull(clientInterceptorList, "clientInterceptorList");
    }

    @Override
    public EnhancedDataSender<MetaDataType> get() {
        final String collectorIp = grpcTransportConfig.getAgentCollectorIp();
        final int collectorPort = grpcTransportConfig.getAgentCollectorPort();
        final boolean sslEnable = grpcTransportConfig.isAgentSslEnable();
        final int senderExecutorQueueSize = grpcTransportConfig.getAgentSenderExecutorQueueSize();

        final ChannelFactoryBuilder channelFactoryBuilder = newChannelFactoryBuilder(sslEnable);
        ChannelFactory channelFactory = channelFactoryBuilder.build();

        final ReconnectExecutor reconnectExecutor = reconnectExecutorProvider.get();

        final ProfilerCommandServiceLocator profilerCommandServiceLocator = createProfilerCommandServiceLocator(activeTraceRepository);

        MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter = this.messageConverter;
        return newAgentGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter,
                channelFactory, reconnectExecutor, retransmissionExecutor, profilerCommandServiceLocator);
    }

    protected EnhancedDataSender<MetaDataType> newAgentGrpcDataSender(String collectorIp, int collectorPort, int senderExecutorQueueSize,
                                                                MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter,
                                                                ChannelFactory channelFactory, ReconnectExecutor reconnectExecutor,
                                                                ScheduledExecutorService retransmissionExecutor,
                                                                ProfilerCommandServiceLocator profilerCommandServiceLocator) {
        return new AgentGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter, reconnectExecutor, retransmissionExecutor, channelFactory, profilerCommandServiceLocator);
    }

    ChannelFactoryBuilder newChannelFactoryBuilder(boolean sslEnable) {
        final int channelExecutorQueueSize = grpcTransportConfig.getAgentChannelExecutorQueueSize();
        final UnaryCallDeadlineInterceptor unaryCallDeadlineInterceptor = new UnaryCallDeadlineInterceptor(grpcTransportConfig.getAgentRequestTimeout());
        final ClientOption clientOption = grpcTransportConfig.getAgentClientOption();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("AgentGrpcDataSender");
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
            SslOption sslOption = grpcTransportConfig.getSslOption();
            channelFactoryBuilder.setSslOption(sslOption);
        }

        return channelFactoryBuilder;
    }

    private ProfilerCommandServiceLocator createProfilerCommandServiceLocator(ActiveTraceRepository activeTraceRepository) {
        ProfilerCommandLocatorBuilder profilerCommandLocatorBuilder = new ProfilerCommandLocatorBuilder();

        profilerCommandLocatorBuilder.addService(new GrpcEchoService());
        profilerCommandLocatorBuilder.addService(new GrpcSamplingRateService(sampler));
        if (activeTraceRepository != null) {
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadCountService(activeTraceRepository));
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadDumpService(activeTraceRepository));
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadLightDumpService(activeTraceRepository));
        }

        final ProfilerCommandServiceLocator commandServiceLocator = profilerCommandLocatorBuilder.build();
        return commandServiceLocator;
    }
}