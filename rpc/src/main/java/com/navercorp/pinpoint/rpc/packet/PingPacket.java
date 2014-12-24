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
public class PingPacket extends BasicPacket {

    public static final PingPacket PING_PACKET = new PingPacket();

    static {
        ChannelBuffer buffer = ChannelBuffers.buffer(2);
        buffer.writeShort(PacketType.CONTROL_PING);
        PING_BYTE = buffer.array();
    }

    private static final byte[] PING_BYTE;

    public PingPacket() {
    }

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_PING;
    }

    @Override
    public ChannelBuffer toBuffer() {
        return ChannelBuffers.wrappedBuffer(PING_BYTE);
    }

    public static PingPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_PING;

        return PING_PACKET;
    }

    @Override
    public String toString() {
        return "PingPacket";
    }

}
