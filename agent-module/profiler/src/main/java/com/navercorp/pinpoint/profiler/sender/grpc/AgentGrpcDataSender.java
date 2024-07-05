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
import com.navercorp.pinpoint.common.profiler.message.AsyncDataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.profiler.message.ResultResponse;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.SocketIdClientInterceptor;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.receiver.grpc.CommandServiceStubFactory;
import com.navercorp.pinpoint.profiler.receiver.grpc.GrpcCommandService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author jaehong.kim
 */
public class AgentGrpcDataSender extends GrpcDataSender<MetaDataType> implements AsyncDataSender<MetaDataType, ResultResponse> {
    private final AgentGrpc.AgentStub agentInfoStub;
    private final AgentGrpc.AgentStub agentPingStub;
    private final GrpcCommandService grpcCommandService;

    private final ReconnectExecutor reconnectExecutor;

    private volatile PingStreamContext pingStreamContext;
    private final Reconnector reconnector;

    public AgentGrpcDataSender(String host, int port, int executorQueueSize,
                               MessageConverter<MetaDataType, GeneratedMessageV3> messageConverter,
                               ReconnectExecutor reconnectExecutor,
                               final ScheduledExecutorService retransmissionExecutor,
                               ChannelFactory channelFactory,
                               ProfilerCommandServiceLocator profilerCommandServiceLocator) {
        super(host, port, executorQueueSize, messageConverter, channelFactory);

        this.agentInfoStub = AgentGrpc.newStub(managedChannel);
        this.agentPingStub = newAgentPingStub();

        this.reconnectExecutor = reconnectExecutor;
        CommandServiceStubFactory commandServiceStubFactory = new CommandServiceStubFactory(managedChannel);
        this.grpcCommandService = new GrpcCommandService(commandServiceStubFactory, reconnectExecutor, profilerCommandServiceLocator);
        {
            final Runnable reconnectJob = new Runnable() {
                @Override
                public void run() {
                    pingStreamContext = newPingStream(agentPingStub, retransmissionExecutor);
                }
            };
            this.reconnector = reconnectExecutor.newReconnector(reconnectJob);
            reconnectJob.run();
        }
    }

    private AgentGrpc.AgentStub newAgentPingStub() {
        AgentGrpc.AgentStub agentStub = AgentGrpc.newStub(managedChannel);
        return agentStub.withInterceptors(new SocketIdClientInterceptor());
    }


    private PingStreamContext newPingStream(AgentGrpc.AgentStub agentStub, ScheduledExecutorService reconnectScheduler) {
        final PingStreamContext pingStreamContext = new PingStreamContext(agentStub, reconnector, reconnectScheduler);
        logger.info("newPingStream:{}", pingStreamContext);
        return pingStreamContext;
    }


    @Override
    public CompletableFuture<ResultResponse> request(MetaDataType data) {
        final GeneratedMessageV3 message = this.messageConverter.toMessage(data);
        if (!(message instanceof PAgentInfo)) {
            throw new IllegalArgumentException("unsupported message " + data);
        }

        final PAgentInfo pAgentInfo = (PAgentInfo) message;

        CompletableFutureObserver<PResult, ResultResponse> observer = new CompletableFutureObserver<>(PResults::toResponse);
        this.agentInfoStub.requestAgentInfo(pAgentInfo, observer);
        return observer.future();
    }


    @Override
    public void stop() {
        if (shutdown) {
            return;
        }
        this.shutdown = true;

        logger.info("Stop {}, channel={}", name, managedChannel);
        final ReconnectExecutor reconnectExecutor = this.reconnectExecutor;
        if (reconnectExecutor != null) {
            reconnectExecutor.close();
        }
        final PingStreamContext pingStreamContext = this.pingStreamContext;
        if (pingStreamContext != null) {
            pingStreamContext.close();
        }

        final GrpcCommandService grpcCommandService = this.grpcCommandService;
        if (grpcCommandService != null) {
            grpcCommandService.stop();
        }
        this.release();
    }

}