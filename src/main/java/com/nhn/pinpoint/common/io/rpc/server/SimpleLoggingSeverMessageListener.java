package com.nhn.pinpoint.common.io.rpc.server;

import com.nhn.pinpoint.common.io.rpc.packet.*;
import com.nhn.pinpoint.common.io.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.common.io.rpc.server.ServerStreamChannel;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SimpleLoggingSeverMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handleSend(SendPacket sendPacket, SocketChannel channel) {
        logger.info("handlerSend");

    }

    @Override
    public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
        logger.info("handlerRequest");
    }


    @Override
    public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
        logger.info("handlerStream");
    }


}
