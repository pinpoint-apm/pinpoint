package com.nhn.pinpoint.rpc;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * @author emeroad
 */
public class ChannelWriteCompleteListenableFuture<T> extends DefaultFuture<T> implements ChannelFutureListener {

    private T result;

    public ChannelWriteCompleteListenableFuture() {
        this(null, 3000);
    }

    public ChannelWriteCompleteListenableFuture(T result) {
        this(result, 3000);
    }
    public ChannelWriteCompleteListenableFuture(T result, long timeoutMillis) {
        super(timeoutMillis);
        this.result = result;
    }

    public ChannelWriteCompleteListenableFuture(long timeoutMillis) {
        super(timeoutMillis);
    }


    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            this.setResult(result);
        } else {
            this.setFailure(future.getCause());
        }
    }
}
