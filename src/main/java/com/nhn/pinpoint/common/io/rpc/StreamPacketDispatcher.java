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
public class StreamPacketDispatcher {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Channel channel;
    private final AtomicInteger idAllocator = new AtomicInteger(0);

    private final ConcurrentMap<Integer, StreamChannel> channelMap = new ConcurrentHashMap<Integer, StreamChannel>();

    public StreamChannel createStreamChannel() {
        final int channelId = allocateChannelId();
        StreamChannel streamChannel = new StreamChannel(channelId);


        StreamChannel old = channelMap.put(channelId, streamChannel);
        if (old != null) {
            throw new IllegalStateException("already channelId exist:" + channelId + " streamChannel:" + old);
        }
        // handler을 붙여서 리턴.
        streamChannel.setStreamPacketDispatcher(this);

        return streamChannel;
    }

    private int allocateChannelId() {
        return idAllocator.get();
    }

    public boolean receivedStreamPacket(StreamPacket packet) {
        StreamChannel streamChannel = channelMap.get(packet.getChannelId());
        if (streamChannel == null) {
            logger.warn("streamChannel not found channelId:{}", packet.getChannelId());
            return false;
        }
//        streamChannel.receiveStreamResponse(packet);
        return true;
    }

    public boolean closeChannel(int channelId) {
        StreamChannel remove = this.channelMap.remove(channelId);
        return remove != null;
    }

    public void writeStreamPacket(StreamPacket packet) {
        channel.write(packet);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
