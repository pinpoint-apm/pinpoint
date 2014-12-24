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

public class TraceSendAckPacket implements Packet {
    private int traceId;

    public TraceSendAckPacket() {
    }

    public TraceSendAckPacket(int traceId) {
        this.traceId = traceId;
    }

    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_TRACE_SEND_ACK;
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketType.APPLICATION_TRACE_SEND_ACK);
        header.writeInt(traceId);

        return header;
    }

    public static TraceSendAckPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_TRACE_SEND_ACK;

        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }

        final int traceId = buffer.readInt();
        return new TraceSendAckPacket(traceId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("TraceSendAckPacket");
        sb.append("{traceId=").append(traceId);
        sb.append('}');
        return sb.toString();
    }

}