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
 * @author koo.taejin
 */
public class ControlHandshakeResponsePacket extends ControlPacket {

    public static final String CODE = "code";
    public static final String SUB_CODE = "subCode";

    public static final String CLUSTER = "cluster";
    
    public ControlHandshakeResponsePacket(byte[] payload) {
        super(payload);
    }

    public ControlHandshakeResponsePacket(int requestId, byte[] payload) {
        super(payload);
        setRequestId(requestId);
    }

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_HANDSHAKE_RESPONSE;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        header.writeShort(PacketType.CONTROL_HANDSHAKE_RESPONSE);
        header.writeInt(getRequestId());

        return PayloadPacket.appendPayload(header, payload);
    }

    public static ControlHandshakeResponsePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_HANDSHAKE_RESPONSE;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int messageId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final ControlHandshakeResponsePacket helloPacket = new ControlHandshakeResponsePacket(payload.array());
        helloPacket.setRequestId(messageId);
        return helloPacket;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{requestId=").append(getRequestId());
        sb.append(", ");
        if (payload == null) {
            sb.append("payload=null");
        } else {
            sb.append("payloadLength=").append(payload.length);
        }
        sb.append('}');
        return sb.toString();
    }

}
