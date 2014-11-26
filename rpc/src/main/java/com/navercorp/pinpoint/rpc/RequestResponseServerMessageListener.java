package com.nhn.pinpoint.rpc;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.packet.HandshakeResponseCode;
import com.nhn.pinpoint.rpc.packet.HandshakeResponseType;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.SocketChannel;

/**
 * @author emeroad
 */
public class RequestResponseServerMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final RequestResponseServerMessageListener LISTENER = new RequestResponseServerMessageListener();

    @Override
    public void handleSend(SendPacket sendPacket, SocketChannel channel) {
        logger.info("handlerSend {} {}", sendPacket, channel);

    }

    @Override
    public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
        logger.info("handlerRequest {}", requestPacket, channel);
        channel.sendResponseMessage(requestPacket, requestPacket.getPayload());
    }

	@Override
	public HandshakeResponseCode handleHandshake(Map properties) {
        logger.info("handle handShake {}", properties);
        return HandshakeResponseType.Success.DUPLEX_COMMUNICATION;
	}

}
