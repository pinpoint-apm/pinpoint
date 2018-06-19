/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Taejin Koo
 */
public class LoggingServerMessageListenerFactory implements ServerMessageListenerFactory {

    private final boolean enableDuplex;

    public LoggingServerMessageListenerFactory() {
        this(false);
    }

    public LoggingServerMessageListenerFactory(boolean enableDuplex) {
        this.enableDuplex = enableDuplex;
    }

    @Override
    public ServerMessageListener create() {
        return new LoggingServerMessageListener(enableDuplex);
    }


    private static class LoggingServerMessageListener implements ServerMessageListener {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final boolean enableDuplex;

        public LoggingServerMessageListener(boolean enableDuplex) {
            this.enableDuplex = enableDuplex;
        }

        @Override
        public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
            logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
        }

        @Override
        public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
            logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());
            pinpointSocket.response(requestPacket.getRequestId(), new byte[0]);
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            logger.info("handleHandshake properties:{}", properties);

            if (enableDuplex) {
                return HandshakeResponseCode.DUPLEX_COMMUNICATION;
            } else {
                return HandshakeResponseCode.SIMPLEX_COMMUNICATION;
            }
        }

        @Override
        public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
            logger.info("handlePing packet:{}, remote:{}", pingPacket, pinpointServer.getRemoteAddress());
        }

    }

}
