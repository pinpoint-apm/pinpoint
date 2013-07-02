package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class RequestPacket extends AbstractPacket {

    private int requestId;

    public RequestPacket() {
    }

    public RequestPacket(byte[] payload) {
        super(payload);
    }

    public RequestPacket(int requestId, byte[] payload) {
        super(payload);
        this.requestId = requestId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public ChannelBuffer toBuffer() {

        ChannelBuffer header = ChannelBuffers.buffer(2 + 4 + 4);
        header.writeShort(PacketHeader.APPLICATION_REQUEST);
        header.writeInt(requestId);

        // 이건 payload 헤더이긴하다.
        header.writeInt(payload.length);
        ChannelBuffer payloadWrap = ChannelBuffers.wrappedBuffer(payload);

        return ChannelBuffers.wrappedBuffer(header, payloadWrap);
    }


    public static RequestPacket readBuffer(short packetType, ChannelBuffer buffer) {
        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int messageId = buffer.readInt();
        final ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        final RequestPacket requestPacket = new RequestPacket(payload.array());
        requestPacket.setRequestId(messageId);
        return requestPacket;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RequestPacket");
        sb.append("{requestId=").append(requestId);
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
