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

package com.navercorp.pinpoint.rpc;

import com.navercorp.pinpoint.rpc.packet.*;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.ServerMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author emeroad
 */
public class RequestResponseServerMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final RequestResponseServerMessageListener LISTENER = new RequestResponseServerMessageListener();

    @Override
    public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        logger.info("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        logger.info("handleRequest packet:{}, remote:{}", requestPacket, pinpointSocket.getRemoteAddress());
        pinpointSocket.response(requestPacket, requestPacket.getPayload());
    }

    @Override
    public HandshakeResponseCode handleHandshake(Map properties) {
        logger.info("handle handShake {}", properties);
        return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
    }

    @Override
    public void handlePing(PingPacket pingPacket, PinpointServer pinpointServer) {
        logger.info("ping received {} {} ", pingPacket, pinpointServer);
    }

}
