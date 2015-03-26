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

import com.navercorp.pinpoint.rpc.common.SocketStateCode;

/**
 * @author emeroad
 * @author Taejin Koo
 */
public class PingPacket extends BasicPacket {

    public static final PingPacket PING_PACKET = new PingPacket();
    
    // optional
    private final int socketId;
    
    private final byte stateVersion;
    private final byte stateCode;
    
    static {
        ChannelBuffer buffer = ChannelBuffers.buffer(2);
        buffer.writeShort(PacketType.CONTROL_PING);
        PING_BYTE = buffer.array();
    }

    private static final byte[] PING_BYTE;

    public PingPacket() {
        this(-1);
    }

    public PingPacket(int socketId) {
        this(socketId, (byte)-1, (byte)-1);
    }

    public PingPacket(int socketId, byte stateVersion, byte stateCode) {
        this.socketId = socketId;
        
        this.stateVersion = stateVersion;
        this.stateCode = stateCode;
    }
    
    @Override
    public short getPacketType() {
        return PacketType.CONTROL_PING;
    }

    @Override
    public ChannelBuffer toBuffer() {
        if (socketId == -1) {
            return ChannelBuffers.wrappedBuffer(PING_BYTE);
        } else {
            // 2 + 4 + 1 + 1
            ChannelBuffer buffer = ChannelBuffers.buffer(8);
            buffer.writeShort(PacketType.CONTROL_PING);
            buffer.writeInt(socketId);
            buffer.writeByte(stateVersion);
            buffer.writeByte(stateCode);
            return buffer;
        } 
    }

    public static PingPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_PING;

        if (buffer.readableBytes() == 6) {
            int socketId = buffer.readInt();
            byte stateVersion = buffer.readByte();
            byte stateCode = buffer.readByte();
            
            return new PingPacket(socketId, stateVersion, stateCode);
        } else {
            return PING_PACKET;
        }
    }

    public int getSocketId() {
        return socketId;
    }

    public byte getStateVersion() {
        return stateVersion;
    }

    public byte getStateCode() {
        return stateCode;
    }

    @Override
    public String toString() {
        if (socketId == -1) {
            return "PingPacket";
        }
        
        StringBuilder sb = new StringBuilder(32);
        sb.append("PingPacket");
        
        if (socketId != -1) {
            sb.append("{socketId:");
            sb.append(socketId);
            sb.append("(");
            sb.append(SocketStateCode.getStateCode(stateCode));
            sb.append(")");
            sb.append("}");
        }
        
        return sb.toString();
    }

}
