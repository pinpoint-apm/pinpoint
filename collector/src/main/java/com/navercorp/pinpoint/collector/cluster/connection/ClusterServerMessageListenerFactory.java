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

package com.navercorp.pinpoint.collector.cluster.connection;

import com.navercorp.pinpoint.rpc.MessageListener;
import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Taejin Koo
 */
class ClusterServerMessageListenerFactory implements ServerMessageListenerFactory {

    private final String clusterId;
    private final MessageListener routeMessageListener;

    public ClusterServerMessageListenerFactory(String clusterId, MessageListener routeMessageListener) {
        this.clusterId = clusterId;
        this.routeMessageListener = routeMessageListener;
    }

    @Override
    public ServerMessageListener create() {
        return new ClusterServerMessageListener(clusterId, routeMessageListener);
    }


    private static class ClusterServerMessageListener implements ServerMessageListener {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final String clusterId;
        private final MessageListener routeMessageListener;

        public ClusterServerMessageListener(String clusterId, MessageListener routeMessageListener) {
            this.clusterId = clusterId;
            this.routeMessageListener = routeMessageListener;
        }

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

            // TODO : need handle control message (looks like getClusterId, ..)
            routeMessageListener.handleRequest(requestPacket, pinpointSocket);
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.info("handle handShake {}", properties);
            return HandshakeResponseCode.DUPLEX_COMMUNICATION;
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            logger.info("ping received packet:{}, remote:{}", pingPacket, pinpointServer);
        }

    }

}
