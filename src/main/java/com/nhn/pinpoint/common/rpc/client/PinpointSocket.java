package com.nhn.pinpoint.common.rpc.client;

import com.nhn.pinpoint.common.rpc.DefaultFuture;
import com.nhn.pinpoint.common.rpc.Future;
import com.nhn.pinpoint.common.rpc.PinpointSocketException;
import com.nhn.pinpoint.common.rpc.ResponseMessage;
import com.nhn.pinpoint.common.rpc.packet.*;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class PinpointSocket extends SimpleChannelHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 0 핸드쉐이크 안함.. 1은 동작중, 2는 closed
    private static final int STATE_INIT = 0;
    private static final int STATE_RUN = 1;
    private static final int STATE_CLOSED = 2;
//    이 상태가 있어야 되나?
//    private static final int STATE_ERROR_CLOSED = 3;

    private final AtomicInteger state = new AtomicInteger(STATE_INIT);

    private volatile Channel channel;

    private long timeoutMillis = 3000;

    public PinpointSocket() {
    }


    void setChannel(Channel channel) {
        this.channel = channel;
    }

    void open() {
        if (this.channel == null) {
            throw new PinpointSocketException("channel is null");
        }
        ChannelContext context = createContext();
        channel.setAttachment(context);

        // 핸드쉐이크를 하면 open 해야됨.
        if (!(this.state.compareAndSet(STATE_INIT, STATE_RUN))) {
            throw new IllegalStateException("invalid open state:" + state.get());
        }

    }

    private ChannelContext createContext() {
        ChannelContext channelContext = new ChannelContext();

        RequestManager requestManager = new RequestManager();
        channelContext.setRequestManager(requestManager);

        StreamChannelManager streamChannelManager = new StreamChannelManager();
        channelContext.setStreamChannelManager(streamChannelManager);
        return channelContext;
    }

    public void send(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        send0(bytes);
    }

    public boolean sendSync(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        ChannelFuture write = send0(bytes);
        return await(write);
    }

    private boolean await(ChannelFuture channelFuture) {
        try {
            channelFuture.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return channelFuture.isSuccess();
        }
        boolean success = channelFuture.isSuccess();
        if (success) {
            return true;
        } else {
            final Throwable cause = channelFuture.getCause();
            if (cause != null) {
                throw new PinpointSocketException(cause);
            } else {
                // 3초에도 io가 안끝나면 일단 timeout인가?
                throw new PinpointSocketException("io timeout");
            }
        }
    }

    private ChannelFuture send0(byte[] bytes) {
        ensureOpen();
        SendPacket send = new SendPacket(bytes);

        return this.channel.write(send);
    }

    public Future<ResponseMessage> request(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }

        ensureOpen();

        RequestPacket request = new RequestPacket(bytes);

        final Channel channel = this.channel;
        final DefaultFuture<ResponseMessage> messageFuture = getRequestManger(channel).register(request, this.timeoutMillis);

        ChannelFuture write = this.channel.write(request);
        write.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    Throwable cause = future.getCause();
                    // io write fail
                    messageFuture.setFailure(cause);
                }
            }
        });

        return messageFuture;
    }

    private ChannelContext getChannelContext(Channel channel) {
        return (ChannelContext) channel.getAttachment();
    }

    private RequestManager getRequestManger(Channel channel) {
        return getChannelContext(channel).getRequestManager();
    }

    private StreamChannelManager getStreamChannelManager(Channel channel) {
        return getChannelContext(channel).getStreamChannelManager();
    }


    public StreamChannel createStreamChannel() {
        ensureOpen();

        final Channel channel = this.channel;
        return getStreamChannelManager(channel).createStreamChannel(channel);
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        final Object message = e.getMessage();
        final ChannelContext channelContext = getChannelContext(e.getChannel());
        if (message instanceof Packet) {
            Packet packet = (Packet) message;
            final short packetType = packet.getPacketType();
            // 점프 테이블로 교체.
            switch (packetType) {
                case PacketType.APPLICATION_RESPONSE:
                    channelContext.getRequestManager().messageReceived((ResponsePacket) message, e.getChannel());
                    return;
                case PacketType.APPLICATION_REQUEST:
                    channelContext.getRequestManager().messageReceived((RequestPacket) message, e.getChannel());
                    return;
                    // connector로 들어오는 request 메시지를 핸들링을 해야 함.
                case PacketType.APPLICATION_STREAM_CREATE:
                case PacketType.APPLICATION_STREAM_CLOSE:
                case PacketType.APPLICATION_STREAM_CREATE_SUCCESS:
                case PacketType.APPLICATION_STREAM_CREATE_FAIL:
                case PacketType.APPLICATION_STREAM_RESPONSE:
                    channelContext.getStreamChannelManager().messageReceived((StreamPacket) message, e.getChannel());
                    return;
                default:
                    logger.warn("unexpectedMessage received:{} address:{}", message, e.getRemoteAddress());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.error("UnexpectedError happened. event:{}", e, e.getCause());
        state.set(STATE_CLOSED);
        e.getChannel().close();

    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        int currentState = state.get();
        if (currentState == STATE_CLOSED) {
            return;
        }
        if (currentState == STATE_RUN) {
            logger.info("unexpectedChannelClosed reconnect channel:{} Cauesed:{}", e.getChannel());
        }
    }

    private void ensureOpen() {
        final int currentState = state.get();
        if (currentState != STATE_RUN) {
            throw new PinpointSocketException("already closed");
        }
    }

    public void close() {
        if (!state.compareAndSet(STATE_RUN, STATE_CLOSED)) {
            return;
        }
        // hand shake close
        Channel channel = this.channel;
        ChannelContext ctx = getChannelContext(channel);

        ctx.getRequestManager().close();
        ctx.getStreamChannelManager().close();
        channel.close();
    }

}
