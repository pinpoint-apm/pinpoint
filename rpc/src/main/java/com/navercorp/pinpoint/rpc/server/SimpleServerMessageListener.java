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

import com.navercorp.pinpoint.rpc.client.SimpleMessageListener;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author emeroad
 */
public class SimpleServerMessageListener extends SimpleMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final SimpleServerMessageListener SIMPLEX_INSTANCE = new SimpleServerMessageListener(HandshakeResponseType.Success.SIMPLEX_COMMUNICATION);
    public static final SimpleServerMessageListener DUPLEX_INSTANCE = new SimpleServerMessageListener(HandshakeResponseType.Success.DUPLEX_COMMUNICATION);

    public static final SimpleServerMessageListener SIMPLEX_ECHO_INSTANCE = new SimpleServerMessageListener(true, HandshakeResponseType.Success.SIMPLEX_COMMUNICATION);
    public static final SimpleServerMessageListener DUPLEX_ECHO_INSTANCE = new SimpleServerMessageListener(true, HandshakeResponseType.Success.DUPLEX_COMMUNICATION);

    private final HandshakeResponseCode handshakeResponseCode;

    public SimpleServerMessageListener(HandshakeResponseCode handshakeResponseCode) {
        this(false, handshakeResponseCode);
    }

    public SimpleServerMessageListener(boolean echo, HandshakeResponseCode handshakeResponseCode) {
        super(echo);
        this.handshakeResponseCode = handshakeResponseCode;
    }

    @Override
    public HandshakeResponseCode handleHandshake(Map properties) {
        logger.info("handleHandshake properties:{}", properties);
        return handshakeResponseCode;
    }

    @Override
    public void handlePing(PingPayloadPacket pingPacket, PinpointServer pinpointServer) {
        logger.info("handlePing packet:{}, remote:{}", pingPacket, pinpointServer.getRemoteAddress());
    }

}
