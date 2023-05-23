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

package com.navercorp.pinpoint.rpc.client;

import com.navercorp.pinpoint.io.ResponseMessage;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.jboss.netty.channel.ChannelFuture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author emeroad
 */
public class PinpointClientFactoryTest {
    private Logger logger = LogManager.getLogger(this.getClass());

    private static DefaultPinpointClientFactory clientFactory;

    @BeforeAll
    public static void setUp() throws IOException {
        clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setPingDelay(100);
    }

    @AfterAll
    public static void tearDown() {
        if (clientFactory != null) {
            clientFactory.release();
        }
    }

    @Test
    public void connectFail() {
        Assertions.assertThrowsExactly(PinpointSocketException.class, () -> {
            int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
            clientFactory.connect("127.0.0.1", availableTcpPort);
        });
    }

    @Test
    public void reconnectFail() throws InterruptedException {
        // confirm simplified error message when api called.
        int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", availableTcpPort);
        ChannelFuture reconnect = clientFactory.reconnect(remoteAddress);
        reconnect.await();
        Assertions.assertFalse(reconnect.isSuccess());
        assertThat(reconnect.getCause()).isInstanceOf(ConnectException.class);

        Thread.sleep(1000);
    }

    @Test
    public void connect() throws IOException, InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);
            PinpointRPCTestUtils.close(client);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void pingInternal() {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        final TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            Awaitility.await()
                    .pollDelay(100, TimeUnit.MILLISECONDS)
                    .timeout(3000, TimeUnit.MILLISECONDS)
                    .until(new Callable<Boolean>() {
                        @Override
                        public Boolean call() {
                            return serverMessageListener.hasReceivedPing();
                        }
                    });
            PinpointRPCTestUtils.close(client);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void ping() throws IOException, InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);
            client.sendPing();
            PinpointRPCTestUtils.close(client);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void pingAndRequestResponse() {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX));
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assertions.assertArrayEquals(randomByte, response);
            PinpointRPCTestUtils.close(client);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void sendSync() {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);
            logger.debug("send1");
            client.send(new byte[20]);
            logger.debug("send2");
            client.sendSync(new byte[20]);

            PinpointRPCTestUtils.close(client);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void requestAndResponse() {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX));
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            byte[] randomByte = TestByteUtils.createRandomByte(20);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assertions.assertArrayEquals(randomByte, response);
            PinpointRPCTestUtils.close(client);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void connectTimeout() {
        int timeout = 1000;

        PinpointClientFactory pinpointClientFactory = null;
        try {
            pinpointClientFactory = new DefaultPinpointClientFactory();
            pinpointClientFactory.setConnectTimeout(timeout);
            int connectTimeout = pinpointClientFactory.getConnectTimeout();

            Assertions.assertEquals(timeout, connectTimeout);
        } finally {
            pinpointClientFactory.release();
        }
    }

    @Disabled
    @Test
    public void throwWriteBufferFullExceptionTest() {
        Assertions.assertThrows(PinpointSocketException.class, () -> {
            TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
            int bindPort = testPinpointServerAcceptor.bind();

            int defaultWriteBufferHighWaterMark = clientFactory.getWriteBufferHighWaterMark();
            int defaultWriteBufferLowWaterMark = clientFactory.getWriteBufferLowWaterMark();
            try {
                clientFactory.setWriteBufferHighWaterMark(2);
                clientFactory.setWriteBufferLowWaterMark(1);

                PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

                List<CompletableFuture<ResponseMessage>> futureList = new ArrayList<>();
                for (int i = 0; i < 30; i++) {
                    CompletableFuture<ResponseMessage> requestFuture = client.request(new byte[20]);
                    futureList.add(requestFuture);
                }

                CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
                for (CompletableFuture<?> future : futureList) {
                    future.get(3000, TimeUnit.MILLISECONDS);
                }

                PinpointRPCTestUtils.close(client);
            } finally {
                clientFactory.setWriteBufferHighWaterMark(defaultWriteBufferHighWaterMark);
                clientFactory.setWriteBufferLowWaterMark(defaultWriteBufferLowWaterMark);

                testPinpointServerAcceptor.close();
            }
        });
    }


}
