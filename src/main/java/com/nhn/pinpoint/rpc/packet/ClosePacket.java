package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class ClosePacket extends BasicPacket {

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_CLOSE;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketType.CONTROL_CLOSE);

        return PayloadPacket.appendPayload(header, payload);
    }

    public static ClosePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_CLOSE;

        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }

        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final ClosePacket requestPacket = new ClosePacket();
        return requestPacket;

    }

    @Override
    public String toString() {
        return "ClosePacket";
    }
}
