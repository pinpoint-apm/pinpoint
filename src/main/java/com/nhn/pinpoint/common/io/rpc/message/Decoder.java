package com.nhn.pinpoint.common.io.rpc.message;

import com.nhn.pinpoint.common.io.rpc.packet.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Decoder extends FrameDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if (buffer.readableBytes() < 2) {
            return null;
        }
        buffer.markReaderIndex();
        final short packetType = buffer.readShort();
        switch (packetType) {
            case PacketHeader.APPLICATION_SEND:
                return readSend(packetType, buffer);
            case PacketHeader.APPLICATION_REQUEST:
                return readRequest(packetType, buffer);
            case PacketHeader.APPLICATION_RESPONSE:
                return readResponse(packetType, buffer);
        }
        logger.error("invalid packetType received. packetType:{}, connection:{}", packetType, channel.getRemoteAddress());

        return null;
    }

    private Object readRequest(short packetType, ChannelBuffer buffer) {
        return RequestPacket.readBuffer(packetType, buffer);
    }

    private Object readResponse(short packetType, ChannelBuffer buffer) {
        return ResponsePacket.readBuffer(packetType, buffer);
    }

    private Object readSend(short packetType, ChannelBuffer buffer) {
        return SendPacket.readBuffer(packetType, buffer);
    }


}
