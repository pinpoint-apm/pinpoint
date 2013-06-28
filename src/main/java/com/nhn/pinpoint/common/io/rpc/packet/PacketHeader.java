package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 *
 */
public class PacketHeader {
    public static final short APPLICATION_SEND = 1;

    public static final short APPLICATION_REQUEST = 5;
    public static final short APPLICATION_RESPONSE = 6;

    public static final short APPLICATION_CREATE_STREAM = 10;
    public static final short APPLICATION_CLOSED_STREAM = 11;
    public static final short APPLICATION_STREAM_RESPONSE = 15;



    private short type;
    private int nextReadSize;

    public PacketHeader(short type, int nextReadSize) {
        this.type = type;
        this.nextReadSize = nextReadSize;
    }

    public ChannelBuffer toBuffer() {
        // 자동으로 복사하는 방법을 쓰자.
        ChannelBuffer buffer = ChannelBuffers.buffer(2);
        buffer.writeShort(type);
        return buffer;
    }

    public int getNextReadPacketSize() {
        return nextReadSize;
    }
}
