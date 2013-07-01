package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.io.rpc.packet.SendPacket;
import org.jboss.netty.channel.Channel;

/**
 *
 */
public interface ServerMessageListener {
    void handleSend(SendPacket sendPacket, Channel channel);

    void handleRequest(RequestPacket requestPacket, Channel channel);

    void handleStreamCreate();
    void handleStreamMessage();
    void handleStreamClosed();

}
