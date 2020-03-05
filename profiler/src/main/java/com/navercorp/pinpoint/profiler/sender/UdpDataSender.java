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

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.client.DnsSocketAddressProvider;
import com.navercorp.pinpoint.rpc.client.SocketAddressProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;

/**
 * @author netspider
 * @author emeroad
 * @author koo.taejin
 */
public class UdpDataSender implements DataSender {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    // Caution. not thread safe
    private final DatagramPacket reusePacket = new DatagramPacket(new byte[1], 1);

    private final DatagramSocket udpSocket;

    private final AsyncQueueingExecutor<Object> executor;

    private final UdpSocketAddressProvider socketAddressProvider;

    private final MessageSerializer<ByteMessage> messageSerializer;


    public UdpDataSender(String host, int port, String threadName,
                         int queueSize, int timeout, int sendBufferSize,
                         MessageSerializer<ByteMessage> messageSerializer) {
        Assert.requireNonNull(host, "host");
        if (!HostAndPort.isValidPort(port)) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        Assert.requireNonNull(host, "host");
        Assert.isTrue(queueSize > 0, "queueSize");
        Assert.isTrue(timeout > 0, "timeout");
        Assert.isTrue(sendBufferSize > 0, "sendBufferSize");

        this.messageSerializer = Assert.requireNonNull(messageSerializer, "messageSerializer");

        final SocketAddressProvider socketAddressProvider = new DnsSocketAddressProvider(host, port);
        this.socketAddressProvider = new RefreshStrategy(socketAddressProvider);
        final InetSocketAddress currentAddress = this.socketAddressProvider.resolve();
        logger.info("UdpDataSender initialized. host={}", currentAddress);
        // TODO If fail to create socket, stop agent start
        this.udpSocket = createSocket(timeout, sendBufferSize);

        this.executor = createAsyncQueueingExecutor(queueSize, threadName);

    }

    @Override
    public boolean send(Object data) {
        return executor.execute(data);
    }

    private AsyncQueueingExecutor<Object> createAsyncQueueingExecutor(int queueSize, String executorName) {
        AsyncQueueingExecutorListener<Object> listener = new DefaultAsyncQueueingExecutorListener() {
            @Override
            public void execute(Object message) {
                UdpDataSender.this.sendPacket(message);
            }
        };
        final AsyncQueueingExecutor<Object> executor = new AsyncQueueingExecutor<Object>(queueSize, executorName, listener);
        return executor;
    }

    @Override
    public void stop() {
        executor.stop();
    }

    private DatagramSocket createSocket(int timeout, int sendBufferSize) {
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

            return datagramSocket;
        } catch (SocketException e) {
            throw new IllegalStateException("DatagramSocket create fail. Cause" + e.getMessage(), e);
        }
    }

    private void sendPacket(Object message) {

        final InetSocketAddress inetSocketAddress = socketAddressProvider.resolve();
        if (inetSocketAddress.getAddress() == null) {
            logger.info("dns lookup fail host:{}", inetSocketAddress);
            return;
        }

        final ByteMessage byteMessage = messageSerializer.serializer(message);
        if (byteMessage == null) {
            logger.warn("sendPacket fail. message:{}", message != null ? message.getClass() : null);
            if (logger.isDebugEnabled()) {
                logger.debug("unknown message:{}", message);
            }
            return;
        }
        final DatagramPacket packet = preparePacket(inetSocketAddress, byteMessage);

        try {
            udpSocket.send(packet);
            if (isDebug) {
                logger.debug("Data sent. size:{}, {}", byteMessage.getLength(), message);
            }
        } catch (PortUnreachableException pe) {
            this.socketAddressProvider.handlePortUnreachable();
            logger.info("packet send error. size:{}, {}", byteMessage.getLength(), message, pe);
        } catch (IOException e) {
            logger.info("packet send error. size:{}, {}", byteMessage.getLength(), message, e);
        }

    }

    private DatagramPacket preparePacket(InetSocketAddress targetAddress, ByteMessage byteMessage) {
        // it's safe to reuse because it's single threaded
        reusePacket.setAddress(targetAddress.getAddress());
        reusePacket.setPort(targetAddress.getPort());

        reusePacket.setData(byteMessage.getMessage(), 0, byteMessage.getLength());
        return reusePacket;
    }

}
