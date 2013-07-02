package com.nhn.pinpoint.common.io.rpc;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

/**
 *
 */
public interface MessageFutureListener {
    void onComplete(MessageFuture messageFuture);
}
