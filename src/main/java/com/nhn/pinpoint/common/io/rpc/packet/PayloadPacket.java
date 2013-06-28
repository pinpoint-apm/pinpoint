package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class PayloadPacket {

    public static ChannelBuffer readPayload(ChannelBuffer buffer) {
        if (buffer.readableBytes() < 4) {
            buffer.resetReaderIndex();
            return null;
        }
        int payloadLength = buffer.readInt();
        if (payloadLength <= 0) {
            return ChannelBuffers.buffer(0);
        }
        if (buffer.readableBytes() < payloadLength) {
            buffer.resetReaderIndex();
            return null;
        }
        return buffer.readBytes(payloadLength);
    }

}
