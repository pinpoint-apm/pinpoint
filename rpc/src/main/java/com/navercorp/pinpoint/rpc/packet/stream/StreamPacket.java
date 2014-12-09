package com.navercorp.pinpoint.rpc.packet.stream;

import com.navercorp.pinpoint.rpc.packet.Packet;

/**
 * @author koo.taejin <kr14910>
 */
public interface StreamPacket extends Packet {

    int getStreamChannelId();

}
