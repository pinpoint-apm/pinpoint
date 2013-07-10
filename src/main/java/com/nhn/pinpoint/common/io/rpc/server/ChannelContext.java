package com.nhn.pinpoint.common.io.rpc.server;

import org.jboss.netty.channel.Channel;

/**
 *
 */
public class ChannelContext {

    private final ServerStreamChannelManager clientStreamChannelManager = new ServerStreamChannelManager();


    public ServerStreamChannel getStreamChannel(int channelId) {
        return clientStreamChannelManager.findStreamChannel(channelId);
    }

    public ServerStreamChannel createChannel(int channelId, Channel channel) {
        return clientStreamChannelManager.createStreamChannel(channelId, channel);
    }
}
