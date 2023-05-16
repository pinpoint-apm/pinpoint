package com.navercorp.pinpoint.rpc.util;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import java.util.concurrent.CompletableFuture;

public final class Futures {
    private Futures() {
    }

    public static <T> ChannelFutureListener writeFailListener(CompletableFuture<T> completableFuture) {
        return new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    completableFuture.completeExceptionally(future.getCause());
                }
            }
        };
    }


    public static CompletableFuture<Void> ioWriteFuture(ChannelFuture future) {

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (channelFuture.isSuccess()) {
                    completableFuture.complete(null);
                } else {
                    completableFuture.completeExceptionally(channelFuture.getCause());
                }
            }
        });
        return completableFuture;
    }
}
