package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import com.nhn.pinpoint.common.io.rpc.packet.SendPacket;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class PinpointSocket {
    // 0 핸드쉐이크 안함.. 1은 동작중, 2는 closed
    private static final int STATE_INIT = 0;
    private static final int STATE_RUN = 1;
    private static final int STATE_CLOSED = 2;
    private final AtomicInteger state = new AtomicInteger(STATE_INIT);

    private final AtomicInteger messageId = new AtomicInteger();

    private ConcurrentMap<Integer, MessageFuture> requestMap = new ConcurrentHashMap<Integer, MessageFuture>();

    private Channel channel;

    public PinpointSocket(Channel channel) {
        this.channel = channel;
        open();
    }

    private void open() {
        // 핸드쉐이크를 하면 open 해야됨.
        if (!(this.state.compareAndSet(STATE_INIT, STATE_RUN))) {
            throw new IllegalStateException("invalid open state");
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
        MessageFuture messageFuture = registerRequest(request);
        this.channel.write(request);
        return messageFuture ;
    }

    private MessageFuture registerRequest(RequestPacket request) {
        int requestId = this.messageId.getAndIncrement();
        request.setRequestId(requestId);
        MessageFuture future = new MessageFuture();
        this.requestMap.put(requestId, future);
        return future;
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

        this.channel.close();
    }

}
