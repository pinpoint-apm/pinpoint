package com.navercorp.pinpoint.grpc.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

import java.util.concurrent.Executor;

public interface ServerChannelType {
    Class<? extends ServerChannel> getChannelType();

    EventLoopGroup newEventLoopGroup(int nThreads, Executor executor);
}
