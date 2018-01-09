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

package com.navercorp.pinpoint.rpc.codec;

import com.navercorp.pinpoint.rpc.packet.PingPayloadPacket;
import com.navercorp.pinpoint.rpc.packet.PingSimplePacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.client.WriteFailFutureListener;
import com.navercorp.pinpoint.rpc.packet.ClientClosePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakePacket;
import com.navercorp.pinpoint.rpc.packet.ControlHandshakeResponsePacket;
import com.navercorp.pinpoint.rpc.packet.PacketType;
import com.navercorp.pinpoint.rpc.packet.PingPacket;
import com.navercorp.pinpoint.rpc.packet.PongPacket;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;
import com.navercorp.pinpoint.rpc.packet.ServerClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamClosePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateFailPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreatePacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCreateSuccessPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPingPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamPongPacket;
import com.navercorp.pinpoint.rpc.packet.stream.StreamResponsePacket;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class PacketDecoder extends FrameDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final WriteFailFutureListener pongWriteFutureListener = new WriteFailFutureListener(logger, "pong write fail.", "pong write success.");

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
                PingSimplePacket pingPacket = (PingSimplePacket) readPing(packetType, buffer);
                if (pingPacket == PingSimplePacket.PING_PACKET) {
                    sendPong(channel);
                    return null;
                }
            case PacketType.CONTROL_PING_PAYLOAD:
                return  readPayloadPing(packetType, buffer);
            case PacketType.CONTROL_PING:
                PingPacket legacyPingPacket = (PingPacket) readLegacyPing(packetType, buffer);
                if (legacyPingPacket == PingPacket.PING_PACKET) {
                    sendPong(channel);
                    // just drop ping
                    return null;
                }
                return legacyPingPacket;
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

    private void sendPong(Channel channel) {

        // a "pong" responds to a "ping" automatically.
        logger.debug("received ping. sending pong. {}", channel);
        ChannelFuture write = channel.write(PongPacket.PONG_PACKET);
        write.addListener(pongWriteFutureListener);
    }


    Object readControlClientClose(short packetType, ChannelBuffer buffer) {
        return ClientClosePacket.readBuffer(packetType, buffer);
    }

    Object readControlServerClose(short packetType, ChannelBuffer buffer) {
        return ServerClosePacket.readBuffer(packetType, buffer);
    }

    Object readPong(short packetType, ChannelBuffer buffer) {
        return PongPacket.readBuffer(packetType, buffer);
    }

    Object readPing(short packetType, ChannelBuffer buffer) {
        return PingSimplePacket.readBuffer(packetType, buffer);
    }

    Object readPayloadPing(short packetType, ChannelBuffer buffer) {
        return PingPayloadPacket.readBuffer(packetType, buffer);
    }

    @Deprecated
    Object readLegacyPing(short packetType, ChannelBuffer buffer) {
        return PingPacket.readBuffer(packetType, buffer);
    }

    Object readSend(short packetType, ChannelBuffer buffer) {
        return SendPacket.readBuffer(packetType, buffer);
    }


    Object readRequest(short packetType, ChannelBuffer buffer) {
        return RequestPacket.readBuffer(packetType, buffer);
    }

    Object readResponse(short packetType, ChannelBuffer buffer) {
        return ResponsePacket.readBuffer(packetType, buffer);
    }



    Object readStreamCreate(short packetType, ChannelBuffer buffer) {
        return StreamCreatePacket.readBuffer(packetType, buffer);
    }


    Object readStreamCreateSuccess(short packetType, ChannelBuffer buffer) {
        return StreamCreateSuccessPacket.readBuffer(packetType, buffer);
    }

    Object readStreamCreateFail(short packetType, ChannelBuffer buffer) {
        return StreamCreateFailPacket.readBuffer(packetType, buffer);
    }

    Object readStreamData(short packetType, ChannelBuffer buffer) {
        return StreamResponsePacket.readBuffer(packetType, buffer);
    }
    
    Object readStreamPong(short packetType, ChannelBuffer buffer) {
        return StreamPongPacket.readBuffer(packetType, buffer);
    }

    Object readStreamPing(short packetType, ChannelBuffer buffer) {
        return StreamPingPacket.readBuffer(packetType, buffer);
    }



    Object readStreamClose(short packetType, ChannelBuffer buffer) {
        return StreamClosePacket.readBuffer(packetType, buffer);
    }

    Object readEnableWorker(short packetType, ChannelBuffer buffer) {
        return ControlHandshakePacket.readBuffer(packetType, buffer);
    }

    Object readEnableWorkerConfirm(short packetType, ChannelBuffer buffer) {
        return ControlHandshakeResponsePacket.readBuffer(packetType, buffer);
    }

}
