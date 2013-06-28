package com.nhn.pinpoint.common.io.rpc.message;

import com.nhn.pinpoint.common.io.rpc.packet.Packet;
import com.nhn.pinpoint.common.io.rpc.packet.PacketHeader;
import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.io.rpc.packet.SendPacket;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 *
 */
public class Decoder extends FrameDecoder {
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if (buffer.readableBytes() < 2) {
            return null;
        }
        buffer.markReaderIndex();
        short packetType = buffer.readShort();
        switch (packetType) {
            case PacketHeader.APPLICATION_SEND:
                return readSend(packetType, buffer);
            case PacketHeader.APPLICATION_REQUEST:
                return readRequest(packetType, buffer);
            default:
        }
        return null;
    }

    private Object readRequest(short packetType, ChannelBuffer buffer) {
        return RequestPacket.readBuffer(packetType, buffer);
    }

    private Object readSend(short packetType, ChannelBuffer buffer) {
        return SendPacket.readBuffer(packetType, buffer);
    }


}
