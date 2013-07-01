package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class ResponsePacket extends AbstractPacket{
    private int requestId;

    public ResponsePacket() {
    }

    public ResponsePacket(byte[] payload) {
        super(payload);
    }

    public ResponsePacket(byte[] payload, int requestId) {
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
        header.writeShort(PacketHeader.APPLICATION_RESPONSE);
        header.writeInt(requestId);
        // 이건 payload 헤더이긴하다.
        header.writeInt(payload.length);

        ChannelBuffer payloadWrap = ChannelBuffers.wrappedBuffer(payload);

        return ChannelBuffers.wrappedBuffer(header, payloadWrap);
    }


    public static ResponsePacket readBuffer(short packetType, ChannelBuffer buffer) {
        if (buffer.readableBytes() < 8) {
            buffer.resetReaderIndex();
            return null;
        }

        final int messageId = buffer.readInt();
        ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
        ResponsePacket responsePacket = new ResponsePacket(payload.array());
        responsePacket.setRequestId(messageId);

        return responsePacket;

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ResponsePacket");
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
