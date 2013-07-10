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
public class TestSeverMessageListener implements ServerMessageListener {

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
    public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
        logger.debug("streamPacket:{} channel:{}", streamPacket, streamChannel);
        if (streamPacket instanceof StreamCreatePacket) {
            streamChannel.sendOpenResult(true, streamPacket.getPayload());
            streamChannel.sendStreamMessage(new byte[1]);
            streamChannel.sendStreamMessage(new byte[2]);
            streamChannel.sendStreamMessage(new byte[3]);

        }  else if(streamPacket instanceof StreamClosePacket) {
            // 채널 종료해야 함.
        }

    }


}
