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

package com.navercorp.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author Taejin Koo
 */
public class PingSimplePacket extends BasicPacket {

    public static final PingSimplePacket PING_PACKET = new PingSimplePacket();
    private static final byte[] PING_BYTE;

    static {
        ChannelBuffer buffer = ChannelBuffers.buffer(2);
        buffer.writeShort(PacketType.CONTROL_PING_SIMPLE);
        PING_BYTE = buffer.array();
    }

    public PingSimplePacket() {
    }

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_PING_SIMPLE;
    }

    @Override
    public ChannelBuffer toBuffer() {
        return ChannelBuffers.wrappedBuffer(PING_BYTE);
    }

    public static PingSimplePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_PING_SIMPLE;
        return PING_PACKET;
    }

    @Override
    public String toString() {
        return "PingSimplePacket";
    }

}
