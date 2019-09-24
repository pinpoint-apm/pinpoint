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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaData;
import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.ClientOption;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.DefaultAgentInformation;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaData;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcMetadataMessageConverter;
import com.navercorp.pinpoint.profiler.context.provider.grpc.DnsExecutorServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.grpc.GrpcNameResolverProvider;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.metadata.AgentInfo;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.JvmGcType;
import io.grpc.NameResolverProvider;

import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentGrpcDataSenderTestMain {
    private static final String AGENT_ID = "mockAgentId";
    private static final String APPLICATION_NAME = "mockApplicationName";
    private static final long START_TIME = System.currentTimeMillis();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ReconnectExecutor reconnectExecutor = new ReconnectExecutor(scheduledExecutorService);


    public void request() throws Exception {
        MessageConverter<GeneratedMessageV3> messageConverter = new GrpcMetadataMessageConverter();
        HeaderFactory headerFactory = new AgentHeaderFactory(AGENT_ID, APPLICATION_NAME, START_TIME);

        DnsExecutorServiceProvider dnsExecutorServiceProvider = new DnsExecutorServiceProvider();
        GrpcNameResolverProvider grpcNameResolverProvider = new GrpcNameResolverProvider(dnsExecutorServiceProvider);
        NameResolverProvider nameResolverProvider = grpcNameResolverProvider.get();

        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("TestAgentGrpcDataSender");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setNameResolverProvider(nameResolverProvider);
        channelFactoryBuilder.setClientOption(new ClientOption.Builder().build());
        ChannelFactory channelFactory = channelFactoryBuilder.build();

        AgentGrpcDataSender sender = new AgentGrpcDataSender("localhost", 9997, 1, messageConverter,
                reconnectExecutor, scheduledExecutorService, channelFactory, null);

        AgentInfo agentInfo = newAgentInfo();

        sender.request(agentInfo);

        TimeUnit.SECONDS.sleep(60);
        sender.stop();
    }

    private AgentInfo newAgentInfo() {
        AgentInformation agentInformation = new DefaultAgentInformation(AGENT_ID, APPLICATION_NAME, true, START_TIME, 99, "", "", ServiceType.TEST_STAND_ALONE, "1.0", "1.0");
        JvmInformation jvmInformation = new JvmInformation("1.0", JvmGcType.G1);
        ServerMetaData serverInfo = new DefaultServerMetaData("serverInfo", Collections.<String>emptyList(), Collections.<Integer, String>emptyMap(), Collections.<ServiceInfo>emptyList());
        return new AgentInfo(agentInformation, serverInfo, jvmInformation);
    }

    public static void main(String[] args) {
        AgentGrpcDataSenderTestMain main = new AgentGrpcDataSenderTestMain();
        try {
            main.request();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            main.close();
        }
    }

    private void close() {
        this.scheduledExecutorService.shutdown();
    }
}