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

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunk;
import com.navercorp.pinpoint.profiler.sender.planer.SendDataPlaner;
import com.navercorp.pinpoint.profiler.sender.planer.SpanChunkStreamSendDataPlaner;
import com.navercorp.pinpoint.profiler.util.ByteBufferUtils;
import com.navercorp.pinpoint.profiler.util.ObjectPool;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Iterator;

/**
 * @author Taejin Koo
 */
public class SpanStreamUdpSender extends AbstractDataSender {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int SOCKET_TIMEOUT = 1000 * 5;
    public static final int SEND_BUFFER_SIZE = 1024 * 64 * 16;
    public static final int DEFAULT_BUFFER_SIZE = 1024 * 16;
    
    public static final int UDP_MAX_PACKET_LENGTH = 65507;
    

    private final SpanStreamSendDataFactory spanStreamSendDataFactory;

    private final DatagramChannel udpChannel;
    private final AsyncQueueingExecutor<Object> executor;

    private final ObjectPool<HeaderTBaseSerializer> serializerPool;

    private final SpanStreamSendDataSerializer spanStreamSendDataSerializer;

    private final StandbySpanStreamDataSendWorker standbySpanStreamDataSendWorker;

    public SpanStreamUdpSender(String host, int port, String threadName, int queueSize) {
        this(host, port, threadName, queueSize, SOCKET_TIMEOUT, SEND_BUFFER_SIZE);
    }
    
    public SpanStreamUdpSender(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize) {
        this(host, port, threadName, queueSize, timeout, sendBufferSize, DEFAULT_BUFFER_SIZE);
    }

    public SpanStreamUdpSender(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize, int dataBufferSize) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        if (threadName == null) {
            throw new NullPointerException("threadName must not be null");
        }
        if (queueSize <= 0) {
            throw new IllegalArgumentException("queueSize");
        }
        if (timeout <= 0) {
            throw new IllegalArgumentException("timeout");
        }
        if (sendBufferSize <= 0) {
            throw new IllegalArgumentException("sendBufferSize");
        }

        // TODO If fail to create socket, stop agent start
        logger.info("UdpDataSender initialized. host={}, port={}", host, port);
        this.udpChannel = createChannel(host, port, timeout, sendBufferSize);

        HeaderTBaseSerializerPoolFactory headerTBaseSerializerPoolFactory = new HeaderTBaseSerializerPoolFactory(false, dataBufferSize, true);
        this.serializerPool = new ObjectPool<HeaderTBaseSerializer>(headerTBaseSerializerPoolFactory, 16);

        this.spanStreamSendDataSerializer = new SpanStreamSendDataSerializer();

        this.spanStreamSendDataFactory = new SpanStreamSendDataFactory(dataBufferSize, 16, serializerPool);

        this.standbySpanStreamDataSendWorker = new StandbySpanStreamDataSendWorker(new FlushHandler(), new StandbySpanStreamDataStorage());
        this.standbySpanStreamDataSendWorker.start();

        this.executor = createAsyncQueueingExecutor(queueSize, threadName);
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
            if (socket != null) {
                socket.close();
            }

            if (datagramChannel != null) {
                try {
                    datagramChannel.close();
                } catch (IOException ignored) {
                }
            }
            
