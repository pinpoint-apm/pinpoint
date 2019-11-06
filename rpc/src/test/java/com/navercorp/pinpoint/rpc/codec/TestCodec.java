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

package com.navercorp.pinpoint.rpc.codec;

import com.navercorp.pinpoint.rpc.packet.Packet;
import com.navercorp.pinpoint.rpc.packet.PacketType;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author Taejin Koo
 */
public class TestCodec {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCodec.class);

    private static final ServerPacketDecoder SERVER_PACKET_DECODER = new ServerPacketDecoder();

    public static byte[] encodePacket(Packet packet) {
        ByteBuffer bb = packet.toBuffer().toByteBuffer(0, packet.toBuffer().writerIndex());
        return bb.array();
    }

    public static Packet decodePacket(byte[] payload) {
        Packet packet = null;
        try {
            packet = (Packet) SERVER_PACKET_DECODER.decode(null, null, ChannelBuffers.wrappedBuffer(payload));
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        if (packet == null) {
            try {
                ChannelBuffer channelBuffer = ChannelBuffers.wrappedBuffer(payload);
                final short packetType = channelBuffer.readShort();
                if (packetType == PacketType.CONTROL_PONG) {
                    return (Packet) SERVER_PACKET_DECODER.readPong(PacketType.CONTROL_PONG, channelBuffer);
                }
            } catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        return packet;
    }

}
