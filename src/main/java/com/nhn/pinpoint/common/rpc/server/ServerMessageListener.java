package com.nhn.pinpoint.common.rpc.server;

import com.nhn.pinpoint.common.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.rpc.packet.SendPacket;
import com.nhn.pinpoint.common.rpc.packet.StreamPacket;

/**
 *
 */
public interface ServerMessageListener {

    void handleSend(SendPacket sendPacket, SocketChannel channel);
    // 외부 노출 Channel은 별도의 Tcp Channel로 감싸는걸로 변경할 것.
    void handleRequest(RequestPacket requestPacket, SocketChannel channel);

    void handleStream(StreamPacket requestPacket, ServerStreamChannel streamChannel);

}
