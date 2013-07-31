package com.nhn.pinpoint.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 */
public interface Packet {

    short getPacketType();

    byte[] getPayload();

   ChannelBuffer toBuffer();
}
