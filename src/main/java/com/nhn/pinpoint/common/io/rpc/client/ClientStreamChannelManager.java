package com.nhn.pinpoint.common.io.rpc.client;

import com.nhn.pinpoint.common.io.rpc.PinpointSocketException;
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
public class ClientStreamChannelManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

   private final AtomicInteger idAllocator = new AtomicInteger(0);

    private final ConcurrentMap<Integer, StreamChannel> channelMap = new ConcurrentHashMap<Integer, StreamChannel>();

    public StreamChannel createStreamChannel(Channel channel) {
        final int channelId = allocateChannelId();
        StreamChannel streamChannel = new StreamChannel(channelId);
        streamChannel.setChannel(channel);


        StreamChannel old = channelMap.put(channelId, streamChannel);
        if (old != null) {
            throw new PinpointSocketException("already channelId exist:" + channelId + " streamChannel:" + old);
        }
        // handle을 붙여서 리턴.
        streamChannel.setClientStreamChannelManager(this);

        return streamChannel;
    }

    private int allocateChannelId() {
        return idAllocator.get();
    }


    public StreamChannel findStreamChannel(int channelId) {
        return this.channelMap.get(channelId);
    }

    public boolean closeChannel(int channelId) {
        StreamChannel remove = this.channelMap.remove(channelId);
        return remove != null;
    }



    public boolean messageReceived(StreamPacket streamPacket, Channel channel) {
        final int channelId = streamPacket.getChannelId();
        final StreamChannel streamChannel = findStreamChannel(channelId);
        if (streamChannel == null) {
            logger.warn("streamChannel not found. channelId:{}", streamPacket.getChannelId());
            return false;
        }
        return streamChannel.receiveStreamPacket(streamPacket);
    }
}
