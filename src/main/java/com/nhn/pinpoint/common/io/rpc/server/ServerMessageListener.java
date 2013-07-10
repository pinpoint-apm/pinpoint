package com.nhn.pinpoint.common.io.rpc.server;

import com.nhn.pinpoint.common.io.rpc.packet.*;
import org.jboss.netty.channel.Channel;

/**
 *
 */
public interface ServerMessageListener {
    void handleSend(SendPacket sendPacket, Channel channel);
    // 외부 노출 Channel은 별도의 Tcp Channel로 감싸는걸로 변경할 것.
    void handleRequest(RequestPacket requestPacket, Channel channel);

    void handleStream(StreamPacket requestPacket, ServerStreamChannel streamChannel);

}
