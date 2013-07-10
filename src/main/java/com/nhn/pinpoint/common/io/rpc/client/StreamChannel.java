package com.nhn.pinpoint.common.io.rpc.client;

import com.nhn.pinpoint.common.io.rpc.*;
import com.nhn.pinpoint.common.io.rpc.packet.PacketType;
import com.nhn.pinpoint.common.io.rpc.packet.StreamCreatePacket;
import com.nhn.pinpoint.common.io.rpc.packet.StreamPacket;
import com.nhn.pinpoint.common.io.rpc.packet.StreamResponsePacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class StreamChannel {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int NONE = 0;
    // OPEN 호출
    private static final int OPEN = 1;
    // OPEN 결과 대기
    private static final int OPEN_AWAIT = 2;
    // 동작중
    private static final int RUN = 3;
    // 닫힘
    private static final int CLOSED = 4;

    private final AtomicInteger state = new AtomicInteger(NONE);

    private final int channelId;

    private StreamChannelManager streamChannelManager;

    private StreamChannelMessageListener streamChannelMessageListener;

    private DefaultFuture<StreamCreateResponse> openLatch;
    private Channel channel;

    public StreamChannel(int channelId) {
        this.channelId = channelId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Future<StreamCreateResponse> open(byte[] bytes) {
        if (!state.compareAndSet(NONE, OPEN)) {
            throw new IllegalStateException("invalid state:" + state.get());
        }
        StreamCreatePacket streamCreatePacket = new StreamCreatePacket(channelId, bytes);

        this.openLatch = new DefaultFuture<StreamCreateResponse>();
        openLatch.setFailureEventHandler(new FailureEventHandler() {
            @Override
            public boolean fireFailure() {
                streamChannelManager.closeChannel(channelId);
                return false;
            }
        });
        ChannelFuture channelFuture = this.channel.write(streamCreatePacket);
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
        return openLatch;
    }


    public boolean receiveStreamPacket(StreamPacket packet) {
        final short packetType = packet.getPacketType();
        switch (packetType) {
            case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                logger.info("APPLICATION_STREAM_CREATE_SUCCESS");
                StreamCreateResponse success = new StreamCreateResponse(true);
                success.setMessage(packet.getPayload());
                return openChannel(RUN, success);

            case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                logger.info("APPLICATION_STREAM_CREATE_FAIL");
                StreamCreateResponse failResult = new StreamCreateResponse(false);
                failResult.setMessage(packet.getPayload());
                return openChannel(CLOSED, failResult);

            case PacketType.APPLICATION_STREAM_RESPONSE:
                logger.info("APPLICATION_STREAM_RESPONSE");

                StreamResponsePacket streamResponsePacket = (StreamResponsePacket) packet;
                StreamChannelMessageListener streamChannelMessageListener = this.streamChannelMessageListener;
                if (streamChannelMessageListener != null) {
                    streamChannelMessageListener.handleStream(this, streamResponsePacket.getPayload());
                }
        }
        return false;
    }

    private boolean openChannel(int channelState, StreamCreateResponse streamCreateResponse) {
        if (state.compareAndSet(OPEN_AWAIT, channelState)) {
            notifyOpenResult(streamCreateResponse);
            return true;
        } else {
            logger.info("invalid stream channel state:{}", state.get());
            return false;
        }
    }




    private boolean notifyOpenResult(StreamCreateResponse failResult) {
        DefaultFuture<StreamCreateResponse> openLatch = this.openLatch;
        if (openLatch != null) {
            return openLatch.setResult(failResult);
        }
        return false;
    }



    public boolean close() {
        if (!state.compareAndSet(RUN, CLOSED)) {
            return false;
        }
        StreamChannelManager streamChannelManager = this.streamChannelManager;
        if (streamChannelManager != null) {
            streamChannelManager.closeChannel(channelId);
            this.streamChannelManager = null;
        }
        return true;
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


    public void setStreamChannelMessageListener(StreamChannelMessageListener streamChannelMessageListener) {
        this.streamChannelMessageListener = streamChannelMessageListener;
    }


}

