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
public class PingPayloadPacket extends BasicPacket {

    private final int pingId;

    private final byte stateCodeVersion;
    private final byte stateCode;

    public PingPayloadPacket(int pingId, byte stateCode) {
        this(pingId, (byte)0, stateCode);
    }

    public PingPayloadPacket(int pingId, byte stateCodeVersion, byte stateCode) {
        this.pingId = pingId;

        this.stateCodeVersion = stateCodeVersion;
        this.stateCode = stateCode;
    }

    public static PingPayloadPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_PING_PAYLOAD;

        if (buffer.readableBytes() < 6) {
            buffer.resetReaderIndex();
            return null;
        }

        int pingId = buffer.readInt();
        byte stateVersion = buffer.readByte();
        byte stateCode = buffer.readByte();

        return new PingPayloadPacket(pingId, stateVersion, stateCode);
    }

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_PING_PAYLOAD;
    }

    @Override
    public ChannelBuffer toBuffer() {
        // 2 + 4 + 1 + 1
        ChannelBuffer buffer = ChannelBuffers.buffer(8);
        buffer.writeShort(PacketType.CONTROL_PING_PAYLOAD);
        buffer.writeInt(pingId);
        buffer.writeByte(stateCodeVersion);
        buffer.writeByte(stateCode);
        return buffer;
    }

    public int getPingId() {
        return pingId;
    }

    public byte getStateCodeVersion() {
        return stateCodeVersion;
    }

    public byte getStateCode() {
        return stateCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PingPayloadPacket{");
        sb.append("pingId=").append(pingId);
        sb.append(", stateCodeVersion=").append(stateCodeVersion);
        sb.append(", stateCode=").append(stateCode);
        sb.append('}');
        return sb.toString();
    }

}
