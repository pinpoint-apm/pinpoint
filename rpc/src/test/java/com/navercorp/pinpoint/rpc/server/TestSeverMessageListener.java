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

package com.navercorp.pinpoint.rpc.server;

import com.navercorp.pinpoint.rpc.PinpointSocket;
import com.navercorp.pinpoint.rpc.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class TestSeverMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private byte[] open;
    private List<byte[]> sendMessageList = new ArrayList<byte[]>();

    @Override
    public void handleSend(SendPacket sendPacket, PinpointSocket pinpointSocket) {
        logger.debug("handleSend packet:{}, remote:{}", sendPacket, pinpointSocket.getRemoteAddress());
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, PinpointSocket pinpointSocket) {
        logger.debug("handleRequest packet:{} remote:{}", requestPacket, pinpointSocket.getRemoteAddress());

        pinpointSocket.response(requestPacket, requestPacket.getPayload());
    }

    @Override
    public HandshakeResponseCode handleHandshake(Map properties) {
        logger.debug("handle handShake properties:{} channel:{}", properties);
        return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
    }

    @Override
    public void handlePing(PingPacket pingPacket, PinpointServer pinpointServer) {
        logger.debug("pingPacket:{} channel:{} ", pingPacket, pinpointServer);
    }

    public byte[] getOpen() {
        return open;
    }

    public List<byte[]> getSendMessage() {
        return sendMessageList;
    }
}

