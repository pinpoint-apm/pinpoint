package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.io.rpc.packet.SendPacket;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class PinpointSocket  {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 0 핸드쉐이크 안함.. 1은 동작중, 2는 closed
    private static final int STATE_INIT = 0;
    private static final int STATE_RUN = 1;
    private static final int STATE_CLOSED = 2;

    private final AtomicInteger state = new AtomicInteger(STATE_INIT);

    private Channel channel;
    private SocketRequestHandler socketRequestHandler;
    private long timeoutMillis = 3000;

    public PinpointSocket() {
        this.socketRequestHandler = new SocketRequestHandler();
    }


    void setChannel(Channel channel) {
        this.channel = channel;
    }

    void open() {
        // 핸드쉐이크를 하면 open 해야됨.
        if (!(this.state.compareAndSet(STATE_INIT, STATE_RUN))) {
            throw new IllegalStateException("invalid open state:" + state.get());
        }
    }

    public void send(byte[] bytes) {
        send0(bytes);
    }

    public void sendSync(byte[] bytes) {
        ChannelFuture write = send0(bytes);
        await(write);
    }

    private void await(ChannelFuture channelFuture) {
        try {
            channelFuture.await(3000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SocketException(e);
        }
        boolean success = channelFuture.isSuccess();
        if (success) {
            return;
        } else {
            final Throwable cause = channelFuture.getCause();
            if (cause != null) {
                throw new SocketException(cause);
            } else {
                // 3초에도 io가 안끝나면 무조껀 timeout인가?
                boolean cancel = channelFuture.cancel();
                throw new SocketException("io timeout");
            }
        }
    }

    private ChannelFuture send0(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        ensureOpen();
        SendPacket send = new SendPacket(bytes);

        return this.channel.write(send);
    }

    public MessageFuture request(byte[] bytes) {
        ensureOpen();

        RequestPacket request = new RequestPacket(bytes);
        final MessageFuture messageFuture = this.socketRequestHandler.register(request, this.timeoutMillis);

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



    public StreamChannelFuture createStreamChannel() {
        ensureOpen();
        return new StreamChannelFuture();
    }


    private void ensureOpen() {
        if (state.get() != STATE_RUN) {
            throw new SocketException("already closed");
        }
    }

    public void close() {
        if (!state.compareAndSet(STATE_RUN, STATE_CLOSED)) {
            return;
        }
        // hand shake close
        this.socketRequestHandler.close();
        this.channel.close();
    }

    ChannelHandler getSocketRequestHandler() {
        return this.socketRequestHandler;
    }
}
