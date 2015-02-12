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

package com.navercorp.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */
public class ServerClosePacket extends BasicPacket {

    public static final ServerClosePacket DEFAULT_SERVER_CLOSE_PACKET = new ServerClosePacket();
    private static final byte[] DEFAULT_SERVER_CLOSE_PACKET_BUFFER;
    static {
        ChannelBuffer buffer = ChannelBuffers.buffer(6);
        buffer.writeShort(PacketType.CONTROL_SERVER_CLOSE);
        buffer.writeInt(-1);
        DEFAULT_SERVER_CLOSE_PACKET_BUFFER = buffer.array();
    }
    
    @Override
    public short getPacketType() {
        return PacketType.CONTROL_SERVER_CLOSE;
    }

    @Override
    public ChannelBuffer toBuffer() {
        if (DEFAULT_SERVER_CLOSE_PACKET == this) {
            return ChannelBuffers.wrappedBuffer(DEFAULT_SERVER_CLOSE_PACKET_BUFFER);
        }
        
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketType.CONTROL_SERVER_CLOSE);

        return PayloadPacket.appendPayload(header, payload);
    }

    public static ServerClosePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_SERVER_CLOSE;

        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }

        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final ServerClosePacket requestPacket = new ServerClosePacket();
        return requestPacket;

    }

    @Override
    public String toString() {
        return "ServerClosePacket";
    }
}
