package com.nhn.pinpoint.rpc.packet;

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
