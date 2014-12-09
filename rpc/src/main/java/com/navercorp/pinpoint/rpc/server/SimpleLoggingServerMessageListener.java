package com.navercorp.pinpoint.rpc.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.packet.HandshakeResponseCode;
import com.navercorp.pinpoint.rpc.packet.HandshakeResponseType;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.SendPacket;

/**
 * @author emeroad
 */
public class SimpleLoggingServerMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final SimpleLoggingServerMessageListener LISTENER = new SimpleLoggingServerMessageListener();

    @Override
    public void handleSend(SendPacket sendPacket, SocketChannel channel) {
        logger.info("handlerSend {} {}", sendPacket, channel);
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
        logger.info("handlerRequest {} {}", requestPacket, channel);
    }

    @Override
    public HandshakeResponseCode handleHandshake(Map properties) {
        logger.info("handleEnableWorker {}", properties);
        return HandshakeResponseType.Success.SUCCESS;
    }

}
