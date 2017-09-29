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

import com.navercorp.pinpoint.common.util.Assert;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.navercorp.pinpoint.rpc.packet.PacketType;
import com.navercorp.pinpoint.rpc.packet.PayloadPacket;

/**
 * @author koo.taejin
 */
public class StreamResponsePacket extends BasicStreamPacket {

    private final static short PACKET_TYPE = PacketType.APPLICATION_STREAM_RESPONSE;

    private final byte[] payload;

    public StreamResponsePacket(int streamChannelId, byte[] payload) {
        super(streamChannelId);

        Assert.requireNonNull(payload, "payload must not be null");
        this.payload = payload;
    }

    @Override
    public short getPacketType() {
        return PACKET_TYPE;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        header.writeShort(getPacketType());
        header.writeInt(getStreamChannelId());

        return PayloadPacket.appendPayload(header, payload);
    }

    public static StreamResponsePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PACKET_TYPE;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int streamChannelId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }

        final StreamResponsePacket packet = new StreamResponsePacket(streamChannelId, payload.array());
        return packet;
    }

}
