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
import com.navercorp.pinpoint.grpc.client.config.ClientOption;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.UnaryCallDeadlineInterceptor;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import com.navercorp.pinpoint.profiler.context.module.AgentDataSender;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandLocatorBuilder;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcActiveThreadCountService;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcActiveThreadDumpService;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcActiveThreadLightDumpService;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcEchoService;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.AgentGrpcDataSender;
import com.navercorp.pinpoint.profiler.sender.grpc.ReconnectExecutor;
import io.grpc.ClientInterceptor;
import io.grpc.NameResolverProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;


/**
 * @author jaehong.kim
 */
public class AgentGrpcDataSenderProvider implements Provider<EnhancedDataSender<Object>> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GrpcTransportConfig grpcTransportConfig;
    private final MessageConverter<GeneratedMessageV3> messageConverter;
    private final HeaderFactory headerFactory;

    private final Provider<ReconnectExecutor> reconnectExecutorProvider;
    private final ScheduledExecutorService retransmissionExecutor;

    private final NameResolverProvider nameResolverProvider;
    private final ActiveTraceRepository activeTraceRepository;

    private List<ClientInterceptor> clientInterceptorList;

    @Inject
    public AgentGrpcDataSenderProvider(GrpcTransportConfig grpcTransportConfig,
                                       @MetadataDataSender MessageConverter<GeneratedMessageV3> messageConverter,
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

    @Inject(optional = true)
    public void setClientInterceptor(@AgentDataSender List<ClientInterceptor> clientInterceptorList) {
        this.clientInterceptorList = Assert.requireNonNull(clientInterceptorList, "clientInterceptorList");
    }

    @Override
    public EnhancedDataSender<Object> get() {
        final String collectorIp = grpcTransportConfig.getAgentCollectorIp();
        final int collectorPort = grpcTransportConfig.getAgentCollectorPort();
        final int senderExecutorQueueSize = grpcTransportConfig.getAgentSenderExecutorQueueSize();

        final ChannelFactoryBuilder channelFactoryBuilder = newChannelFactoryBuilder();
        ChannelFactory channelFactory = channelFactoryBuilder.build();

        final ReconnectExecutor reconnectExecutor = reconnectExecutorProvider.get();

        final ProfilerCommandServiceLocator profilerCommandServiceLocator = createProfilerCommandServiceLocator(activeTraceRepository);

        return newAgentGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter,
                channelFactory, reconnectExecutor, retransmissionExecutor, profilerCommandServiceLocator);
    }

    protected EnhancedDataSender<Object> newAgentGrpcDataSender(String collectorIp, int collectorPort, int senderExecutorQueueSize,
                                                                MessageConverter<GeneratedMessageV3> messageConverter, ChannelFactory channelFactory, ReconnectExecutor reconnectExecutor,
                                                                ScheduledExecutorService retransmissionExecutor,
                                                                ProfilerCommandServiceLocator profilerCommandServiceLocator) {
        return new AgentGrpcDataSender(collectorIp, collectorPort, senderExecutorQueueSize, messageConverter, reconnectExecutor, retransmissionExecutor, channelFactory, profilerCommandServiceLocator);
    }

    ChannelFactoryBuilder newChannelFactoryBuilder() {
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
        return channelFactoryBuilder;
    }

    private ProfilerCommandServiceLocator createProfilerCommandServiceLocator(ActiveTraceRepository activeTraceRepository) {
        ProfilerCommandLocatorBuilder profilerCommandLocatorBuilder = new ProfilerCommandLocatorBuilder();

        profilerCommandLocatorBuilder.addService(new GrpcEchoService());
        if (activeTraceRepository != null) {
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadCountService(activeTraceRepository));
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadDumpService(activeTraceRepository));
            profilerCommandLocatorBuilder.addService(new GrpcActiveThreadLightDumpService(activeTraceRepository));
        }

        final ProfilerCommandServiceLocator commandServiceLocator = profilerCommandLocatorBuilder.build();
        return commandServiceLocator;
    }
}