package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.profiler.logging.Logger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

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