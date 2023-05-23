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

package com.navercorp.pinpoint.thrift.sender;


import com.navercorp.pinpoint.common.profiler.concurrent.executor.AsyncQueueingExecutor;
import com.navercorp.pinpoint.common.profiler.message.BypassMessageConverter;
import com.navercorp.pinpoint.common.profiler.message.EnhancedDataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * @author emeroad
 * @author koo.taejin
 * @author netspider
 */
public class TcpDataSender<T> implements EnhancedDataSender<T, ResponseMessage>, ReconnectEventListenerRegistry<PinpointClient> {

    private static final int DEFAULT_QUEUE_SIZE = 1024 * 5;

    private final Logger logger;

    static {
        // preClassLoad
        ChannelBuffers.buffer(2);
    }

    private final PinpointClient client;
    private final Timer timer;

    private final AtomicBoolean fireState = new AtomicBoolean(false);

    private final BiConsumer<Void, Throwable> writeFailFutureListener;

    private final MessageSerializer<T, byte[]> messageSerializer;

    private final RetryQueue retryQueue = new RetryQueue();

    protected final AsyncQueueingExecutor<Object> executor;


    public TcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory) {
        this(name, ClientFactoryUtils.newPinpointClientProvider(host, port, clientFactory),
                newDefaultMessageSerializer(), DEFAULT_QUEUE_SIZE);
    }


    private static <V> MessageSerializer<V, byte[]> newDefaultMessageSerializer() {
        MessageConverter<V, TBase<?, ?>> messageConverter = new BypassMessageConverter<>();
        return new ThriftMessageSerializer<>(messageConverter);
    }


    public TcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory, MessageSerializer<T, byte[]> messageSerializer) {
        this(name, ClientFactoryUtils.newPinpointClientProvider(host, port, clientFactory), messageSerializer, DEFAULT_QUEUE_SIZE);
    }

    public TcpDataSender(String name, String host, int port, PinpointClientFactory clientFactory, MessageSerializer<T, byte[]> messageSerializer, int queueSize) {
        this(name, ClientFactoryUtils.newPinpointClientProvider(host, port, clientFactory), messageSerializer, queueSize);
    }

    private TcpDataSender(String name, ClientFactoryUtils.PinpointClientProvider clientProvider, MessageSerializer<T, byte[]> messageSerializer, int queueSize) {
        this.logger = newLogger(name);

        Objects.requireNonNull(clientProvider, "clientProvider");
        this.client = clientProvider.get();

        Assert.isTrue(queueSize > 0, "queueSize must be 'queueSize > 0'");

        this.messageSerializer = Objects.requireNonNull(messageSerializer, "messageSerializer");
        this.timer = createTimer(name);

        this.writeFailFutureListener = new WriteFailFutureListener<>(logger, "io write fail.", clientProvider.getAddressAsString());

        final String executorName = getExecutorName(name);
        this.executor = createAsyncQueueingExecutor(queueSize, executorName);
    }

    private AsyncQueueingExecutor<Object> createAsyncQueueingExecutor(int queueSize, String executorName) {
        return new AsyncQueueingExecutor<>(queueSize, executorName, this::sendPacket);
    }

    private Logger newLogger(String name) {
        final String loggerName = getLoggerName(name);
        return LogManager.getLogger(loggerName);
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
    public boolean send(T data) {
        return executor.execute(data);
    }

    @Override
    public boolean request(T data) {
        return this.request(data, 3);
    }

    @Override
    public boolean request(T data, int retryCount) {
        final RequestMessage<T, ResponseMessage> message = RequestMessageFactory.request(data, retryCount);
        return executor.execute(message);
    }

    @Override
    public boolean request(T data, BiConsumer<ResponseMessage, Throwable> listener) {
        final RequestMessage<T, ResponseMessage> message = RequestMessageFactory.request(data, 3, listener);
        return executor.execute(message);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public boolean addEventListener(java.util.function.Consumer<PinpointClient> eventListener) {
        return this.client.addPinpointClientReconnectEventListener(eventListener);
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
            if (message instanceof RequestMessage<?, ?>) {
                final RequestMessage<T, ResponseMessage> requestMessage = (RequestMessage<T, ResponseMessage>) message;
                if (doRequest(requestMessage)) {
                    return;
                }
            }

            final byte[] copy = messageSerializer.serializer((T)message);
            if (copy == null) {
                logger.error("sendPacket fail. invalid dto type:{}", message.getClass());
                return;
            }
            doSend(copy);
        } catch (Exception e) {
            logger.warn("tcp send fail. Caused:{}", e.getMessage(), e);
        }
    }

    private boolean doRequest(RequestMessage<T, ResponseMessage> requestMessage) {
        final T message = requestMessage.getMessage();

        final byte[] copy = messageSerializer.serializer(message);
        if (copy == null) {
            return false;
        }

        final BiConsumer<ResponseMessage, Throwable> futureListener = requestMessage.getFutureListener();
        if (futureListener != null) {
            doRequest(copy, futureListener);
        } else {
            int retryCount = requestMessage.getRetryCount();
            doRequest(copy, retryCount, message);
        }

        return true;
    }

    protected void doSend(byte[] copy) {
        CompletableFuture<Void> write = this.client.sendAsync(copy);
        write.whenComplete(writeFailFutureListener);
    }

    // Separate doRequest method to avoid creating unnecessary objects. (Generally, sending message is successed when firt attempt.)
    private void doRequest(final byte[] requestPacket, final int maxRetryCount, final Object targetClass) {
        RetryMessage retryMessage = new RetryMessage(1, maxRetryCount, requestPacket, targetClass.getClass().getSimpleName());

        BiConsumer<ResponseMessage, Throwable> futureListener = newResponseListener(retryMessage);

        doRequest(requestPacket, futureListener);
    }

    private BiConsumer<ResponseMessage, Throwable> newResponseListener(RetryMessage retryMessage) {
        return new BiConsumer<ResponseMessage, Throwable>() {
            @Override
            public void accept(ResponseMessage responseMessage, Throwable th) {
                if (responseMessage != null) {
                    HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
                    TBase<?, ?> response = deserialize(deserializer, responseMessage.getMessage());
                    if (response instanceof TResult) {
                        TResult result = (TResult) response;
                        if (result.isSuccess()) {
                            logger.debug("result success");
                        } else {
                            logger.info("request fail. request:{} Caused:{}", retryMessage, result.getMessage());
                            retryRequest(retryMessage);
                        }
                    } else {
                        logger.warn("Invalid response:{}", response);
                        // This is not retransmission. need to log for debugging
                        // it could be null
//                        retryRequest(requestPacket);
                    }
                } else {
                    logger.info("request fail. request:{} Caused:{}", retryMessage, th.getMessage(), th);
                    retryRequest(retryMessage);
                }
            }
        };
    }

    // Separate doRequest method to avoid creating unnecessary objects. (Generally, sending message is successed when firt attempt.)
    private void doRequest(final RetryMessage retryMessage) {
        BiConsumer<ResponseMessage, Throwable> futureListener = newResponseListener(retryMessage);
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

    private void doRequest(final byte[] requestPacket, BiConsumer<ResponseMessage, Throwable> futureListener) {
        final CompletableFuture<ResponseMessage> response = this.client.request(requestPacket);
        response.whenComplete(futureListener);
    }

    private boolean fireTimeout() {
        return fireState.compareAndSet(false, true);
    }

    private void fireComplete() {
        logger.debug("fireComplete");
        fireState.compareAndSet(true, false);
    }

}
