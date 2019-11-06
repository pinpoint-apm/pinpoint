/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.codec;

import com.navercorp.pinpoint.rpc.packet.PacketType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * ServerPacketDecoder passes the PING related packets(without status value) to the next step.
 *
 * @author emeroad
 * @author koo.taejin
 */
public class ServerPacketDecoder extends PacketDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if (buffer.readableBytes() < 2) {
            return null;
        }
        buffer.markReaderIndex();
        final short packetType = buffer.readShort();
        switch (packetType) {
            case PacketType.APPLICATION_SEND:
                return readSend(packetType, buffer);
            case PacketType.APPLICATION_REQUEST:
                return readRequest(packetType, buffer);
            case PacketType.APPLICATION_RESPONSE:
                return readResponse(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE:
                return readStreamCreate(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CLOSE:
                return readStreamClose(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                return readStreamCreateSuccess(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                return readStreamCreateFail(packetType, buffer);
            case PacketType.APPLICATION_STREAM_RESPONSE:
                return readStreamData(packetType, buffer);
            case PacketType.APPLICATION_STREAM_PING:
                return readStreamPing(packetType, buffer);
            case PacketType.APPLICATION_STREAM_PONG:
                return readStreamPong(packetType, buffer);
            case PacketType.CONTROL_CLIENT_CLOSE:
                return readControlClientClose(packetType, buffer);
            case PacketType.CONTROL_SERVER_CLOSE:
                return readControlServerClose(packetType, buffer);
            case PacketType.CONTROL_PING_SIMPLE:
                return readPing(packetType, buffer);
            case PacketType.CONTROL_PING_PAYLOAD:
                return readPayloadPing(packetType, buffer);
            case PacketType.CONTROL_PING:
                return readLegacyPing(packetType, buffer);
            case PacketType.CONTROL_PONG:
                logger.debug("receive pong. {}", channel);
                readPong(packetType, buffer);
                // just also drop pong.
                return null;
            case PacketType.CONTROL_HANDSHAKE:
                return readEnableWorker(packetType, buffer);
            case PacketType.CONTROL_HANDSHAKE_RESPONSE:
                return readEnableWorkerConfirm(packetType, buffer);
        }
        logger.error("invalid packetType received. packetType:{}, channel:{}", packetType, channel);
        channel.close();
        return null;
    }

}
