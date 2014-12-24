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
public class PongPacket extends BasicPacket {

    public static final PongPacket PONG_PACKET = new PongPacket();

    static {
        ChannelBuffer buffer = ChannelBuffers.buffer(2);
        buffer.writeShort(PacketType.CONTROL_PONG);
        PONG_BYTE = buffer.array();
    }

    private static final byte[] PONG_BYTE;

    public PongPacket() {
    }

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_PONG;
    }

    @Override
    public ChannelBuffer toBuffer() {
        return ChannelBuffers.wrappedBuffer(PONG_BYTE);
    }

    public static PongPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_PONG;


        return PONG_PACKET;
    }

    @Override
    public String toString() {
        return "PongPacket";
    }

}
