package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */
public class StreamCreateFailPacket extends BasicStreamPacket {

    private int channelId;

    public StreamCreateFailPacket(int channelId) {
        super(channelId);
    }

    public StreamCreateFailPacket(byte[] payload) {
        super(payload);
    }

    public StreamCreateFailPacket(int channelId, byte[] payload) {
        super(channelId, payload);
    }

    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_STREAM_CREATE_FAIL;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);

        header.writeShort(PacketType.APPLICATION_STREAM_CREATE_FAIL);
        header.writeInt(channelId);

        return PayloadPacket.appendPayload(header, payload);
    }

    public static StreamCreateFailPacket readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_STREAM_CREATE_FAIL;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int streamId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final StreamCreateFailPacket streamCreatePacket = new StreamCreateFailPacket(payload.array());
        streamCreatePacket.setChannelId(streamId);
        return streamCreatePacket;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StreamCreateFailPacket");
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
