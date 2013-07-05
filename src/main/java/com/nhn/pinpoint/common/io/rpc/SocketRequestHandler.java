package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.io.rpc.packet.ResponsePacket;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SocketRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestProcessor requestProcessor = new RequestProcessor();

    public SocketRequestHandler() {
    }

    public void messageReceived(ResponsePacket responsePacket, Channel channel) {
        final int requestId = responsePacket.getRequestId();
        final DefaultFuture<ResponseMessage> future = requestProcessor.removeMessageFuture(requestId);
        if (future == null) {
            logger.warn("future not found:{}, channel:{}", responsePacket, channel);
            return;
        } else {
            logger.debug("responsePacket arrived packet:{}, channel:{}", responsePacket, channel);
        }

        ResponseMessage response = new ResponseMessage();
        response.setMessage(responsePacket.getPayload());
        future.setObject(response);
    }

    public void messageReceived(RequestPacket requestPacket, Channel channel) {
        logger.error("unexpectedMessage received:{} address:{}", requestPacket, channel.getRemoteAddress());
    }



    public DefaultFuture<ResponseMessage> register(RequestPacket requestPacket, long timeoutMillis) {
        return  this.requestProcessor.registerRequest(requestPacket, timeoutMillis);
    }

    public void close() {
        this.requestProcessor.close();
    }
}

