package com.nhn.pinpoint.rpc;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * @author emeroad
 */
public class ChannelWriteFailListenableFuture<T> extends DefaultFuture<T> implements ChannelFutureListener {

    public ChannelWriteFailListenableFuture() {
        super(3000);
    }

    public ChannelWriteFailListenableFuture(long timeoutMillis) {
        super(timeoutMillis);
    }


    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            // io write fail
            this.setFailure(future.getCause());
        }
    }
}
