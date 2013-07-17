package com.nhn.pinpoint.common.rpc.client;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

/**
 *
 */
public class Reconnect implements ChannelFutureListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private PinpointSocket socket;
    private SocketAddress socketAddress;

    public Reconnect(PinpointSocket socket, SocketAddress socketAddress) {
        this.socket = socket;
        this.socketAddress = socketAddress;
    }

    public SocketAddress getSocketAddress() {
        return socketAddress;
    }

    public PinpointSocket getSocket() {
        return socket;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            Channel newChannel = future.getChannel();
            ChannelContext context = (ChannelContext) newChannel.getAttachment();
            SocketHandler socketHandler = context.getSocketHandler();
            socket.replaceSocketHandler(socketHandler);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("reconnect fail. sleep(3000) channel:{} address:{}", new Object[]{future.getChannel(), socketAddress});
            }
//            Thread.sleep(3000);
            // 재귀 호출하면 stack depth가 너무 깊어져서 많이 반복하면 스택오버플로가 발생할수있어서 스택을 풀고 호출한다.
            Exception exception = new Exception();
            logger.info("thread.", exception);
//            socketFactory.reconnect(new Reconnect(socket, socketAddress));
        }
    }
}
