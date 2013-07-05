package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.StreamCreateResultPacket;
import com.nhn.pinpoint.common.io.rpc.packet.StreamPacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class StreamChannelManager {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Channel channel;
    private final AtomicInteger idAllocator = new AtomicInteger(0);

    private final ConcurrentMap<Integer, StreamChannel> channelMap = new ConcurrentHashMap<Integer, StreamChannel>();

    public StreamChannel createStreamChannelFuture() {

        final int channelId = allocateChannelId();
        StreamChannel streamChannel = new StreamChannel(channelId);


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

    public boolean receivedStreamPacket(StreamPacket packet) {
        StreamChannel streamChannel = channelMap.get(packet.getChannelId());
        if (streamChannel == null) {
            logger.warn("streamChannel not found channelId:{}", packet.getChannelId());
            return false;
        }
        return streamChannel.receiveStreamPacket(packet);
    }

    public StreamChannel findStreamChannel(int channelId) {
        return this.channelMap.get(channelId);
    }

    public boolean closeChannel(int channelId) {
        StreamChannel remove = this.channelMap.remove(channelId);
        return remove != null;
    }

    public ChannelFuture writeStreamPacket(StreamPacket packet) {
        // connector이 close상태 체크가 필요함.
        // ex : ensureOpen()

        return channel.write(packet);
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void messageReceived(StreamCreateResultPacket streamCreateResult, Channel channel) {
        int channelId = streamCreateResult.getChannelId();
        StreamChannel streamChannel = findStreamChannel(channelId);
        streamChannel.receiveStreamPacket(streamCreateResult);

    }
}
