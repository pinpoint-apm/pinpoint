/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.packet.RequestPacket;
import com.navercorp.pinpoint.rpc.packet.ResponsePacket;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author emeroad
 */
public class RequestManager {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AtomicInteger requestId = new AtomicInteger(1);

    private final ConcurrentMap<Integer, CompletableFuture<ResponseMessage>> requestMap = new ConcurrentHashMap<>();
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

    private BiConsumer<ResponseMessage, Throwable> createFailureEventHandler(final int requestId) {
        return new BiConsumer<ResponseMessage, Throwable>() {
            @Override
            public void accept(ResponseMessage responseMessage, Throwable throwable) {
                if (throwable != null) {
                    removeMessageFuture(requestId);
                }
            }
        };
    }

    private <T> void addTimeoutTask(CompletableFuture<T> future, long timeoutMillis) {
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

    public void messageReceived(ResponsePacket responsePacket, String objectUniqName) {
        final int requestId = responsePacket.getRequestId();
        final CompletableFuture<ResponseMessage> future = removeMessageFuture(requestId);
        if (future == null) {
            logger.warn("future not found:{}, objectUniqName:{}", responsePacket, objectUniqName);
            return;
        } else {
            logger.debug("responsePacket arrived packet:{}, objectUniqName:{}", responsePacket, objectUniqName);
        }

        ResponseMessage response = ResponseMessage.wrap(responsePacket.getPayload());
        future.complete(response);
    }

    public void messageReceived(ResponsePacket responsePacket, PinpointServer pinpointServer) {
        final int requestId = responsePacket.getRequestId();
        final CompletableFuture<ResponseMessage> future = removeMessageFuture(requestId);
        if (future == null) {
            logger.warn("future not found:{}, pinpointServer:{}", responsePacket, pinpointServer);
            return;
        } else {
            logger.debug("responsePacket arrived packet:{}, pinpointServer:{}", responsePacket, pinpointServer);
        }

        ResponseMessage response = ResponseMessage.wrap(responsePacket.getPayload());
        future.complete(response);
    }

    public CompletableFuture<ResponseMessage> removeMessageFuture(int requestId) {
        return this.requestMap.remove(requestId);
    }

    public void messageReceived(RequestPacket requestPacket, Channel channel) {
        logger.error("unexpectedMessage received:{} address:{}", requestPacket, channel.getRemoteAddress());
    }

    public CompletableFuture<ResponseMessage> register(int requestId) {
        return register(requestId, defaultTimeoutMillis);
    }

    public CompletableFuture<ResponseMessage> register(int requestId, long timeoutMillis) {
        // shutdown check
        final CompletableFuture<ResponseMessage> responseFuture = new CompletableFuture<>();

        final CompletableFuture<ResponseMessage> old = this.requestMap.put(requestId, responseFuture);
        if (old != null) {
            throw new PinpointSocketException("unexpected error. old future exist:" + old + " id:" + requestId);
        }
        // when future fails, put a handle in order to remove a failed future in the requestMap.
        BiConsumer<ResponseMessage, Throwable> removeTable = createFailureEventHandler(requestId);
        responseFuture.whenComplete(removeTable);

        addTimeoutTask(responseFuture, timeoutMillis);
        return responseFuture;
    }

//    public ChannelWriteFailListenableFuture<ResponseMessage> register(final int requestId, final long timeoutMillis) {
//        // shutdown check
//        final ChannelWriteFailListenableFuture<ResponseMessage> responseFuture = new ChannelWriteFailListenableFuture<ResponseMessage>(timeoutMillis) {
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                fireWriteComplete(requestId, future, this, timeoutMillis);
//            }
//        };
//        return responseFuture;
//    }
//
//    private void fireWriteComplete(int requestId, ChannelFuture ioWriteFuture, DefaultFuture<ResponseMessage> responseFuture, long timeoutMillis) {
//        if (ioWriteFuture.isSuccess()) {
//            final DefaultFuture old = requestMap.put(requestId, responseFuture);
//            if (old != null) {
//                PinpointSocketException pinpointSocketException = new PinpointSocketException("unexpected error. old responseFuture exist:" + old + " id:" + requestId);
//                responseFuture.setFailure(pinpointSocketException);
//                return;
//            } else {
//                FailureEventHandler removeTable = createFailureEventHandler(requestId);
//                responseFuture.setFailureEventHandler(removeTable);
//                addTimeoutTask(responseFuture, timeoutMillis);
//            }
//        } else {
//            responseFuture.setFailure(ioWriteFuture.getCause());
//        }
//    }

    public void close() {
        logger.debug("close()");
        final PinpointSocketException closed = new PinpointSocketException("socket closed");

        // Could you handle race conditions of "close" more precisely?
//        final Timer timer = this.timer;
//        if (timer != null) {
//            Set<Timeout> stop = timer.stop();
//            for (Timeout timeout : stop) {
//                DefaultFuture future = (DefaultFuture)timeout.getTask();
//                future.setFailure(closed);
//            }
//        }
        int requestFailCount = 0;
        for (Map.Entry<Integer, CompletableFuture<ResponseMessage>> entry : requestMap.entrySet()) {
            if (entry.getValue().completeExceptionally(closed)) {
                requestFailCount++;
            }
        }
        this.requestMap.clear();
        if (requestFailCount > 0) {
            logger.info("requestManager failCount:{}", requestFailCount);
        }

    }

}

