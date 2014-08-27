package com.nhn.pinpoint.rpc.packet;

/**
 *  type marker
 */
public interface StreamPacket extends Packet {
    int getChannelId();

}
