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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author emeroad
 */
//@Ignore
public class ReconnectTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static PinpointClientFactory clientFactory;

    private final TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX);


    @BeforeAll
    public static void setUp() throws IOException {
        clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setReconnectDelay(200);
        clientFactory.setPingDelay(100);
        clientFactory.setWriteTimeoutMillis(200);
        clientFactory.setRequestTimeoutMillis(200);
    }

    @AfterAll
    public static void tearDown() {
        if (clientFactory != null) {
            clientFactory.release();
        }
    }

    @Test
    public void reconnect() throws IOException, InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
        int bindPort = testPinpointServerAcceptor.bind();

        final AtomicBoolean reconnectPerformed = new AtomicBoolean(false);

        TestPinpointServerAcceptor newTestPinpointServerAcceptor = null;
        try {
            PinpointClient client = clientFactory.connect("localhost", bindPort);
            client.addPinpointClientReconnectEventListener(new Consumer<PinpointClient>() {

                @Override
                public void accept(PinpointClient client) {
                    reconnectPerformed.set(true);
                }

            });

            testPinpointServerAcceptor.close();
            logger.debug("server.close");
            assertClientDisconnected(client);

            newTestPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            newTestPinpointServerAcceptor.bind(bindPort);
            logger.debug("bind server");
            assertClientConnected(client);

            logger.debug("request server");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assertions.assertArrayEquals(randomByte, response);

            PinpointRPCTestUtils.close(client);
        } finally {
            TestPinpointServerAcceptor.staticClose(newTestPinpointServerAcceptor);
        }

        Assertions.assertTrue(reconnectPerformed.get());
    }

    // it takes very long time. 
    // @Test
    @Disabled
    @SuppressWarnings("unused")
    public void reconnectStressTest() throws IOException, InterruptedException {
        int count = 3;

        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();

        int threadCount = tbean.getThreadCount();
        for (int i = 0; i < count; i++) {
            logger.debug((i + 1) + "th's start.");

            TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
            int bindPort = testPinpointServerAcceptor.bind();

            PinpointClient client = clientFactory.connect("localhost", bindPort);

            testPinpointServerAcceptor.close();
            logger.debug("server.close");
            assertClientDisconnected(client);

            testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            logger.debug("bind server");
            assertClientConnected(client);

            logger.debug("request server");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assertions.assertArrayEquals(randomByte, response);

            PinpointRPCTestUtils.close(client);
            testPinpointServerAcceptor.close();
        }

        Thread.sleep(10000);

        Assertions.assertEquals(threadCount, tbean.getThreadCount());
    }


    @Test
    public void scheduledConnect() {
        final PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setReconnectDelay(200);
        PinpointClient client = null;

        TestPinpointServerAcceptor testPinpointServerAcceptor = null;
        try {
            int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
            client = clientFactory.scheduledConnect("localhost", availableTcpPort);

            testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
            testPinpointServerAcceptor.bind(availableTcpPort);
            assertClientConnected(client);

            logger.debug("request server");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assertions.assertArrayEquals(randomByte, response);
        } finally {
            PinpointRPCTestUtils.close(client);
            clientFactory.release();
            TestPinpointServerAcceptor.staticClose(testPinpointServerAcceptor);
        }
    }

    @Test
    public void scheduledConnectAndClosed() throws IOException, InterruptedException {
        int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
        PinpointClient client = clientFactory.scheduledConnect("localhost", availableTcpPort);

        logger.debug("close");
        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void scheduledConnectDelayAndClosed() throws IOException, InterruptedException {
        int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
        PinpointClient client = clientFactory.scheduledConnect("localhost", availableTcpPort);

        Thread.sleep(2000);
        logger.debug("close pinpoint client");
        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void scheduledConnectStateTest() {
        int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
        PinpointClient client = clientFactory.scheduledConnect("localhost", availableTcpPort);

        client.send(new byte[10]);

        try {
            CompletableFuture<Void> future = client.sendAsync(new byte[10]);
            future.get(3000, TimeUnit.MILLISECONDS);
        } catch (Throwable ignored) {
        }

        try {
            client.sendSync(new byte[10]);
            Assertions.fail();
        } catch (PinpointSocketException ignored) {
        }

        try {
            PinpointRPCTestUtils.request(client, new byte[10]);
            Assertions.fail();
        } catch (PinpointSocketException ignored) {
        }

        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void serverFirstClose() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a socket should be closed properly.
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

        byte[] randomByte = TestByteUtils.createRandomByte(10);
        CompletableFuture<ResponseMessage> response = client.request(randomByte);
        try {
            response.get(3000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.debug("timeout.", e);
        }
        // close server by force
        testPinpointServerAcceptor.close();
        assertClientDisconnected(client);
        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void serverCloseAndWrite() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a client socket should be closed properly.
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
        int bindPort = testPinpointServerAcceptor.bind();

        PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

        // just close server and request
        testPinpointServerAcceptor.close();

        byte[] randomByte = TestByteUtils.createRandomByte(10);
        CompletableFuture<ResponseMessage> response = client.request(randomByte);
        try {
            response.get(3000, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
        }

        assertClientDisconnected(client);
        PinpointRPCTestUtils.close(client);
    }

    private void assertClientDisconnected(final PinpointClient client) {
        Awaitility.await("assertClientDisconnected")
                .untilAsserted(() -> assertThat(isConnected(client).call()).isFalse());
    }

    private void assertClientConnected(final PinpointClient client) {
        Awaitility.await("assertClientConnected")
                .until(isConnected(client));
    }

    private Callable<Boolean> isConnected(final PinpointClient client) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return client.isConnected();
            }
        };
    }

}
