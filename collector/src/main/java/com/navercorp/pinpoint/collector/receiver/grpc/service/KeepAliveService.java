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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.trace.KeepAliveGrpc;
import com.navercorp.pinpoint.grpc.trace.PPing;
import com.navercorp.pinpoint.rpc.client.HandshakerFactory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.DefaultChannelProperties;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KeepAliveService extends KeepAliveGrpc.KeepAliveImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentEventAsyncTaskService agentEventAsyncTask;
    private final AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask;
    private final SocketIdProvider socketIdProvider;
    private final ChannelPropertiesFactory channelPropertiesFactory;

    public KeepAliveService(AgentEventAsyncTaskService agentEventAsyncTask, AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask,
                            SocketIdProvider socketIdProvider, ChannelPropertiesFactory channelPropertiesFactory) {
        this.agentEventAsyncTask = Objects.requireNonNull(agentEventAsyncTask, "agentEventAsyncTask must not be null");
        this.agentLifeCycleAsyncTask = Objects.requireNonNull(agentLifeCycleAsyncTask, "agentLifeCycleAsyncTask must not be null");
        this.socketIdProvider = Objects.requireNonNull(socketIdProvider, "socketIdProvider must not be null");
        this.channelPropertiesFactory = Objects.requireNonNull(channelPropertiesFactory, "channelPropertiesFactory must not be null");
    }

    @Override
    public StreamObserver<PPing> clientKeepAlive(StreamObserver<PPing> responseObserver) {
        final ServerCallStreamObserver<PPing> serverCallStreamObserver = (ServerCallStreamObserver<PPing>) responseObserver;
        serverCallStreamObserver.setOnReadyHandler(new Runnable() {
            @Override
            public void run() {
                if (serverCallStreamObserver.isReady()) {
                    // TODO Connect
                    logger.debug("Connected client keep-alive");
                    // updateState(ManagedAgentLifeCycle.RUNNING);
                }
            }
        });

        serverCallStreamObserver.setOnCancelHandler(new Runnable() {
            @Override
            public void run() {
                // TODO Disconnect. Closed by client or server ?
                if (serverCallStreamObserver.isCancelled()) {
                    logger.debug("Disconnected client keep-alive");
                    // updateState(ManagedAgentLifeCycle.CLOSED_BY_CLIENT);
                }
            }
        });

        return new StreamObserver<PPing>() {
            @Override
            public void onNext(PPing pPing) {
                logger.debug("Received client keep-alive " + pPing);
                // TODO
//                updateState(pPing);
                responseObserver.onNext(PPing.newBuilder().build());
            }

            @Override
            public void onError(Throwable throwable) {
                Status status = Status.fromThrowable(throwable);
                logger.warn("Failed to client keep-alive status={}", status);
                //throwable.printStackTrace();
            }

            @Override
            public void onCompleted() {
                logger.debug("Completed client keep-alive");
                responseObserver.onCompleted();
            }
        };
    }

    private void updateState(final PPing ping) {
        final int eventCounter = ping.getId();
        final long pingTimestamp = System.currentTimeMillis();
        final ChannelProperties channelProperties = channelPropertiesFactory.newChannelProperties(Collections.emptyMap());
        try {
            if (!(eventCounter < 0)) {
                agentLifeCycleAsyncTask.handleLifeCycleEvent(channelProperties, pingTimestamp, AgentLifeCycleState.RUNNING, eventCounter);
            }
            agentEventAsyncTask.handleEvent(channelProperties, pingTimestamp, AgentEventType.AGENT_PING);
        } catch (Exception e) {
            logger.warn("Error handling client ping event", e);
        }
    }

    private void updateState(ManagedAgentLifeCycle managedAgentLifeCycle) {

        final Context current = Context.current();
        final AgentHeaderFactory.Header header = ServerContext.getAgentInfo(current);
        if (header == null) {
            logger.warn("Not found request header");
            return;
        }
        final TransportMetadata transportMetadata = ServerContext.getTransportMetadata(current);
        if (transportMetadata == null) {
            logger.warn("Not found transportMetadata");
            return;
        }

        long eventTimestamp = System.currentTimeMillis();
        Map<Object, Object> properties = new HashMap<>();
        // TODO type miss match  int != long
        final int socketId = (int) socketIdProvider.getSocketId();
        properties.put(HandshakerFactory.SOCKET_ID, socketId);
        properties.put(HandshakePropertyType.AGENT_ID.getName(), header.getAgentId());
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), header.getAgentStartTime());
        final ChannelProperties channelProperties = channelPropertiesFactory.newChannelProperties(properties);

        AgentLifeCycleState agentLifeCycleState = managedAgentLifeCycle.getMappedState();
        AgentEventType agentEventType = managedAgentLifeCycle.getMappedEvent();
        try {
            this.agentLifeCycleAsyncTask.handleLifeCycleEvent(channelProperties, eventTimestamp, agentLifeCycleState, managedAgentLifeCycle.getEventCounter());
            this.agentEventAsyncTask.handleEvent(channelProperties, eventTimestamp, agentEventType);
        } catch (Exception e) {
            logger.warn("Failed to update state. header={}, lifeCycle={}", header, managedAgentLifeCycle);
        }
    }
}