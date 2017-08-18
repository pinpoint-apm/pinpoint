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

package com.navercorp.pinpoint.collector.receiver.udp;

import com.navercorp.pinpoint.collector.receiver.DataReceiver;
import com.navercorp.pinpoint.collector.receiver.DispatchWorker;
import com.navercorp.pinpoint.collector.receiver.DispatchWorkerOption;
import com.navercorp.pinpoint.collector.util.PooledObject;
import org.apache.hadoop.hbase.shaded.org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void startStop() {
        DataReceiver receiver = null;

        DispatchWorker mockWorker = mock(DispatchWorker.class);
        try {
            receiver = new UDPReceiver("test", new PacketHandlerFactory() {
                @Override
                public PacketHandler createPacketHandler() {
                    return null;
                }
            }, ADDRESS, PORT, 8, mockWorker);
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            Assert.fail(e.getMessage());
        } finally {
            if (receiver!= null) {
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

        datagramSocket.setReceiveBufferSize(64*1024*10);
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


    @Test
    public void datagramPacket_length_zero() {
        DataReceiver receiver = null;
        DatagramSocket datagramSocket = null;

        CountDownLatch latch = new CountDownLatch(2);
        DispatchWorkerOption option = new DispatchWorkerOption("test", 1, 10);
        DispatchWorker mockWorker = mockDispatchWorker(latch, option);
        PacketHandlerFactory packetHandlerFactory = mock(PacketHandlerFactory.class);

        try {
            receiver = new UDPReceiver("test", packetHandlerFactory, ADDRESS, PORT, 8, mockWorker) {
                private int readCount = 0;
                @Override
                protected PooledObject<DatagramPacket> read0(DatagramSocket socket) {
                    if (readCount == 0) {
                        readCount++;
                        PooledObject mock = mock(PooledObject.class);
                        when(mock.getObject()).thenReturn(new DatagramPacket(new byte[0], 0));
                        return mock;
                    }
                    return super.read0(socket);
                }
            };
            receiver.start();

            datagramSocket = new DatagramSocket();
            datagramSocket.connect(new InetSocketAddress(ADDRESS, PORT));

            // Size 0 is not send in java, but L4 is possible.
            datagramSocket.send(new DatagramPacket(new byte[0], 0));
            datagramSocket.send(new DatagramPacket(new byte[1], 1));

            latch.await(3000, TimeUnit.MILLISECONDS);
            Mockito.verify(mockWorker).execute(any(Runnable.class));
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            Assert.fail(e.getMessage());
        } finally {
            if (receiver!= null) {
                receiver.shutdown();
            }
            IOUtils.closeQuietly(datagramSocket);
        }
    }

    private DispatchWorker mockDispatchWorker(CountDownLatch latch, DispatchWorkerOption option) {
        DispatchWorker mockWorker = new DispatchWorker(option) {
            @Override
            public void execute(Runnable runnable) {
                logger.info("execute:{}", runnable.getClass());
                latch.countDown();
            }
        };
        return Mockito.spy(mockWorker);
    }


}
