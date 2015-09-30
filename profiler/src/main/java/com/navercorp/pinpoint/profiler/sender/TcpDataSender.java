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

package com.navercorp.pinpoint.profiler.sender;


import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.thrift.TBase;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;

/**
 * @author emeroad
 * @author koo.taejin
 * @author netspider
 */
public class TcpDataSender extends AbstractDataSender implements EnhancedDataSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    static {
        // preClassLoad
        ChannelBuffers.buffer(2);
    }

    private final PinpointClient client;
    private final Timer timer;
    
    private final AtomicBoolean fireState = new AtomicBoolean(false);

    private final WriteFailFutureListener writeFailFutureListener;


    private final HeaderTBaseSerializer serializer = HeaderTBaseSerializerFactory.DEFAULT_FACTORY.createSerializer();

    private final RetryQueue retryQueue = new RetryQueue();

    private AsyncQueueingExecutor<Object> executor;

    public TcpDataSender(PinpointClient client) {
        this.client = client;
        this.timer = createTimer();
        writeFailFutureListener = new WriteFailFutureListener(logger, "io write fail.", "host", -1);
        this.executor = createAsyncQueueingExecutor(1024 * 5, "Pinpoint-TcpDataExecutor");
    }
    
    private Timer createTimer() {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-DataSender-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }
    
    @Override
    public boolean send(TBase<?, ?> data) {
        return executor.execute(data);
    }

    @Override
    public boolean request(TBase<?, ?> data) {
        return this.request(data, 3);
    }

    @Override
    public boolean request(TBase<?, ?> data, int retryCount) {
        RequestMarker message = new RequestMarker(data, retryCount);
        return executor.execute(message);
    }

    @Override
    public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
        RequestMarker message = new RequestMarker(data, listener);
        return executor.execute(message);
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return this.client.addPinpointClientReconnectEventListener(eventListener);
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return this.client.removePinpointClientReconnectEventListener(eventListener);
    }

    @Override
    public void stop() {
        executor.stop();

        Set<Timeout> stop = timer.stop();
        if (!stop.isEmpty()) {
            logger.info("stop Timeout:{}", stop.size());
        }
    }

    @Override
    protected void sendPacket(Object message) {
        try {
            if (message instanceof TBase) {
                byte[] copy = serialize(serializer, (TBase) message);
                if (copy == null) {
                    return;
                }
                doSend(copy);
            } else if (message instanceof RequestMarker) {
                RequestMarker requestMarker = (RequestMarker) message;

                TBase tBase = requestMarker.getTBase();
                int retryCount = requestMarker.getRetryCount();
                FutureListener futureListener = requestMarker.getFutureListener();
                byte[] copy = serialize(serializer, tBase);
                if (copy == null) {
                    return;
                }
                
                if (futureListener != null) {
                    doRequest(copy, futureListener);
                } else {
                    doRequest(copy, retryCount, tBase);
                }
            } else {
                logger.error("sendPacket fail. invalid dto type:{}", message.getClass());
                return;
            }
        } catch (Exception e) {
            logger.warn("tcp send fail. Caused:{}", e.getMessage(), e);
        }
    }

    private void doSend(byte[] copy) {
        Future write = this.client.sendAsync(copy);
        write.setListener(writeFailFutureListener);
    }

    private void doRequest(final byte[] requestPacket, final int retryCount, final Object targetClass) {
        FutureListener futureListener = (new FutureListener<ResponseMessage>() {
            @Override
            public void onComplete(Future<ResponseMessage> future) {
                if (future.isSuccess()) {
                    // Should cache?
                    HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
                    TBase<?, ?> response = deserialize(deserializer, future.getResult());
                    if (response instanceof TResult) {
                        TResult result = (TResult) response;
                        if (result.isSuccess()) {
                            logger.debug("result success");
                        } else {
                            logger.warn("request fail. clazz:{} Caused:{}", targetClass, result.getMessage());
                            retryRequest(requestPacket, retryCount, targetClass.getClass().getSimpleName());
                        }
                    } else {
                        logger.warn("Invalid ResponseMessage. {}", response);
                        // This is not retransmission. need to log for debugging
                        // it could be null
//                        retryRequest(requestPacket);
                    }
                } else {
                    logger.warn("request fail. clazz:{} Caused:{}", targetClass, future.getCause().getMessage(), future.getCause());
                    retryRequest(requestPacket, retryCount, targetClass.getClass().getSimpleName());
                }
            }
        });

        doRequest(requestPacket, futureListener);
    }

    private void retryRequest(byte[] requestPacket, int retryCount, final String className) {
        RetryMessage retryMessage = new RetryMessage(retryCount, requestPacket);
        retryQueue.add(retryMessage);
        if (fireTimeout()) {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    while(true) {
                        RetryMessage retryMessage = retryQueue.get();
                        if (retryMessage == null) {
                            // Maybe concurrency issue. But ignore it because it's unlikely.
                            fireComplete();
                            return;
                        }
                        int fail = retryMessage.fail();
                        doRequest(retryMessage.getBytes(), fail, className);
                    }
                }
            }, 1000 * 10, TimeUnit.MILLISECONDS);
        }
    }

    private void doRequest(final byte[] requestPacket, FutureListener futureListener) {
        final Future<ResponseMessage> response = this.client.request(requestPacket);
        response.setListener(futureListener);
    }

    private boolean fireTimeout() {
        if (fireState.compareAndSet(false, true)) {
            return true;
        } else {
            return false;
        }
    }

    private void fireComplete() {
        logger.debug("fireComplete");
        fireState.compareAndSet(true, false);
    }

    @Override
    public boolean isNetworkAvailable() {
        if (this.client == null) {
            return false;
        }
        return this.client.isConnected();
    }
}
