package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */
public class TraceSendPacket extends SendPacket {
    private int traceId;

    public TraceSendPacket() {
    }

    public TraceSendPacket(byte[] payload) {
        super(payload);
    }
    public TraceSendPacket(int traceId, byte[] payload) {
        super(payload);
        this.traceId = traceId;
    }

    public int getTraceId() {
        return traceId;
    }

    public void setTraceId(int traceId) {
        this.traceId = traceId;
    }

    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_TRACE_SEND;
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        header.writeShort(PacketType.APPLICATION_TRACE_SEND);
        header.writeInt(traceId);

        return PayloadPacket.appendPayload(header, payload);
    }

    public static Packet readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_TRACE_SEND;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int traceId = buffer.readInt();
        ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        return new TraceSendPacket(traceId, payload.array());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("TraceSendPacket");
        sb.append("{traceId=").append(traceId);
        sb.append(", ");
        if (payload == null) {
            sb.append("payload=null}");
        } else {
            sb.append("payloadLength=").append(payload.length);
            sb.append('}');
        }

        return sb.toString();
    }

}
