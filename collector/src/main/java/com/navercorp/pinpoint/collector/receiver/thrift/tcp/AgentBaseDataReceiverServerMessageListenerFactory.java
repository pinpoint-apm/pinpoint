/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

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
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import com.navercorp.pinpoint.rpc.util.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
class AgentBaseDataReceiverServerMessageListenerFactory implements ServerMessageListenerFactory {

    private final Executor executor;
    private final TCPPacketHandler tcpPacketHandler;
    private final AgentEventHandler agentEventHandler;
    private final AgentLifeCycleEventHandler agentLifeCycleEventHandler;

    public AgentBaseDataReceiverServerMessageListenerFactory(Executor executor, TCPPacketHandler tcpPacketHandler, AgentEventHandler agentEventHandler, AgentLifeCycleEventHandler agentLifeCycleEventHandler) {
        this.executor = Assert.requireNonNull(executor, "executor must not be null");
        this.tcpPacketHandler = Assert.requireNonNull(tcpPacketHandler, "tcpPacketHandler must not be null");
        this.agentEventHandler = Assert.requireNonNull(agentEventHandler, "agentEventTask must not be null");
        this.agentLifeCycleEventHandler = Assert.requireNonNull(agentLifeCycleEventHandler, "agentLifeCycleTask must not be null");
    }

    @Override
    public ServerMessageListener create() {
        return new AgentBaseDataReceiverServerMessageListener(executor, tcpPacketHandler, agentEventHandler, agentLifeCycleEventHandler);
    }


    private static class AgentBaseDataReceiverServerMessageListener implements ServerMessageListener {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final Executor executor;
        private final TCPPacketHandler tcpPacketHandler;
        private final AgentEventHandler agentEventHandler;
        private final AgentLifeCycleEventHandler agentLifeCycleEventHandler;

        private AgentBaseDataReceiverServerMessageListener(Executor executor, TCPPacketHandler tcpPacketHandler, AgentEventHandler agentEventHandler, AgentLifeCycleEventHandler agentLifeCycleEventHandler) {
            this.executor = Assert.requireNonNull(executor, "executor must not be null");
            this.tcpPacketHandler = Assert.requireNonNull(tcpPacketHandler, "tcpPacketHandler must not be null");
            this.agentEventHandler = Assert.requireNonNull(agentEventHandler, "agentEventTask must not be null");
            this.agentLifeCycleEventHandler = Assert.requireNonNull(agentLifeCycleEventHandler, "agentLifeCycleTask must not be null");
        }

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
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    tcpPacketHandler.handleSend(sendPacket, pinpointSocket);
                }
            });
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    tcpPacketHandler.handleRequest(requestPacket, pinpointSocket);
                }
            });
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            final int eventCounter = pingPacket.getPingId();
            long pingTimestamp = System.currentTimeMillis();
            try {
                if (!(eventCounter < 0)) {
                    agentLifeCycleEventHandler.handleLifeCycleEvent(pinpointServer, pingTimestamp, AgentLifeCycleState.RUNNING, eventCounter);
                }
                agentEventHandler.handleEvent(pinpointServer, pingTimestamp, AgentEventType.AGENT_PING);
            } catch (Exception e) {
                logger.warn("Error handling ping event", e);
            }
        }

    }

}
