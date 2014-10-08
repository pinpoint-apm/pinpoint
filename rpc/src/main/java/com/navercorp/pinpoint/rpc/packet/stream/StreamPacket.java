package com.nhn.pinpoint.rpc.packet.stream;

import com.nhn.pinpoint.rpc.packet.Packet;

/**
 * @author koo.taejin <kr14910>
 */
public interface StreamPacket extends Packet {

    int getStreamChannelId();

}
