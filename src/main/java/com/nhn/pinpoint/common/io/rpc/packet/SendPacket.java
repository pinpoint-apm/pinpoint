package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class SendPacket extends AbstractPacket {


    public SendPacket() {
    }

    public SendPacket(byte[] payload) {
        super(payload);
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketHeader.APPLICATION_SEND);
        // 이건 payload 헤더이긴하다.
        header.writeInt(payload.length);

        ChannelBuffer payloadWrap = ChannelBuffers.wrappedBuffer(payload);

        return ChannelBuffers.wrappedBuffer(header, payloadWrap);
    }

    public static Packet readBuffer(short packetType, ChannelBuffer buffer) {
        ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        return new SendPacket(payload.array());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(64);
        sb.append("SendPacket");
        if (payload == null) {
            sb.append("{payload=null}");
        } else {
            sb.append("{payloadLength=").append(payload.length);
            sb.append('}');
        }

        return sb.toString();
    }

}
