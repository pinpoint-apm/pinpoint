/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.tcp;

import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.navercorp.pinpoint.collector.config.AgentBaseDataReceiverConfiguration;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.AddressFilterAdaptor;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.collector.rpc.handler.AgentLifeCycleHandler;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.ChannelFilter;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class AgentBaseDataReceiver {

    private final Logger logger = LoggerFactory.getLogger(AgentBaseDataReceiver.class);

    private PinpointServerAcceptor serverAcceptor;

    private final AgentBaseDataReceiverConfiguration configuration;
    private final AddressFilter addressFilter;

    private final ZookeeperClusterService clusterService;

    private final Executor executor;

    private final SendPacketHandler sendPacketHandler;
    private final RequestPacketHandler requestPacketHandler;


    @Resource(name = "agentEventService")
    private AgentEventService agentEventService;

    @Resource(name = "agentLifeCycleHandler")
    private AgentLifeCycleHandler agentLifeCycleHandler;

    @Resource(name = "channelStateChangeEventHandlers")
    private List<ServerStateChangeEventHandler> channelStateChangeEventHandlers = Collections.emptyList();

    public AgentBaseDataReceiver(AgentBaseDataReceiverConfiguration configuration, Executor executor, AddressFilter addressFilter, DispatchHandler dispatchHandler) {
        this(configuration, executor, addressFilter, dispatchHandler, null);
    }

    public AgentBaseDataReceiver(AgentBaseDataReceiverConfiguration configuration, Executor executor, AddressFilter addressFilter, DispatchHandler dispatchHandler, ZookeeperClusterService service) {
        this.configuration = Assert.requireNonNull(configuration, "config must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
        this.addressFilter = Objects.requireNonNull(addressFilter, "addressFilter must not be null");

        Objects.requireNonNull(dispatchHandler, "dispatchHandler must not be null");
        this.sendPacketHandler = new SendPacketHandler(dispatchHandler);
        this.requestPacketHandler = new RequestPacketHandler(dispatchHandler);

        this.clusterService = service;
    }

    @PostConstruct
    public void start() {
        if (logger.isInfoEnabled()) {
            logger.info("start() started");
        }
        ChannelFilter connectedFilter = new AddressFilterAdaptor(addressFilter);
        PinpointServerAcceptor acceptor = new PinpointServerAcceptor(connectedFilter);
        prepare(acceptor);

        // take care when attaching message handlers as events are generated from the IO thread.
        // pass them to a separate queue and handle them in a different thread.
        acceptor.setMessageListener(new ServerMessageListener() {

            @Override
            public HandshakeResponseCode handleHandshake(Map properties) {
                if (properties == null) {
                    return HandshakeResponseType.ProtocolError.PROTOCOL_ERROR;
                }

                boolean hasRequiredKeys = HandshakePropertyType.hasRequiredKeys(properties);
                if (!hasRequiredKeys) {
                    return HandshakeResponseType.PropertyError.PROPERTY_ERROR;
                }

                boolean supportServer = MapUtils.getBoolean(properties, HandshakePropertyType.SUPPORT_SERVER.getName(), true);
                if (supportServer) {
                    return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
                } else {
                    return HandshakeResponseType.Success.SIMPLEX_COMMUNICATION;
                }
            }

            @Override
            public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
                receive(sendPacket, pinpointSocket);
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
                requestResponse(requestPacket, pinpointSocket);
            }

            @Override
            public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
                recordPing(pingPacket, pinpointServer);
            }
        });
        acceptor.bind(configuration.getBindIp(), configuration.getBindPort());

        this.serverAcceptor = acceptor;

        if (logger.isInfoEnabled()) {
            logger.info("start() completed");
        }
    }

    private void prepare(PinpointServerAcceptor acceptor) {
        if (clusterService != null && clusterService.isEnable()) {
            acceptor.addStateChangeEventHandler(clusterService.getChannelStateChangeEventHandler());
        }

        for (ServerStateChangeEventHandler channelStateChangeEventHandler : this.channelStateChangeEventHandlers) {
            acceptor.addStateChangeEventHandler(channelStateChangeEventHandler);
        }
    }

    private void receive(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendPacketHandler.handle(sendPacket, pinpointSocket);
            }
        });
    }

    private void requestResponse(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                requestPacketHandler.handle(requestPacket, pinpointSocket);
            }
        });
    }

    private void recordPing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
        final int eventCounter = pingPacket.getPingId();
        long pingTimestamp = System.currentTimeMillis();
        try {
            if (!(eventCounter < 0)) {
                agentLifeCycleHandler.handleLifeCycleEvent(pinpointServer, pingTimestamp, AgentLifeCycleState.RUNNING, eventCounter);
            }
            agentEventService.handleEvent(pinpointServer, pingTimestamp, AgentEventType.AGENT_PING);
        } catch (Exception e) {
            logger.warn("Error handling ping event", e);
        }
    }

    @PreDestroy
    public void stop() {
        if (logger.isInfoEnabled()) {
            logger.info("stop() started");
        }

        if (serverAcceptor != null) {
            serverAcceptor.close();
        }


        if (logger.isInfoEnabled()) {
            logger.info("stop() completed");
        }
    }



}
