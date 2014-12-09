package com.navercorp.pinpoint.rpc.server;

import java.util.Map;

import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;

/**
 * @author emeroad
 */
public interface ServerMessageListener {

    void handleSend(SendPacket sendPacket, SocketChannel channel);
    // 외부 노출 Channel은 별도의 Tcp Channel로 감싸는걸로 변경할 것.
    void handleRequest(RequestPacket requestPacket, SocketChannel channel);

    HandshakeResponseCode handleHandshake(Map properties);

}
