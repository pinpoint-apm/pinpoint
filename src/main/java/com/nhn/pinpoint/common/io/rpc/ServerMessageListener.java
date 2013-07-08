package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.*;
import org.jboss.netty.channel.Channel;

/**
 *
 */
public interface ServerMessageListener {
    void handleSend(SendPacket sendPacket, Channel channel);

    void handleRequest(RequestPacket requestPacket, Channel channel);

    void handleStream(StreamPacket requestPacket, Channel channel);

}
