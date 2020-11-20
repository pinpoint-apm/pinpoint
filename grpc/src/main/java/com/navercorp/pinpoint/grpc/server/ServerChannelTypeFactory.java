package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.ChannelTypeEnum;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.Executor;

public class ServerChannelTypeFactory {

    public ServerChannelType newChannelType(ChannelTypeEnum type) {
        Assert.requireNonNull(type, "channelTypeEnum");

        switch (type) {
            case NIO:
                return new NioServerChannelType();
            case EPOLL:
                return newEpollChannelType();
            case AUTO:
            default:
                return newEpollChannelType();
        }
    }

    private ServerChannelType newEpollChannelType() {
        if (Epoll.isAvailable()) {
            return new EpollServerChannelType();
        }
        return new NioServerChannelType();
    }

    public static class EpollServerChannelType implements ServerChannelType {
        @Override
        public Class<? extends ServerChannel> getChannelType() {
            return EpollServerSocketChannel.class;
        }

        @Override
        public EventLoopGroup newEventLoopGroup(int nThreads, Executor executor) {
            return new EpollEventLoopGroup(nThreads, executor);
        }
    }

    public static class NioServerChannelType implements ServerChannelType {
        @Override
        public Class<? extends ServerChannel> getChannelType() {
            return NioServerSocketChannel.class;
        }

        @Override
        public EventLoopGroup newEventLoopGroup(int nThreads, Executor executor) {
            return new NioEventLoopGroup(nThreads, executor);
        }
    }
}
