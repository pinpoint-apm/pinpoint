package com.nhn.pinpoint.rpc.server;

import com.nhn.pinpoint.rpc.PinpointSocketException;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class ServerStreamChannelManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;
    private final ConcurrentMap<Integer, ServerStreamChannel> channelMap = new ConcurrentHashMap<Integer, ServerStreamChannel>();

    public ServerStreamChannelManager(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
    }

    public ServerStreamChannel createStreamChannel(int channelId) {
        ServerStreamChannel streamChannel = new ServerStreamChannel(channelId);
        streamChannel.setChannel(channel);

        ServerStreamChannel old = channelMap.put(channelId, streamChannel);
        if (old != null) {
            throw new PinpointSocketException("already channelId exist:" + channelId + " streamChannel:" + old);
        }
        // handle을 붙여서 리턴.
        streamChannel.setServerStreamChannelManager(this);

        return streamChannel;
    }


    public ServerStreamChannel findStreamChannel(int channelId) {
        return this.channelMap.get(channelId);
    }

    public boolean closeChannel(int channelId) {
        ServerStreamChannel remove = this.channelMap.remove(channelId);
        return remove != null;
    }


    public void closeInternal() {
        final boolean debugEnabled = logger.isDebugEnabled();
        for (Map.Entry<Integer, ServerStreamChannel> streamChannel : this.channelMap.entrySet()) {
            streamChannel.getValue().closeInternal();
            if (debugEnabled) {
                logger.debug("ServerStreamChannel.closeInternal() id:{}, {}", streamChannel.getKey(), channel);
            }
        }


    }
}
