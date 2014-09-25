package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */
public class StreamCreatePacket extends BasicStreamPacket {

    public StreamCreatePacket(int channelId) {
        super(channelId);
    }

    public StreamCreatePacket(byte[] payload) {
        super(payload);
    }

    public StreamCreatePacket(int channelId, byte[] payload) {
        super(channelId, payload);
    }


    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_STREAM_CREATE;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        header.writeShort(PacketType.APPLICATION_STREAM_CREATE);
        header.writeInt(channelId);

        return PayloadPacket.appendPayload(header, payload);
    }


    public static StreamCreatePacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_STREAM_CREATE;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int channelId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final StreamCreatePacket streamCreatePacket = new StreamCreatePacket(payload.array());
        streamCreatePacket.setChannelId(channelId);
        return streamCreatePacket;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StreamCreatePacket");
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
