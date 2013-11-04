package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.rpc.DefaultFuture;
import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 */
public class PinpointSocket {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile SocketHandler socketHandler;

    private volatile boolean closed;

    public PinpointSocket(SocketHandler socketHandler) {
        if (socketHandler == null) {
            throw new NullPointerException("socketHandler");
        }
        this.socketHandler = socketHandler;
        socketHandler.setPinpointSocket(this);
    }

    public PinpointSocket() {
        this.socketHandler = new ReconnectStateSocketHandler();
    }


    void reconnectSocketHandler(SocketHandler socketHandler) {
        if (socketHandler == null) {
            throw new NullPointerException("socketHandler must not be null");
        }
        if (closed) {
            logger.warn("reconnectSocketHandler(). socketHandler force close.");
            socketHandler.close();
            return;
        }
        logger.warn("reconnectSocketHandler:{}", socketHandler);
        this.socketHandler = socketHandler;
    }

    public void sendSync(byte[] bytes) {
        ensureOpen();
        socketHandler.sendSync(bytes);
    }


    public Future sendAsync(byte[] bytes) {
        ensureOpen();
        return socketHandler.sendAsync(bytes);
    }

    public void send(byte[] bytes) {
        ensureOpen();
        socketHandler.send(bytes);
    }


    public Future<ResponseMessage> request(byte[] bytes) {
        if (socketHandler == null) {
            return returnFailureFuture();
        }
        return socketHandler.request(bytes);
    }


    public StreamChannel createStreamChannel() {
        // 실패를 리턴하는 StreamChannel을 던져야 되는데. StreamChannel을 interface로 변경해야 됨.
        // 일단 그냥 ex를 던지도록 하겠음.
        ensureOpen();
        return socketHandler.createStreamChannel();
    }

    private Future<ResponseMessage> returnFailureFuture() {
        DefaultFuture<ResponseMessage> future = new DefaultFuture<ResponseMessage>();
        future.setFailure(new PinpointSocketException("socketHandler is null"));
        return future;
    }

    private void ensureOpen() {
        if (socketHandler == null) {
            throw new PinpointSocketException("socketHandler is null");
        }
    }

    /**
     * ping packet을 tcp 채널에 write한다.
     * write 실패시 PinpointSocketException이 throw 된다.
     */
    public void sendPing() {
        SocketHandler socketHandler = this.socketHandler;
        if (socketHandler == null) {
            return;
        }
        socketHandler.sendPing();
    }

    public void close() {
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
        }
        SocketHandler socketHandler = this.socketHandler;
        if (socketHandler == null) {
            return;
        }
        socketHandler.close();
    }

    public boolean isClosed() {
        return closed;
    }
}
