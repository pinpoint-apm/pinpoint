package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */
public class ClientClosePacket extends BasicPacket {

    @Override
    public short getPacketType() {
        return PacketType.CONTROL_CLIENT_CLOSE;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketType.CONTROL_CLIENT_CLOSE);

        return PayloadPacket.appendPayload(header, payload);
    }

    public static ClientClosePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.CONTROL_CLIENT_CLOSE;

        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }

        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final ClientClosePacket requestPacket = new ClientClosePacket();
        return requestPacket;

    }

    @Override
    public String toString() {
        return "ClientClosePacket";
    }
}
