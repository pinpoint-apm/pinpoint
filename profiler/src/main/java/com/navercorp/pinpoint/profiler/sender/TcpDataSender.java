/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender;


import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.profiler.context.thrift.BypassMessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author emeroad
 * @author koo.taejin
 * @author netspider
 */
public class TcpDataSender implements EnhancedDataSender<Object> {

    private static final int DEFAULT_QUEUE_SIZE = 1024 * 5;

    private final Logger logger;

    static {
        // preClassLoad
        ChannelBuffers.buffer(2);
    }

    private final PinpointClient client;
    private final Timer timer;

    private final AtomicBoolean fireState = new AtomicBoolean(false);

    private final WriteFailFutureListener writeFailFutureListener;

    private final MessageSerializer<byte[]> messageSerializer;

    private final RetryQueue retryQueue = new RetryQueue();

    protected final AsyncQueueingExecutor<Object> executor;


    public TcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory) {
        this(name, ClientFactoryUtils.newPinpointClientProvider(host, port, clientFactory), newDefaultMessageSerializer(), DEFAULT_QUEUE_SIZE);
    }

    private static ThriftMessageSerializer newDefaultMessageSerializer() {
        MessageConverter<TBase<?, ?>> messageConverter = new BypassMessageConverter<TBase<?, ?>>();
        return new ThriftMessageSerializer(messageConverter);
    }

    public TcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory, MessageSerializer<byte[]> messageSerializer) {
        this(name, ClientFactoryUtils.newPinpointClientProvider(host, port, clientFactory), messageSerializer, DEFAULT_QUEUE_SIZE);
    }

    public TcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory, MessageSerializer<byte[]> messageSerializer, int queueSize) {
        this(name, ClientFactoryUtils.newPinpointClientProvider(host, port, clientFactory), messageSerializer, queueSize);
    }

    private TcpDataSender(String name, ClientFactoryUtils.PinpointClientProvider clientProvider, MessageSerializer<byte[]> messageSerializer, int queueSize) {
        this.logger = newLogger(name);

        Assert.requireNonNull(clientProvider, "clientProvider");
        this.client = clientProvider.get();

        Assert.isTrue(queueSize > 0, "queueSize must be 'queueSize > 0'");

        this.messageSerializer = Assert.requireNonNull(messageSerializer, "messageSerializer");
        this.timer = createTimer(name);

        this.writeFailFutureListener = new WriteFailFutureListener(logger, "io write fail.", clientProvider.getAddressAsString());

        final String executorName = getExecutorName(name);
        this.executor = createAsyncQueueingExecutor(queueSize, executorName);
    }

    private AsyncQueueingExecutor<Object> createAsyncQueueingExecutor(int queueSize, String executorName) {
        AsyncQueueingExecutorListener<Object> listener = new DefaultAsyncQueueingExecutorListener() {
            @Override
            public void execute(Object message) {
                TcpDataSender.this.sendPacket(message);
            }
        };
        final AsyncQueueingExecutor<Object> executor = new AsyncQueueingExecutor<Object>(queueSize, executorName, listener);
        return executor;
    }

    private Logger newLogger(String name) {
        final String loggerName = getLoggerName(name);
        return LoggerFactory.getLogger(loggerName);
    }

    private String getLoggerName(String name) {
        if (name == null) {
            return this.getClass().getName();
        } else {
            return this.getClass().getName() + "@" + name;
        }
    }

    private String getExecutorName(String name) {
        name = StringUtils.defaultString(name, "DEFAULT");
        return String.format("Pinpoint-TcpDataSender(%s)-Executor", name);
    }


    private Timer createTimer(String name) {
        final String timerName = getTimerName(name);

        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer(timerName, 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    private String getTimerName(String name) {
        name = StringUtils.defaultString(name, "DEFAULT");
        return String.format("Pinpoint-TcpDataSender(%s)-Timer", name);
    }

    @Override
    public boolean send(Object data) {
        return executor.execute(data);
    }

    @Override
    public boolean request(Object data) {
        return this.request(data, 3);
    }

    @Override
    public boolean request(Object data, int retryCount) {
        final RequestMessage<?> message = RequestMessageFactory.request(data, retryCount);
        return executor.execute(message);
    }

    @Override
    public boolean request(Object data, FutureListener<ResponseMessage> listener) {
        final RequestMessage<Object> message = RequestMessageFactory.request(data, listener);
        return executor.execute(message);
    }

    public boolean isConnected() {
        return client.isConnected();
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

        if (client != null) {
            client.close();
        }
    }

    protected void sendPacket(Object message) {
        try {
            if (message instanceof RequestMessage<?>) {
                final RequestMessage<?> requestMessage = (RequestMessage<?>) message;
                if (doRequest(requestMessage)) {
                    return;
                }
            }

            final byte[] copy = messageSerializer.serializer(message);
            if (copy == null) {
                logger.error("sendPacket fail. invalid dto type:{}", message.getClass());
                return;
            }
            doSend(copy);
        } catch (Exception e) {
            logger.warn("tcp send fail. Caused:{}", e.getMessage(), e);
        }
    }

    private boolean doRequest(RequestMessage<?> requestMessage) {
        final Object message = requestMessage.getMessage();

        final byte[] copy = messageSerializer.serializer(message);
        if (copy == null) {
            return false;
        }

        final FutureListener futureListener = requestMessage.getFutureListener();
        if (futureListener != null) {
            doRequest(copy, futureListener);
        } else {
            int retryCount = requestMessage.getRetryCount();
            doRequest(copy, retryCount, message);
        }

        return true;
    }

    protected void doSend(byte[] copy) {
        Future write = this.client.sendAsync(copy);
        write.setListener(writeFailFutureListener);
    }

    // Separate doRequest method to avoid creating unnecessary objects. (Generally, sending message is successed when firt attempt.)
    private void doRequest(final byte[] requestPacket, final int maxRetryCount, final Object targetClass) {
        FutureListener futureListener = (new FutureListener<ResponseMessage>() {
            @Override
            public void onComplete(Future<ResponseMessage> future) {
                if (future.isSuccess()) {
                    // Should cache?
                    ResponseMessage responseMessage = future.getResult();
                    HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
                    TBase<?, ?> response = deserialize(deserializer, responseMessage.getMessage());
                    if (response instanceof TResult) {
                        TResult result = (TResult) response;
                        if (result.isSuccess()) {
                            logger.debug("result success");
                        } else {
                            logger.info("request fail. request:{} Caused:{}", targetClass, result.getMessage());
                            RetryMessage retryMessage = new RetryMessage(1, maxRetryCount, requestPacket, targetClass.getClass().getSimpleName());
                            retryRequest(retryMessage);
                        }
                    } else {
                        logger.warn("Invalid response:{}", response);
                        // This is not retransmission. need to log for debugging
                        // it could be null
//                        retryRequest(requestPacket);
                    }
                } else {
                    logger.info("request fail. request:{} Caused:{}", targetClass, future.getCause().getMessage(), future.getCause());
                    RetryMessage retryMessage = new RetryMessage(1, maxRetryCount, requestPacket, targetClass.getClass().getSimpleName());
                    retryRequest(retryMessage);
                }
            }
        });

        doRequest(requestPacket, futureListener);
    }

    // Separate doRequest method to avoid creating unnecessary objects. (Generally, sending message is successed when firt attempt.)
    private void doRequest(final RetryMessage retryMessage) {
        FutureListener futureListener = (new FutureListener<ResponseMessage>() {
            @Override
            public void onComplete(Future<ResponseMessage> future) {
                if (future.isSuccess()) {
                    // Should cache?
                    HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
                    ResponseMessage responseMessage = future.getResult();
                    TBase<?, ?> response = deserialize(deserializer, responseMessage.getMessage());
                    if (response instanceof TResult) {
                        TResult result = (TResult) response;
                        if (result.isSuccess()) {
                            logger.debug("result success");
                        } else {
                            logger.info("request fail. request:{}, Caused:{}", retryMessage, result.getMessage());
                            retryRequest(retryMessage);
                        }
                    } else {
                        logger.warn("Invalid response:{}", response);
                        // This is not retransmission. need to log for debugging
                        // it could be null
//                        retryRequest(requestPacket);
                    }
                } else {
                    logger.info("request fail. request:{}, caused:{}", retryMessage, future.getCause().getMessage(), future.getCause());
                    retryRequest(retryMessage);
                }
            }
        });

        doRequest(retryMessage.getBytes(), futureListener);
    }

    private TBase<?, ?> deserialize(HeaderTBaseDeserializer deserializer, byte[] message) {
        final Message<TBase<?, ?>> deserialize = SerializationUtils.deserialize(message, deserializer, null);
        if (deserialize == null) {
            return null;
        }
        return deserialize.getData();
    }

    private void retryRequest(RetryMessage retryMessage) {
        retryQueue.add(retryMessage);
        if (fireTimeout()) {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    while (true) {
                        RetryMessage retryMessage = retryQueue.get();
                        if (retryMessage == null) {
                            // Maybe concurrency issue. But ignore it because it's unlikely.
                            fireComplete();
                            return;
                        }
                        int fail = retryMessage.fail();
                        doRequest(retryMessage);
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

}
