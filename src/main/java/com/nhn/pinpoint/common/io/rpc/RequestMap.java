package com.nhn.pinpoint.common.io.rpc;

import com.nhn.pinpoint.common.io.rpc.packet.RequestPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class RequestMap {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AtomicInteger requestId = new AtomicInteger(1);

    private final ConcurrentMap<Integer, MessageFuture> requestMap = new ConcurrentHashMap<Integer, MessageFuture>();

    public RequestMap() {
    }

    public MessageFuture registerRequest(final RequestPacket request) {
        final int requestId = getNextRequestId();
        request.setRequestId(requestId);

        final MessageFuture future = new MessageFuture();

        final MessageFuture old = this.requestMap.put(requestId, future);
        if (old != null) {
            logger.warn("unexpected error. old future exist:{}", old);
        }
        return future;
    }

    private int getNextRequestId() {
        return this.requestId.getAndIncrement();
    }

    public MessageFuture findMessageFuture(int requestId) {
        return this.requestMap.get(requestId);
    }

}
