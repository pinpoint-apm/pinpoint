package com.nhn.pinpoint.common.rpc.server;

import com.nhn.pinpoint.common.rpc.packet.StreamPacket;
import com.nhn.pinpoint.common.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.rpc.packet.SendPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SimpleLoggingSeverMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final SimpleLoggingSeverMessageListener LISTENER = new SimpleLoggingSeverMessageListener();

    @Override
    public void handleSend(SendPacket sendPacket, SocketChannel channel) {
        logger.info("handlerSend {}", sendPacket);

    }

    @Override
    public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
        logger.info("handlerRequest {}", requestPacket);
    }


    @Override
    public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
        logger.info("handlerStream {}", streamChannel);
    }


}
