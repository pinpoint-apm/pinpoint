package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * @author emeroad
 */
public class SendPacket extends BasicPacket {


    public SendPacket() {
    }

    public SendPacket(byte[] payload) {
        super(payload);
    }

    @Override
    public short getPacketType() {
        return PacketType.APPLICATION_SEND;
    }

    @Override
    public ChannelBuffer toBuffer() {
        ChannelBuffer header = ChannelBuffers.buffer(2 + 4);
        header.writeShort(PacketType.APPLICATION_SEND);


        return PayloadPacket.appendPayload(header, payload);
    }

    public static Packet readBuffer(short packetType, ChannelBuffer buffer) {
        assert packetType == PacketType.APPLICATION_SEND;

        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }

        ChannelBuffer payload = PayloadPacket.readPayload(buffer);
        if (payload == null) {
            return null;
        }
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
