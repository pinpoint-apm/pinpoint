package com.navercorp.pinpoint.collector.util;


import com.navercorp.pinpoint.rpc.PinpointSocketException;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author emeroad
 */
public class RequestManager<RES> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AtomicInteger requestId = new AtomicInteger(1);

    private final ConcurrentMap<Integer, CompletableFuture<RES>> requestMap = new ConcurrentHashMap<>();
    // Have to move Timer into factory?
    private final Timer timer;
    private final long defaultTimeoutMillis;

    public RequestManager(Timer timer, long defaultTimeoutMillis) {
        this.timer = Objects.requireNonNull(timer, "timer");

        if (defaultTimeoutMillis <= 0) {
            throw new IllegalArgumentException("defaultTimeoutMillis must greater than zero.");
        }
        this.defaultTimeoutMillis = defaultTimeoutMillis;
    }

    private BiConsumer<RES, Throwable> createFailureEventHandler(final int requestId) {
        return new RequestFailListener<>(requestId, this::removeMessageFuture);
    }

    private void addTimeoutTask(CompletableFuture<RES> future, long timeoutMillis) {
        Objects.requireNonNull(future, "future");

        try {
            Timeout timeout = timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    if (timeout.isCancelled()) {
                        return;
                    }
                    if (future.isDone()) {
                        return;
                    }
                    future.completeExceptionally(new TimeoutException("Timeout by RequestManager-TIMER"));
                }
            }, timeoutMillis, TimeUnit.MILLISECONDS);
            future.thenAccept(t -> timeout.cancel());
        } catch (IllegalStateException e) {
            // this case is that timer has been shutdown. That maybe just means that socket has been closed.
            future.completeExceptionally(new PinpointSocketException("socket closed")) ;
        }
    }

    public int nextRequestId() {
        return this.requestId.getAndIncrement();
    }


    public CompletableFuture<RES> messageReceived(int responseId, Supplier<Object> sourceName) {
        final CompletableFuture<RES> future = removeMessageFuture(responseId);
        if (future == null) {
            logger.warn("ResponseFuture not found. responseId:{}, sourceName:{}", responseId, sourceName.get());
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("ResponsePacket is arrived responseId:{}, sourceName:{}", responseId, sourceName.get());
        }
        return future;
    }


    public CompletableFuture<RES> removeMessageFuture(int requestId) {
        return this.requestMap.remove(requestId);
    }

    public CompletableFuture<RES> register(int requestId) {
        return register(requestId, defaultTimeoutMillis);
    }

    public CompletableFuture<RES> register(int requestId, long timeoutMillis) {
        // shutdown check
        final CompletableFuture<RES> responseFuture = new CompletableFuture<>();

        final CompletableFuture<RES> old = this.requestMap.put(requestId, responseFuture);
        if (old != null) {
            throw new PinpointSocketException("unexpected error. old future exist:" + old + " id:" + requestId);
        }
        // when future fails, put a handle in order to remove a failed future in the requestMap.
        BiConsumer<RES, Throwable> removeTable = createFailureEventHandler(requestId);
        responseFuture.whenComplete(removeTable);

        addTimeoutTask(responseFuture, timeoutMillis);
        return responseFuture;
    }

    public void close() {
        logger.debug("close()");
        final PinpointSocketException closed = new PinpointSocketException("socket closed");

        int requestFailCount = 0;
        for (Map.Entry<Integer, CompletableFuture<RES>> entry : requestMap.entrySet()) {
            if (entry.getValue().completeExceptionally(closed)) {
                requestFailCount++;
            }
        }
        this.requestMap.clear();
        if (requestFailCount > 0) {
            logger.info("Close fail count:{}", requestFailCount);
        }

    }

}
