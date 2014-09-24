package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.FailureEventHandler;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.StreamCreateResponse;
import com.nhn.pinpoint.rpc.packet.*;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
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
                logger.debug("APPLICATION_STREAM_CREATE_SUCCESS {}", channel);
                StreamCreateResponse success = new StreamCreateResponse(true);
                success.setMessage(packet.getPayload());
                return openChannel(RUN, success);

            case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                logger.debug("APPLICATION_STREAM_CREATE_FAIL {}", channel);
                StreamCreateResponse failResult = new StreamCreateResponse(false);
                failResult.setMessage(packet.getPayload());
                return openChannel(CLOSED, failResult);

            case PacketType.APPLICATION_STREAM_RESPONSE: {
                logger.debug("APPLICATION_STREAM_RESPONSE {}", channel);

                StreamResponsePacket streamResponsePacket = (StreamResponsePacket) packet;
                StreamChannelMessageListener streamChannelMessageListener = this.streamChannelMessageListener;
                if (streamChannelMessageListener != null) {
                    streamChannelMessageListener.handleStreamResponse(this, streamResponsePacket.getPayload());
                }
                return true;
            }
            case PacketType.APPLICATION_STREAM_CLOSE: {
                logger.debug("APPLICATION_STREAM_CLOSE {}", channel);

                this.closeInternal();

                StreamClosePacket streamClosePacket = (StreamClosePacket) packet;
                StreamChannelMessageListener streamChannelMessageListener = this.streamChannelMessageListener;
                if (streamChannelMessageListener != null) {
                    streamChannelMessageListener.handleClose(this, streamClosePacket.getPayload());
                }

                return true;
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
        return close0(true);
    }



    boolean closeInternal() {
        return close0(false);
    }

    private boolean close0(boolean safeClose) {
        if (!state.compareAndSet(RUN, CLOSED)) {
            return false;
        }

        if (safeClose) {
            StreamClosePacket closePacket = new StreamClosePacket(this.channelId);
            this.channel.write(closePacket);

            StreamChannelManager streamChannelManager = this.streamChannelManager;
            if (streamChannelManager != null) {
                streamChannelManager.closeChannel(channelId);
                this.streamChannelManager = null;
            }
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
        if (channel != null ? !channel.equals(that.channel) : that.channel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = channelId;
        result = 31 * result + (channel != null ? channel.hashCode() : 0);
        return result;
    }

    public void setStreamChannelMessageListener(StreamChannelMessageListener streamChannelMessageListener) {
        this.streamChannelMessageListener = streamChannelMessageListener;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StreamChannel");
        sb.append("{channelId=").append(channelId);
        sb.append(", channel=").append(channel);
        sb.append('}');
        return sb.toString();
    }
}

