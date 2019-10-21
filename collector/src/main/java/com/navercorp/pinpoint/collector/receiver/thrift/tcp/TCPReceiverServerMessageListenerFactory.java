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

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import com.navercorp.pinpoint.rpc.server.ServerMessageListenerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author Taejin Koo
 */
class TCPReceiverServerMessageListenerFactory implements ServerMessageListenerFactory {

    private final Executor executor;
    private final TCPPacketHandler tcpPacketHandler;

    TCPReceiverServerMessageListenerFactory(Executor executor, TCPPacketHandler tcpPacketHandler) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.tcpPacketHandler = Objects.requireNonNull(tcpPacketHandler, "tcpPacketHandler");
    }

    @Override
    public ServerMessageListener create() {
        return new TCPReceiverServerMessageListener(executor, tcpPacketHandler);
    }


    private static class TCPReceiverServerMessageListener implements ServerMessageListener {

        private final Executor executor;
        private final TCPPacketHandler tcpPacketHandler;

        public TCPReceiverServerMessageListener(Executor executor, TCPPacketHandler tcpPacketHandler) {
            this.executor = Objects.requireNonNull(executor, "executor");
            this.tcpPacketHandler = Objects.requireNonNull(tcpPacketHandler, "tcpPacketHandler");
        }

        @Override
        public HandshakeResponseCode handleHandshake(Map properties) {
            return HandshakeResponseCode.SIMPLEX_COMMUNICATION;
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
        }

    }

}
