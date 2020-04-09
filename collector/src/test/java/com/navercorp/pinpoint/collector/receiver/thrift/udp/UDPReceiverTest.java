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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.google.common.util.concurrent.MoreExecutors;
import com.navercorp.pinpoint.collector.util.DatagramPacketFactory;
import com.navercorp.pinpoint.collector.util.DefaultObjectPool;
import com.navercorp.pinpoint.collector.util.ObjectPool;
import com.navercorp.pinpoint.collector.util.ObjectPoolFactory;
import com.navercorp.pinpoint.common.util.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author emeroad
 */
public class UDPReceiverTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = SocketUtils.findAvailableUdpPort(10999);

    private final PacketHandler loggingPacketHandler = new PacketHandler() {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        @Override
        public void receive(DatagramSocket localSocket, Object packet) {
            logger.info("receive localSocket:{} packet:{}", localSocket, packet);
        }
    };

    @Test
    public void startStop() {
        UDPReceiver receiver = null;

        InetSocketAddress bindAddress = new InetSocketAddress(ADDRESS, PORT);

        Executor executor = MoreExecutors.directExecutor();
        PacketHandlerFactory packetHandlerFactory = mock(PacketHandlerFactory.class);
        when(packetHandlerFactory.createPacketHandler()).thenReturn(loggingPacketHandler);
        try {
            ObjectPoolFactory<DatagramPacket> packetFactory = new DatagramPacketFactory();
            ObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(packetFactory, 10);
            receiver = new UDPReceiver("test", packetHandlerFactory, executor, 8, bindAddress, pool);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            Assert.fail(e.getMessage());
        } finally {
            if (receiver != null) {
                receiver.shutdown();
            }
        }
    }

    @Test
    public void hostNullCheck() {
        InetSocketAddress address = new InetSocketAddress((InetAddress) null, PORT);
        logger.debug(address.toString());
    }

    @Test
    public void socketBufferSize() throws SocketException {
        DatagramSocket datagramSocket = new DatagramSocket();
        int receiveBufferSize = datagramSocket.getReceiveBufferSize();
        logger.debug("{}", receiveBufferSize);

        datagramSocket.setReceiveBufferSize(64 * 1024 * 10);
        logger.debug("{}", datagramSocket.getReceiveBufferSize());

        datagramSocket.close();
    }

    @Test
    public void sendSocketBufferSize() throws IOException {
        DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0, 0);

        DatagramSocket datagramSocket = new DatagramSocket();
        datagramSocket.connect(new InetSocketAddress(ADDRESS, PORT));

        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }

    private final AtomicInteger zeroPacketCounter = new AtomicInteger();
    void interceptValidatePacket(DatagramPacket packet) {
        if (packet.getLength() == 0) {
            zeroPacketCounter.incrementAndGet();
        }
    }

    @Test
    public void datagramPacket_length_zero() {
        UDPReceiver receiver = null;
        DatagramSocket datagramSocket = null;

        CountDownLatch latch = new CountDownLatch(1);
        Executor mockExecutor = mockDispatchWorker(latch);

        PacketHandlerFactory packetHandlerFactory = mock(PacketHandlerFactory.class);
        when(packetHandlerFactory.createPacketHandler()).thenReturn(loggingPacketHandler);

        try {
            InetSocketAddress bindAddress = new InetSocketAddress(ADDRESS, PORT);
            ObjectPoolFactory<DatagramPacket> packetFactory = new DatagramPacketFactory();
            ObjectPool<DatagramPacket> pool = new DefaultObjectPool<>(packetFactory, 10);
            receiver = new UDPReceiver("test", packetHandlerFactory, mockExecutor, 8, bindAddress, pool) {
                @Override
                boolean validatePacket(DatagramPacket packet) {
                    interceptValidatePacket(packet);
                    return super.validatePacket(packet);
                }
            };
            receiver.start();

            datagramSocket = new DatagramSocket();
            datagramSocket.connect(new InetSocketAddress(ADDRESS, PORT));

            datagramSocket.send(new DatagramPacket(new byte[0], 0));
            datagramSocket.send(new DatagramPacket(new byte[1], 1));

            Assert.assertTrue(latch.await(30000, TimeUnit.MILLISECONDS));
            Assert.assertEquals(zeroPacketCounter.get(), 1);
            Mockito.verify(mockExecutor).execute(any(Runnable.class));
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            Assert.fail(e.getMessage());
        } finally {
            if (receiver != null) {
                receiver.shutdown();
            }
            IOUtils.closeQuietly((Closeable) datagramSocket);
        }
    }

    private Executor mockDispatchWorker(CountDownLatch latch) {

        Executor mockWorker = new Executor() {
            @Override
            public void execute(Runnable runnable) {
                logger.info("execute:{}", runnable.getClass());
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            }
        };
        return Mockito.spy(mockWorker);
    }


}
