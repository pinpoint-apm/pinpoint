package com.nhn.pinpoint.common.rpc.client;

import com.nhn.pinpoint.common.rpc.DefaultFuture;
import com.nhn.pinpoint.common.rpc.Future;
import com.nhn.pinpoint.common.rpc.PinpointSocketException;
import com.nhn.pinpoint.common.rpc.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
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
    }


    void replaceSocketHandler(SocketHandler socketHandler) {
        if (closed) {
            logger.warn("already closed");
            socketHandler.close();
            return;
        }
        logger.info("replaceSocketHandler:{}", socketHandler);
        this.socketHandler = socketHandler;
    }

    public boolean sendSync(byte[] bytes) {
        ensureOpen();
        return socketHandler.sendSync(bytes);
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

    public void close() {
        closed = true;
        socketHandler.close();
    }

    public boolean isClosed() {
        return closed;
    }
}
