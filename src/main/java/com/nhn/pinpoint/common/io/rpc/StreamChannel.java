package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.StreamCreatePacket;
import com.nhn.pinpoint.common.io.rpc.packet.StreamPacket;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class StreamChannel {
    private static final int NONE = 0;
    // OPEN 호출
    private static final int OPEN = 1;
    // OPEN 결과 대기
    private static final int OPEN_AWAIT = 2;
    // 동작중
    private static final int RUN = 3;
    // 닫힘
    private static final int CLOSED = 4;

    private AtomicInteger state = new AtomicInteger(NONE);

    private final int channelId;


    private StreamChannelManager streamChannelManager;

//    private DefaultFuture<StreamChannel> openLatch = new DefaultFuture<StreamChannel>(0);

    public StreamChannel(int channelId) {
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }

    public Future<StreamChannel> open(byte[] bytes) {
        if (!state.compareAndSet(NONE, OPEN)) {
            throw new IllegalStateException("invalid state");
        }
        StreamCreatePacket streamCreatePacket = new StreamCreatePacket(channelId, bytes);

        final DefaultFuture<StreamChannel> future = new DefaultFuture<StreamChannel>();
        future.setFailureEventHandler(new FailureEventHandler() {
            @Override
            public boolean fireFailure() {
                streamChannelManager.closeChannel(channelId);
                return false;
            }
        });
        ChannelFuture channelFuture = this.streamChannelManager.writeStreamPacket(streamCreatePacket);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.setFailure(future.getCause());
                }
            }
        });


        if (!state.compareAndSet(OPEN, OPEN_AWAIT)) {
            throw new IllegalStateException("invalid state");
        }
        return future;
    }

    public void setStreamResponseListener() {

    }

    public boolean receiveStreamPacket(StreamPacket packet) {

        return true;
    }


    public synchronized void close() {
        StreamChannelManager streamChannelManager = this.streamChannelManager;
        if (streamChannelManager != null) {
            streamChannelManager.closeChannel(channelId);
            this.streamChannelManager = null;
        }

    }

    public void setStreamChannelManager(StreamChannelManager streamChannelManager) {
        this.streamChannelManager = streamChannelManager;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StreamChannel that = (StreamChannel) o;

        if (channelId != that.channelId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return channelId;
    }
}

