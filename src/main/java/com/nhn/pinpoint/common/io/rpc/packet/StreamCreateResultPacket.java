package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class StreamCreateResultPacket extends BasicPacket implements StreamPacket {
    private boolean result;
    private int channelId;

    public StreamCreateResultPacket(int channelId) {
        this.channelId = channelId;
    }

    public StreamCreateResultPacket(byte[] payload) {
        super(payload);
    }

    public StreamCreateResultPacket(int channelId, byte[] payload) {
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

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        if(result) {
            header.writeShort(PacketType.APPLICATION_STREAM_CREATE_SUCCESS);
        } else {
            header.writeShort(PacketType.APPLICATION_STREAM_CREATE_FAIL);
        }
        header.writeInt(channelId);

        return PayloadPacket.appendPayload(header, payload);
    }


    public static StreamCreateResultPacket readBuffer(short packetType, ChannelBuffer buffer, boolean success) {
        assert packetType == PacketType.APPLICATION_STREAM_CREATE_SUCCESS || packetType == PacketType.APPLICATION_STREAM_CREATE_FAIL;

        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int streamId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final StreamCreateResultPacket streamCreatePacket = new StreamCreateResultPacket(payload.array());
        streamCreatePacket.setChannelId(streamId);
        streamCreatePacket.setResult(success);
        return streamCreatePacket;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StreamCreateResultPacket");
        sb.append("{channelId=").append(channelId);
        sb.append(", ");
        sb.append("result=").append(result);
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
