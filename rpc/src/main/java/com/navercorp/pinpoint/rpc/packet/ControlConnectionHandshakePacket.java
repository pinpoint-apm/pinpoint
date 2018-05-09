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

package com.navercorp.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author Taejin Koo
 */
public class ControlConnectionHandshakePacket extends BasicPacket {

    public ControlConnectionHandshakePacket(byte[] payload) {
        super(payload);
    }

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_CONNECTION_HANDSHAKE;
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketType.CONTROL_CONNECTION_HANDSHAKE);
        return PayloadPacket.appendPayload(header, payload);
    }

    public static ControlConnectionHandshakePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_CONNECTION_HANDSHAKE;

        if (buffer.readableBytes() < 6) {
            buffer.resetReaderIndex();
            return null;
        }

        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }

        final ControlConnectionHandshakePacket packet = new ControlConnectionHandshakePacket(payload.array());
        return packet;
    }

}
