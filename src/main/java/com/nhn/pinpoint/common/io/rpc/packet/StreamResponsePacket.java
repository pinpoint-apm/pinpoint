package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class StreamResponsePacket extends BasicPacket implements StreamPacket {
    private int channelId;

    public StreamResponsePacket(int channelId) {
        this.channelId = channelId;
    }

    public StreamResponsePacket(byte[] payload) {
        super(payload);
    }

    public StreamResponsePacket(int channelId, byte[] payload) {
        super(payload);
        this.channelId = channelId;
    }
    @Override
    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        header.writeShort(PacketType.APPLICATION_STREAM_RESPONSE);
        header.writeInt(channelId);

        return PayloadPacket.appendPayload(header, payload);
    }


    public static StreamResponsePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_STREAM_RESPONSE;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int channelId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final StreamResponsePacket streamResponsePacket = new StreamResponsePacket(payload.array());
        streamResponsePacket.setChannelId(channelId);
        return streamResponsePacket;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StreamResponsePacket");
        sb.append("{channelId=").append(channelId);
        sb.append(", ");
        if (payload == null) {
            sb.append("payload=null");
        } else {
            sb.append("payloadLength=").append(payload.length);
        }
        sb.append('}');
        return sb.toString();
    }
}
