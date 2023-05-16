package com.navercorp.pinpoint.grpc.client;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.Executor;

public interface ChannelType {
    Class<? extends Channel> getChannelType();

    EventLoopGroup newEventLoopGroup(int nThreads, Executor executor);
}
