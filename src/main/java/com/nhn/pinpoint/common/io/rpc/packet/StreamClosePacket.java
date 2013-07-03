package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class StreamClosePacket extends BasicPacket implements StreamPacket {
    private int channelId;

    public StreamClosePacket(int channelId) {
        this.channelId = channelId;
    }

    public StreamClosePacket(byte[] payload) {
        super(payload);
    }

    public StreamClosePacket(int channelId, byte[] payload) {
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
        header.writeShort(PacketType.APPLICATION_STREAM_CLOSE);
        header.writeInt(channelId);

        return PayloadPacket.appendPayload(header, payload);
    }


    public static StreamClosePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_STREAM_CLOSE;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int streamId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final StreamClosePacket streamClosePacket = new StreamClosePacket(payload.array());
        streamClosePacket.setChannelId(streamId);
        return streamClosePacket;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StreamClosePacket");
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
