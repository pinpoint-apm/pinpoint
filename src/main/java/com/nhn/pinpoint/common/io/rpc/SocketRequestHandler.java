package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.message.ResponseMessage;
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
            final MessageFuture messageFuture = requestMap.findMessageFuture(requestId);
            if (messageFuture == null) {
                logger.warn("messageFuture not found:{}, channel:{}", responsePacket, e.getChannel());
                return;
            } else {
                logger.debug("responsePacket arrived packet:{}, channel:{}", responsePacket, e.getChannel());
            }

            ResponseMessage response = new ResponseMessage();
            response.setMessage(responsePacket.getPayload());
            messageFuture.readyMessage(response);
            return;
        } else {
            if (message instanceof RequestPacket) {
                RequestPacket rp = (RequestPacket) message;
            }
            logger.error("unexpectedMessage received:{} address:{}", message, e.getRemoteAddress());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("UnexpectedError happened. event:{}", e, e.getCause());

    }


    public MessageFuture register(RequestPacket requestPacket) {
        MessageFuture messageFuture = this.requestMap.registerRequest(requestPacket);
        messageFuture.markTime();
        return messageFuture;
    }

    public void close() {


    }
}

