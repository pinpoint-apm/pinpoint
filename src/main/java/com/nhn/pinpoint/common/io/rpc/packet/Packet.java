package com.nhn.pinpoint.common.io.rpc.packet;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 *
 */
public interface Packet {

   ChannelBuffer toBuffer();
}
