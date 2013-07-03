package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.StreamCreatePacket;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class StreamChannel {

    private AtomicInteger state = new AtomicInteger(0);


    private final int channelId;


    private StreamPacketDispatcher streamPacketDispatcher;

    private StreamChannelFuture openLatch;

    public StreamChannel(int channelId) {
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }

    public StreamChannelFuture open(byte[] bytes) {
        StreamCreatePacket streamCreatePacket = new StreamCreatePacket(channelId, bytes);
        this.streamPacketDispatcher.writeStreamPacket(streamCreatePacket);

        openLatch = new StreamChannelFuture(this);
        return openLatch;
    }

    public void setStreamResponseListener() {

    }

    public void receiveStreamResponse(byte[] stream) {
        openLatch.open();
    }


    public synchronized void close() {
        StreamPacketDispatcher streamPacketDispatcher = this.streamPacketDispatcher;
        if (streamPacketDispatcher != null) {
            streamPacketDispatcher.closeChannel(channelId);
            this.streamPacketDispatcher = null;
        }

    }

    public void setStreamPacketDispatcher(StreamPacketDispatcher streamPacketDispatcher) {
        this.streamPacketDispatcher = streamPacketDispatcher;
    }
}
