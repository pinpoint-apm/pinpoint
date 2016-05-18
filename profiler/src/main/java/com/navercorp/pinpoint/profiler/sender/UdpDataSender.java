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

import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * @author netspider
 * @author emeroad
 * @author koo.taejin
 */
public class UdpDataSender extends AbstractDataSender implements DataSender {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    public static final int SOCKET_TIMEOUT = 1000 * 5;
    public static final int SEND_BUFFER_SIZE = 1024 * 64 * 16;
    public static final int UDP_MAX_PACKET_LENGTH = 65507;

    // Caution. not thread safe
    protected final DatagramPacket reusePacket = new DatagramPacket(new byte[1], 1);

    protected final DatagramSocket udpSocket;

    // Caution. not thread safe
    private final HeaderTBaseSerializer serializer = new HeaderTBaseSerializerFactory(false, UDP_MAX_PACKET_LENGTH, false).createSerializer();

    private final AsyncQueueingExecutor<Object> executor;

    public UdpDataSender(String host, int port, String threadName, int queueSize) {
        this(host, port, threadName, queueSize, SOCKET_TIMEOUT, SEND_BUFFER_SIZE);
    }

    public UdpDataSender(String host, int port, String threadName, int queueSize, int timeout, int sendBufferSize) {
        if (host == null ) {
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
        this.udpSocket = createSocket(host, port, timeout, sendBufferSize);

        this.executor = createAsyncQueueingExecutor(queueSize, threadName);
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        return executor.execute(data);
    }

    @Override
    public void stop() {
        executor.stop();
    }

    private DatagramSocket createSocket(String host, int port, int timeout, int sendBufferSize) {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket();

            datagramSocket.setSoTimeout(timeout);
            datagramSocket.setSendBufferSize(sendBufferSize);
            if (logger.isInfoEnabled()) {
                final int checkSendBufferSize = datagramSocket.getSendBufferSize();
                if (sendBufferSize != checkSendBufferSize) {
                    logger.info("DatagramSocket.setSendBufferSize() error. {}!={}", sendBufferSize, checkSendBufferSize);
                }
            }

            final InetSocketAddress serverAddress = new InetSocketAddress(host, port);
            datagramSocket.connect(serverAddress);
            return datagramSocket;
        } catch (SocketException e) {
            throw new IllegalStateException("DatagramSocket create fail. Cause" + e.getMessage(), e);
        }
    }

    protected void sendPacket(Object message) {
        if (message instanceof TBase) {
            final TBase dto = (TBase) message;
            // do not copy bytes because it's single threaded
            final byte[] internalBufferData = serialize(this.serializer, dto);
            if (internalBufferData == null) {
                logger.warn("interBufferData is null");
                return;
            }

            final int internalBufferSize = this.serializer.getInterBufferSize();
            if (isLimit(internalBufferSize)) {
                // When packet size is greater than UDP packet size limit, it's better to discard packet than let the socket API fails.
                logger.warn("discard packet. Caused:too large message. size:{}, {}", internalBufferSize, dto);
                return;
            }
            // it's safe to reuse because it's single threaded
            reusePacket.setData(internalBufferData, 0, internalBufferSize);

            try {
                udpSocket.send(reusePacket);
                if (isDebug) {
                    logger.debug("Data sent. size:{}, {}", internalBufferSize, dto);
                }
            } catch (IOException e) {
                logger.info("packet send error. size:{}, {}", internalBufferSize, dto, e);
            }
        } else {
            logger.warn("sendPacket fail. invalid type:{}", message != null ? message.getClass() : null);
            return;
        }
    }

    // for test
    protected boolean isLimit(int interBufferSize) {
        if (interBufferSize > UDP_MAX_PACKET_LENGTH) {
            return true;
        }
        return false;
    }
}
