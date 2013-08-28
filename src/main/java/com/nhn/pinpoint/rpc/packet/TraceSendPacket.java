package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class TraceSendPacket extends BasicPacket {


    public TraceSendPacket() {
    }

    public TraceSendPacket(byte[] payload) {
        super(payload);
    }

    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_TRACE_SEND;
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(4 + 4);
        header.writeShort(PacketType.APPLICATION_TRACE_SEND);


        return PayloadPacket.appendPayload(header, payload);
    }

    public static Packet readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_TRACE_SEND;

        ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        return new TraceSendPacket(payload.array());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("TraceSendPacket");
        if (payload == null) {
            sb.append("{payload=null}");
        } else {
            sb.append("{payloadLength=").append(payload.length);
            sb.append('}');
        }

        return sb.toString();
    }

}
