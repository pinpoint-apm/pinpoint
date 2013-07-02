package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.io.rpc.packet.ResponsePacket;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SocketRequestHandler extends SimpleChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RequestMap requestMap = new RequestMap();

    public SocketRequestHandler() {
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        final Object message = e.getMessage();
        if (message instanceof ResponsePacket) {
            final ResponsePacket responsePacket = (ResponsePacket) message;

            final int requestId = responsePacket.getRequestId();
            final MessageFuture messageFuture = requestMap.removeMessageFuture(requestId);
            if (messageFuture == null) {
                logger.warn("messageFuture not found:{}, channel:{}", responsePacket, e.getChannel());
                return;
            } else {
                logger.debug("responsePacket arrived packet:{}, channel:{}", responsePacket, e.getChannel());
            }

            ResponseMessage response = new ResponseMessage();
            response.setMessage(responsePacket.getPayload());
            messageFuture.setMessage(response);
            return;
        } else {
            if (message instanceof RequestPacket) {
                RequestPacket rp = (RequestPacket) message;
                // connector로 들어오는 request 메시지를 핸들링을 해야 함.
            }
            logger.error("unexpectedMessage received:{} address:{}", message, e.getRemoteAddress());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("UnexpectedError happened. event:{}", e, e.getCause());

    }


    public MessageFuture register(RequestPacket requestPacket, long timeoutMillis) {
        return  this.requestMap.registerRequest(requestPacket, timeoutMillis);
    }

    public void close() {
        this.requestMap.close();
    }
}

