package com.nhn.pinpoint.rpc.client;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;

/**
 *
 */
public class WriteFailFutureListener implements ChannelFutureListener {

    private final Logger logger;
    private final String message;

    public WriteFailFutureListener(Logger logger, String message) {
        this.logger = logger;
        this.message = message;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            logger.warn("{} channel:{}", message, future.getChannel());
        }
    }
}
