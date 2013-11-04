package com.nhn.pinpoint.rpc.server;

import org.jboss.netty.channel.Channel;

/**
 * @author emeroad
 */
public class ChannelContext {

    private final ServerStreamChannelManager streamChannelManager;

    private final Channel channel;

    private final SocketChannel socketChannel;

    private volatile boolean closePacketReceived;

    public ChannelContext(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel must not be null");
        }
        this.channel = channel;
        this.socketChannel = new SocketChannel(channel);
        this.streamChannelManager = new ServerStreamChannelManager(channel);
    }


    public ServerStreamChannel getStreamChannel(int channelId) {
        return streamChannelManager.findStreamChannel(channelId);
    }

    public ServerStreamChannel createStreamChannel(int channelId) {
        return streamChannelManager.createStreamChannel(channelId);
    }


    public void closeAllStreamChannel() {
        streamChannelManager.closeInternal();
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public boolean isClosePacketReceived() {
        return closePacketReceived;
    }

    public void closePacketReceived() {
        this.closePacketReceived = true;
    }
}
