package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.StreamPacket;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class ServerStreamChannelManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    private final ConcurrentMap<Integer, ServerStreamChannel> channelMap = new ConcurrentHashMap<Integer, ServerStreamChannel>();


    public ServerStreamChannel createStreamChannel(int channelId, Channel channel) {

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


}
