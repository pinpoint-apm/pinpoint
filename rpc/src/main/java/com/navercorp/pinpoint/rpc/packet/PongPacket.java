package com.nhn.pinpoint.rpc.packet;

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
