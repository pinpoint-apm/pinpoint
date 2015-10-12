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

package com.navercorp.pinpoint.rpc.packet.stream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.navercorp.pinpoint.rpc.packet.PacketType;

/**
 * @author koo.taejin
 */
public class StreamCreateFailPacket extends BasicStreamPacket {

    private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_CREATE_FAIL;

    private final StreamCode code;

    public StreamCreateFailPacket(int streamChannelId, short code) {
        this(streamChannelId, StreamCode.getCode(code));
    }

    public StreamCreateFailPacket(int streamChannelId, StreamCode code) {
        super(streamChannelId);
        this.code = code;
    }

    @Override
    public short getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 2);
        header.writeShort(getPacketType());
        header.writeInt(getStreamChannelId());
        header.writeShort(code.value());

        return header;
    }

    public static StreamCreateFailPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PACKET_TYPE;

        if (buffer.readableBytes() < 6) {
            buffer.resetReaderIndex();
            return null;
        }

        final int streamChannelId = buffer.readInt();
        final short code = buffer.readShort();

        final StreamCreateFailPacket packet = new StreamCreateFailPacket(streamChannelId, code);
        return packet;
    }

    public StreamCode getCode() {
        return code;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{streamChannelId=").append(getStreamChannelId());
        sb.append(", ");
        sb.append("code=").append(getCode());
        sb.append('}');
        return sb.toString();
    }

}
