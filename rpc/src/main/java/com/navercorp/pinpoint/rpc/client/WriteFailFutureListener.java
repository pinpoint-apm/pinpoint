package com.nhn.pinpoint.rpc.client;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;

/**
 * @author emeroad
 */
public class WriteFailFutureListener implements ChannelFutureListener {

    private final Logger logger;
    private final String failMessage;
    private final String successMessage;

    public WriteFailFutureListener(Logger logger, String failMessage, String successMessage) {
        if (logger == null) {
            throw new NullPointerException("logger must not be null");
        }
        this.logger = logger;
        this.failMessage = failMessage;
        this.successMessage = successMessage;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            if (logger.isWarnEnabled()) {
                final Throwable cause = future.getCause();
                logger.warn("{} channel:{} Caused:{}", failMessage, future.getChannel(), cause.getMessage(), cause);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("{} channel:{}", successMessage, future.getChannel());
            }
        }
    }
}
