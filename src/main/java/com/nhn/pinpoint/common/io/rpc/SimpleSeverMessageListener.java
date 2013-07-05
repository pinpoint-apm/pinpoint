package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.*;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SimpleSeverMessageListener implements ServerMessageListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void handleSend(SendPacket sendPacket, Channel channel) {
        logger.debug("sendPacket:{} channel:{}", sendPacket, channel);
    }

    @Override
    public void handleRequest(RequestPacket requestPacket, Channel channel) {
        logger.debug("requestPacket:{} channel:{}", requestPacket, channel);
        ResponsePacket responsePacket = new ResponsePacket(requestPacket.getPayload(), requestPacket.getRequestId());
        channel.write(responsePacket);
    }


    @Override
    public void handleStream(StreamPacket streamPacket, Channel channel) {

    }


}
