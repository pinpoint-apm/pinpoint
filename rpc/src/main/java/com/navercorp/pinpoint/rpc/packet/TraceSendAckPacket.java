package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */

public class TraceSendAckPacket implements Packet {
    private int traceId;

    public TraceSendAckPacket() {
    }

    public TraceSendAckPacket(int traceId) {
        this.traceId = traceId;
    }

    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_TRACE_SEND_ACK;
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketType.APPLICATION_TRACE_SEND_ACK);
        header.writeInt(traceId);

        return header;
    }

    public static TraceSendAckPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_TRACE_SEND_ACK;

        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }

        final int traceId = buffer.readInt();
        return new TraceSendAckPacket(traceId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("TraceSendAckPacket");
        sb.append("{traceId=").append(traceId);
        sb.append('}');
        return sb.toString();
    }

}