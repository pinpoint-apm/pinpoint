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

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.control.ProtocolException;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.server.handler.HandshakerHandler;
import com.navercorp.pinpoint.rpc.util.ControlMessageEncodingUtils;

/**
 * @author koo.taejin
 */
public class PinpointServerSocketHandshaker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public HandshakeResponseCode handleHandshake(Map<Object, Object> handshakeData, HandshakerHandler handler) {
        if (handshakeData == null) {
            return HandshakeResponseType.ProtocolError.PROTOCOL_ERROR;
        }

        HandshakeResponseCode code = handler.handleHandshake(handshakeData);
        
        return code;
    }
    
    public void sendHandshakeResponse(Channel channel, int requestId, HandshakeResponseCode responseCode, boolean isFirst) {
        if (!isFirst) {
            if (HandshakeResponseCode.DUPLEX_COMMUNICATION == responseCode) {
                sendHandshakeResponse0(channel, requestId, HandshakeResponseCode.ALREADY_DUPLEX_COMMUNICATION);
            } else if (HandshakeResponseCode.SIMPLEX_COMMUNICATION == responseCode) {
                sendHandshakeResponse0(channel, requestId, HandshakeResponseCode.ALREADY_SIMPLEX_COMMUNICATION);
            } else {
                sendHandshakeResponse0(channel, requestId, responseCode);
            }
        } else {
            sendHandshakeResponse0(channel, requestId, responseCode);
        }
    }

    private void sendHandshakeResponse0(Channel channel, int requestId, HandshakeResponseCode handShakeResponseCode) {
        try {
            logger.info("write HandshakeResponsePakcet. channel:{}, HandshakeResponseCode:{}.", channel, handShakeResponseCode);

            Map<String, Object> result = new HashMap<String, Object>();
            result.put(ControlHandshakeResponsePacket.CODE, handShakeResponseCode.getCode());
            result.put(ControlHandshakeResponsePacket.SUB_CODE, handShakeResponseCode.getSubCode());

            byte[] resultPayload = ControlMessageEncodingUtils.encode(result);
            ControlHandshakeResponsePacket packet = new ControlHandshakeResponsePacket(requestId, resultPayload);

            channel.write(packet);
        } catch (ProtocolException e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    public Map<Object, Object> decodeHandshakePacket(ControlHandshakePacket message) {
        try {
            byte[] payload = message.getPayload();
            Map<Object, Object> properties = (Map) ControlMessageEncodingUtils.decode(payload);
            return properties;
        } catch (ProtocolException e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

}
