package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.ChannelTypeEnum;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.Executor;

public class ChannelTypeFactory {

    public ChannelType newChannelType(ChannelTypeEnum type) {
        Assert.requireNonNull(type, "channelTypeEnum");

        switch (type) {
            case NIO:
                return new NioChannelType();
            case EPOLL:
                return newEpollChannelType();
            case AUTO:
            default:
                return newEpollChannelType();
        }
    }

    private ChannelType newEpollChannelType() {
        if (Epoll.isAvailable()) {
            return new EpollChannelType();
        }
        return new NioChannelType();
    }

    public static class EpollChannelType implements ChannelType {
        @Override
        public Class<? extends Channel> getChannelType() {
            return EpollSocketChannel.class;
        }

        @Override
        public EventLoopGroup newEventLoopGroup(int nThreads, Executor executor) {
            return new EpollEventLoopGroup(nThreads, executor);
        }
    }


    public static class NioChannelType implements ChannelType {
        @Override
        public Class<? extends Channel> getChannelType() {
            return NioSocketChannel.class;
        }

        @Override
        public EventLoopGroup newEventLoopGroup(int nThreads, Executor executor) {
            return new NioEventLoopGroup(nThreads, executor);
        }
    }
}
