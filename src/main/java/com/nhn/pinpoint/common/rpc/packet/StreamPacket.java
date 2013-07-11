package com.nhn.pinpoint.common.rpc.packet;

/**
 *  type marker
 */
public interface StreamPacket extends Packet {
    int getChannelId();

}
