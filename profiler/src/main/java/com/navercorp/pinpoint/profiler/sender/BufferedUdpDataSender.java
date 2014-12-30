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

import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.ChunkHeaderBufferedTBaseSerializerFlushHandler;

/**
 * split & buffering
 * 
 * only use pair collector-ChunkedUDPReceiver
 * 
 * @author jaehong.kim
 *
 */
public class BufferedUdpDataSender extends UdpDataSender {
    private static final int CHUNK_SIZE = 1024 * 16;

    private static final String SCHEDULED_FLUSH = "BufferedUdpDataSender-ScheduledFlush";

    private final ChunkHeaderBufferedTBaseSerializer chunkHeaderBufferedSerializer = new ChunkHeaderBufferedTBaseSerializerFactory().createSerializer();

    private final Thread flushThread;


    public BufferedUdpDataSender(String host, int port, String threadName, int queueSize) {
        this(host, port, threadName, queueSize, SOCKET_TIMEOUT, SEND_BUFFER_SIZE, CHUNK_SIZE);
    }

    public BufferedUdpDataSender(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize, int chunkSize) {
        super(host, port, threadName, queueSize, timeout, sendBufferSize);

        chunkHeaderBufferedSerializer.setChunkSize(chunkSize);
        chunkHeaderBufferedSerializer.setFlushHandler(new ChunkHeaderBufferedTBaseSerializerFlushHandler() {
            @Override
            public void handle(byte[] buffer, int offset, int length) {
                if (buffer == null) {
                    logger.warn("interBufferData is null");
                    return;
                }

                final int internalBufferSize = length;
                if (isLimit(internalBufferSize)) {
                    logger.warn("discard packet. Caused:too large message. size:{}", internalBufferSize);
                    return;
                }
                // We can reuse this because this runs in single thread
                reusePacket.setData(buffer, 0, internalBufferSize);

                try {
                    udpSocket.send(reusePacket);
                    if (isDebug) {
                        logger.debug("Data sent. {size={}}", internalBufferSize);
                    }
                } catch (IOException e) {
                    logger.warn("packet send error. size:{}", internalBufferSize, e);
                }
            }
        });

        flushThread = startScheduledFlush();
    }

    // for test
    String getFlushThreadName() {
        return flushThread.getName();
    }

    private Thread startScheduledFlush() {
        final ThreadFactory threadFactory = new PinpointThreadFactory(SCHEDULED_FLUSH, true);
        final Thread thread = threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                final Thread currentThread = Thread.currentThread();
                while (!currentThread.isInterrupted()) {
                    try {
                        chunkHeaderBufferedSerializer.flush();
                    } catch (TException e) {
                        logger.warn("Failed to flush. caused={}", e.getMessage(), e);
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException ignored) {
                        currentThread.interrupt();
                    }
                }
                logger.info("stop ScheduledFlush {} - {}", currentThread.getName(), currentThread.getId());
            }
        });
        logger.info("stop ScheduledFlush {} - {}", thread.getName(), thread.getId());
        thread.start();
        return thread;
    }


    @Override
    protected void sendPacket(Object message) {
        if (message instanceof TBase) {
            try {
                final TBase<?, ?> packet = (TBase<?, ?>) message;
                chunkHeaderBufferedSerializer.add(packet);
                logger.debug("Send packet {}", packet);
            } catch (TException e) {
                logger.warn("sendPacket fail.", e);
            }
        } else {
            logger.warn("sendPacket fail. invalid type:{}", message != null ? message.getClass() : null);
            return;
        }
    }

    @Override
    public void stop() {
        super.stop();
        stopFlushThread();
    }

    private void stopFlushThread() {
        final Thread flushThread = this.flushThread;
        // terminate thread
        flushThread.interrupt();
        try {
            flushThread.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}