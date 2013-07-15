package com.nhn.pinpoint.common.rpc.codec;

import com.nhn.pinpoint.common.rpc.packet.*;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class PacketDecoder extends FrameDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
        if (buffer.readableBytes() < 2) {
            return null;
        }
        buffer.markReaderIndex();
        final short packetType = buffer.readShort();
        switch (packetType) {
            case PacketType.APPLICATION_SEND:
                return readSend(packetType, buffer);
            case PacketType.APPLICATION_REQUEST:
                return readRequest(packetType, buffer);
            case PacketType.APPLICATION_RESPONSE:
                return readResponse(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE:
                return readStreamCreate(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CLOSE:
                return readStreamClose(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                return readStreamCreateSuccess(packetType, buffer);
            case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                return readStreamCreateFail(packetType, buffer);
            case PacketType.APPLICATION_STREAM_RESPONSE:
                return readStreamResponse(packetType, buffer);

            case PacketType.CONTROL_CLOSE:
                return readControlClose(packetType, buffer);
        }
        logger.error("invalid packetType received. packetType:{}, channel:{}", packetType, channel);
        channel.close();
        return null;
    }

    private Object readControlClose(short packetType, ChannelBuffer buffer) {
        return ClosePacket.readBuffer(packetType, buffer);
    }


    private Object readSend(short packetType, ChannelBuffer buffer) {
        return SendPacket.readBuffer(packetType, buffer);
    }


    private Object readRequest(short packetType, ChannelBuffer buffer) {
        return RequestPacket.readBuffer(packetType, buffer);
    }

    private Object readResponse(short packetType, ChannelBuffer buffer) {
        return ResponsePacket.readBuffer(packetType, buffer);
    }



    private Object readStreamCreate(short packetType, ChannelBuffer buffer) {
        return StreamCreatePacket.readBuffer(packetType, buffer);
    }


    private Object readStreamCreateSuccess(short packetType, ChannelBuffer buffer) {
        return StreamCreateSuccessPacket.readBuffer(packetType, buffer);
    }

    private Object readStreamCreateFail(short packetType, ChannelBuffer buffer) {
        return StreamCreateFailPacket.readBuffer(packetType, buffer);
    }

    private Object readStreamResponse(short packetType, ChannelBuffer buffer) {
        return StreamResponsePacket.readBuffer(packetType, buffer);
    }

    private Object readStreamClose(short packetType, ChannelBuffer buffer) {
        return StreamClosePacket.readBuffer(packetType, buffer);
    }



}