            throw new IllegalStateException("DatagramChannel create fail. Cause" + e.getMessage(), e);
        }
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        return executor.execute(data);
    }

    @Override
    public void stop() {
        try {
            standbySpanStreamDataSendWorker.stop();
        } catch (Exception e) {
            logger.debug("Failed to stop standbySpanStreamDataSendWorker.", e);
        }

        try {
            udpChannel.close();
        } catch (IOException e) {
            logger.debug("Failed to close udp channel.", e);
        }

        executor.stop();
    }

    @Override
    protected void sendPacket(Object message) {
        if (logger.isDebugEnabled()) {
            logger.debug("sendPacket message:{}", message);
        }

        if (message instanceof Span) {
            handleSpan((Span) message);
        } else if (message instanceof SpanChunk) {
            handleSpanChunk((SpanChunk) message);
        } else {
            logger.info("sendPacket fail. invalid type:{}", messageToString(message));
        }
    }

    private String messageToString(Object message) {
        if(message == null) {
            return null;
        }
        return message.getClass().toString();
    }

    private void handleSpan(Span span) {
        if (span == null) {
            return;
        }

        HeaderTBaseSerializer serializer = serializerPool.getObject();
        PartitionedByteBufferLocator partitionedByteBufferLocator = spanStreamSendDataSerializer.serializeSpanStream(serializer, span);
        if (partitionedByteBufferLocator == null) {
            serializerPool.returnObject(serializer);
            return;
        }
        
        doAddAndFlush(partitionedByteBufferLocator, serializer);
    }

    // streaming
    private void handleSpanChunk(SpanChunk spanChunk) {
        if (spanChunk == null) {
            return;
        }

        HeaderTBaseSerializer serializer = serializerPool.getObject();
        PartitionedByteBufferLocator partitionedByteBufferLocator = spanStreamSendDataSerializer.serializeSpanChunkStream(serializer, spanChunk);
        if (partitionedByteBufferLocator == null) {
            serializerPool.returnObject(serializer);
            return;
        }

        doAddAndFlush(partitionedByteBufferLocator, serializer);
    }

    private void doAddAndFlush(PartitionedByteBufferLocator partitionedByteBufferLocator, HeaderTBaseSerializer serializer) {
        logger.debug("PartitionedByteBufferLocator {}.", partitionedByteBufferLocator);

        SpanStreamSendData currentSpanStreamSendData = standbySpanStreamDataSendWorker.getStandbySpanStreamSendData();
        if (currentSpanStreamSendData == null) {
            currentSpanStreamSendData = spanStreamSendDataFactory.create();
        }

        try {
            if (!currentSpanStreamSendData.addBuffer(partitionedByteBufferLocator.getByteBuffer())) {
                SendDataPlaner sendDataPlaner = new SpanChunkStreamSendDataPlaner(partitionedByteBufferLocator, spanStreamSendDataFactory);
                
                Iterator<SpanStreamSendData> sendDataIterator = sendDataPlaner.getSendDataIterator(currentSpanStreamSendData, serializer);
                while (sendDataIterator.hasNext()) {
                    SpanStreamSendData sendData = sendDataIterator.next();
                    if (sendData.getFlushMode() == SpanStreamSendDataMode.FLUSH) {
                        flush(sendData);
                    } else if (sendData.getFlushMode() == SpanStreamSendDataMode.WAIT_BUFFER) {
                        boolean isAdded = standbySpanStreamDataSendWorker.addStandbySpanStreamData(sendData);
                        if (!isAdded) {
                            flush(sendData);
                        }
                    }
                }
            } else {
                boolean isAdded = standbySpanStreamDataSendWorker.addStandbySpanStreamData(currentSpanStreamSendData);
                if (!isAdded) {
                    flush(currentSpanStreamSendData);
                }
            }
        } catch (IOException e) {
            logger.warn("UDPChannel write fail.", e);
        }
    }

    private void flush(SpanStreamSendData spanStreamSendData) throws IOException {
        if (spanStreamSendData == null) {
            return;
        }
        
        ByteBuffer[] byteBuffers = spanStreamSendData.getSendBuffers();
        int remainingLength = ByteBufferUtils.getRemaining(byteBuffers);

        try {
            if (remainingLength != 0) {
                long sentBufferSize = udpChannel.write(byteBuffers);
                if (remainingLength != sentBufferSize) {
                    logger.warn("sent buffer {}/{}.", sentBufferSize, remainingLength);
                } else {
                    logger.debug("Data sent. size:{}, {}", sentBufferSize);
                }
            }
        } finally {
            spanStreamSendData.done();
        }
    }

    class FlushHandler implements StandbySpanStreamDataFlushHandler {

        @Override
        public void handleFlush(SpanStreamSendData spanStreamSendData) {
            if (spanStreamSendData == null) {
                return;
            }

            try {
                ByteBuffer[] byteBuffers = spanStreamSendData.getSendBuffers();
                int remainingLength = ByteBufferUtils.getRemaining(byteBuffers);

                if (remainingLength != 0) {
                    long sentBufferSize = udpChannel.write(byteBuffers);
                    if (remainingLength != sentBufferSize) {
                        logger.warn("sent buffer {}/{}.", sentBufferSize, remainingLength);
                    } else {
                        // TODO need check ????
                    }
                       
                }
            } catch (IOException e) {
                logger.warn("Failed to flush span stream data.", e);
            } finally {
                spanStreamSendData.done();
            }
        }

        @Override
        public void exceptionCaught(SpanStreamSendData spanStreamSendData, Throwable e) {
            logger.warn("Failed to flush span stream data.", e);
        }

    }

}
