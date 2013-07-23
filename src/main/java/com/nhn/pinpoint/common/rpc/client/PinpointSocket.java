package com.nhn.pinpoint.common.rpc.client;

import com.nhn.pinpoint.common.rpc.Future;
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
        this.socketHandler = socketHandler;
        socketHandler.setPinpointSocket(this);
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
        return socketHandler.sendSync(bytes);
    }

    public void send(byte[] bytes) {
        socketHandler.send(bytes);
    }

    public Future<ResponseMessage> request(byte[] bytes) {
        logger.info("socketHandler:{},", socketHandler);
        return socketHandler.request(bytes);
    }

    public StreamChannel createStreamChannel() {
        return socketHandler.createStreamChannel();
    }

    public void close() {
        closed = true;
        socketHandler.close();
    }
}
