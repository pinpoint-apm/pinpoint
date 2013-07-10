package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.*;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class EmptySeverMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handleSend(SendPacket sendPacket, Channel channel) {

    }

    @Override
    public void handleRequest(RequestPacket requestPacket, Channel channel) {
        logger.info("handlerRequest");
    }


    @Override
    public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
        logger.info("handlerStream");
    }


}
