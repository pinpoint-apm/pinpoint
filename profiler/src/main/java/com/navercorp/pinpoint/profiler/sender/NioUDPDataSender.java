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
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.buffer.ByteBufferFactory;
import com.navercorp.pinpoint.rpc.buffer.ByteBufferFactoryLocator;
import com.navercorp.pinpoint.rpc.buffer.ByteBufferType;
import com.navercorp.pinpoint.thrift.io.ByteBufferOutputStream;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer2;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory2;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author Taejin Koo
 */
public class NioUDPDataSender implements DataSender {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    public static final int SOCKET_TIMEOUT = 1000 * 5;
    public static final int SEND_BUFFER_SIZE = 1024 * 64 * 16;
    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    private final DatagramChannel datagramChannel;
    private final HeaderTBaseSerializer2 serializer;
    private final ByteBufferOutputStream byteBufferOutputStream;

    private final AsyncQueueingExecutor<Object> executor;
    private final MessageConverter<TBase<?, ?>> messageConverter;

    private volatile boolean closed = false;


    public NioUDPDataSender(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize,
                            MessageConverter<TBase<?, ?>> messageConverter) {
        Assert.requireNonNull(host, "host");
        Assert.requireNonNull(threadName, "threadName");
        Assert.isTrue(queueSize > 0, "queueSize");
        Assert.isTrue(timeout > 0, "timeout");
        Assert.isTrue(sendBufferSize > 0, "sendBufferSize");

        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");

        // TODO If fail to create socket, stop agent start
        logger.info("NioUDPDataSender initialized. host={}, port={}", host, port);
        this.datagramChannel = createChannel(host, port, timeout, sendBufferSize);

        HeaderTBaseSerializerFactory2 serializerFactory = new HeaderTBaseSerializerFactory2();
        this.serializer = serializerFactory.createSerializer();

        ByteBufferFactory bufferFactory = ByteBufferFactoryLocator.getFactory(ByteBufferType.DIRECT);
        ByteBuffer byteBuffer = bufferFactory.getBuffer(UDP_MAX_PACKET_LENGTH);
        this.byteBufferOutputStream = new ByteBufferOutputStream(byteBuffer);

        this.executor = createAsyncQueueingExecutor(queueSize, threadName);
    }

    private AsyncQueueingExecutor<Object> createAsyncQueueingExecutor(int queueSize, String executorName) {
        AsyncQueueingExecutorListener<Object> listener = new DefaultAsyncQueueingExecutorListener() {
            @Override
            public void execute(Object message) {
                NioUDPDataSender.this.sendPacket(message);
            }
        };
        final AsyncQueueingExecutor<Object> executor = new AsyncQueueingExecutor<Object>(queueSize, executorName, listener);
        return executor;
    }



    private DatagramChannel createChannel(String host, int port, int timeout, int sendBufferSize) {
        DatagramChannel datagramChannel = null;
        DatagramSocket socket = null;
        try {
            datagramChannel = DatagramChannel.open();
            socket = datagramChannel.socket();
            socket.setSoTimeout(timeout);
            socket.setSendBufferSize(sendBufferSize);

            if (logger.isWarnEnabled()) {
                final int checkSendBufferSize = socket.getSendBufferSize();
                if (sendBufferSize != checkSendBufferSize) {
                    logger.warn("DatagramChannel.setSendBufferSize() error. {}!={}", sendBufferSize, checkSendBufferSize);
                }
            }

            InetSocketAddress serverAddress = new InetSocketAddress(host, port);
            datagramChannel.connect(serverAddress);

            return datagramChannel;
        } catch (IOException e) {
            IOUtils.closeQuietly(socket);
            IOUtils.closeQuietly(datagramChannel);

            throw new IllegalStateException("DatagramChannel create fail. Cause" + e.getMessage(), e);
        }
    }

    @Override
    public boolean send(Object data) {
        return executor.execute(data);
    }

    @Override
    public void stop() {
        try {
            closed = true;
            executor.stop();
        } finally {
            try {
                byteBufferOutputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void sendPacket(Object message) {
        if (closed) {
            throw new PinpointSocketException("NioUDPDataSender already closed.");
        }

        if (message instanceof TBase) {
            final TBase tBase = (TBase) message;
            sendPacket(tBase);
            return;
        }
        final TBase<?, ?> tBase = this.messageConverter.toMessage(message);
        if (tBase != null) {
            sendPacket(tBase);
            return;
        }
        logger.warn("sendPacket fail. invalid type:{}", message != null ? message.getClass() : null);
        if (logger.isDebugEnabled()) {
            logger.debug("unknown message:{}", message);
        }
    }

    private void sendPacket(TBase tBase) {
        byteBufferOutputStream.clear();
        // do not copy bytes because it's single threaded
        try {
            serializer.serialize(tBase,  byteBufferOutputStream);
        } catch (TException e) {
            throw new PinpointSocketException("Serialize " + tBase + " failed. Error:" +  e.getMessage(), e);
        }
        ByteBuffer byteBuffer = byteBufferOutputStream.getByteBuffer();
        int bufferSize = byteBuffer.remaining();
        try {
            datagramChannel.write(byteBuffer);
        } catch (IOException e) {
            final Thread currentThread = Thread.currentThread();
            if (currentThread.isInterrupted()) {
                logger.warn("{} thread interrupted.", currentThread.getName());
                throw new PinpointSocketException(currentThread.getName() + " thread interrupted.", e);
            } else {
                throw new PinpointSocketException("packet send error. size:" + bufferSize + ", " +  tBase, e);
            }
        }
    }

}
