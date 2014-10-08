package com.nhn.pinpoint.rpc.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.packet.stream.StreamPacket;

/**
 * @author emeroad
 */
public class StreamChannelManager {

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
        streamChannel.setStreamChannelManager(this);

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

    public void close() {
        logger.debug("close()");
        final ConcurrentMap<Integer, StreamChannel> channelMap = this.channelMap;

        int forceCloseChannel = 0;
        for (Map.Entry<Integer, StreamChannel> entry : channelMap.entrySet()) {
           if(entry.getValue().closeInternal()) {
               forceCloseChannel++;
           }
        }
        channelMap.clear();
        if(forceCloseChannel > 0) {
            logger.info("streamChannelManager forceCloseChannel {}", forceCloseChannel);
        }
    }


    public boolean messageReceived(StreamPacket streamPacket, Channel channel) {
        final int channelId = streamPacket.getStreamChannelId();
        final StreamChannel streamChannel = findStreamChannel(channelId);
        if (streamChannel == null) {
            logger.warn("streamChannel not found. channelId:{} ", channelId, channel);
            return false;
        }
        return streamChannel.receiveStreamPacket(streamPacket);
    }
}
