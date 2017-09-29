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

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.TestAwaitTaskUtils;
import com.navercorp.pinpoint.rpc.TestAwaitUtils;
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.SimpleServerMessageListener;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author emeroad
 */
//@Ignore
public class ReconnectTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static int bindPort;
    private static PinpointClientFactory clientFactory;

    private final TestAwaitUtils awaitUtils = new TestAwaitUtils(100, 1000);

    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = SocketUtils.findAvailableTcpPort();
        
        clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setReconnectDelay(200);
        clientFactory.setPingDelay(100);
        clientFactory.setTimeoutMillis(200);
    }
    
    @AfterClass
    public static void tearDown() {
        if (clientFactory != null) {
            clientFactory.release();
        }
    }


    @Test
    public void reconnect() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE);
        
        final AtomicBoolean reconnectPerformed = new AtomicBoolean(false);

        PinpointServerAcceptor newServerAcceptor = null;
        try {
            PinpointClient client = clientFactory.connect("localhost", bindPort);
            client.addPinpointClientReconnectEventListener(new PinpointClientReconnectEventListener() {

                @Override
                public void reconnectPerformed(PinpointClient client) {
                    reconnectPerformed.set(true);
                }

            });
            
            PinpointRPCTestUtils.close(serverAcceptor);
            logger.debug("server.close");
            assertClientDisconnected(client);

            newServerAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE);
            logger.debug("bind server");
            assertClientConnected(client);

            logger.debug("request server");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);
            
            Assert.assertArrayEquals(randomByte, response);
            
            PinpointRPCTestUtils.close(client);
        } finally {
            PinpointRPCTestUtils.close(newServerAcceptor);
        }
        
        Assert.assertTrue(reconnectPerformed.get());
    }
    
    // it takes very long time. 
    // @Test
    @Ignore
    public void reconnectStressTest() throws IOException, InterruptedException {
        int count = 3;
        
        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();

        int threadCount = tbean.getThreadCount();
        for (int i = 0; i < count; i++) {
            logger.debug((i + 1) + "th's start.");
            
            PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE);
            PinpointClient client = clientFactory.connect("localhost", bindPort);

            PinpointRPCTestUtils.close(serverAcceptor);
            logger.debug("server.close");
            assertClientDisconnected(client);

            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE);
            logger.debug("bind server");
            assertClientConnected(client);

            logger.debug("request server");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assert.assertArrayEquals(randomByte, response);

            PinpointRPCTestUtils.close(client);
            PinpointRPCTestUtils.close(serverAcceptor);
        }
        
        Thread.sleep(10000);

        Assert.assertEquals(threadCount, tbean.getThreadCount());
    }


    @Test
    public void scheduledConnect() throws IOException, InterruptedException {
        final PinpointClientFactory clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setReconnectDelay(200);
        PinpointClient client = null;
        PinpointServerAcceptor serverAcceptor = null;
        try {
            client = clientFactory.scheduledConnect("localhost", bindPort);

            serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, SimpleServerMessageListener.DUPLEX_ECHO_INSTANCE);
            assertClientConnected(client);

            logger.debug("request server");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assert.assertArrayEquals(randomByte, response);
        } finally {
            PinpointRPCTestUtils.close(client);
            clientFactory.release();
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void scheduledConnectAndClosed() throws IOException, InterruptedException {
        PinpointClient client = clientFactory.scheduledConnect("localhost", bindPort);

        logger.debug("close");
        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void scheduledConnectDelayAndClosed() throws IOException, InterruptedException {
        PinpointClient client = clientFactory.scheduledConnect("localhost", bindPort);

        Thread.sleep(2000);
        logger.debug("close pinpoint client");
        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void scheduledConnectStateTest() {
        PinpointClient client = clientFactory.scheduledConnect("localhost", bindPort);

        client.send(new byte[10]);

        try {
            Future future = client.sendAsync(new byte[10]);
            future.await();
            future.getResult();
            Assert.fail();
        } catch (PinpointSocketException e) {
        }

        try {
            client.sendSync(new byte[10]);
            Assert.fail();
        } catch (PinpointSocketException e) {
        }

        try {
            PinpointRPCTestUtils.request(client, new byte[10]);
            Assert.fail();
        } catch (PinpointSocketException e) {
        }

        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void serverFirstClose() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a socket should be closed properly.
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort);
        PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

        byte[] randomByte = TestByteUtils.createRandomByte(10);
        Future<ResponseMessage> response = client.request(randomByte);
        response.await();
        try {
            response.getResult();
        } catch (Exception e) {
            logger.debug("timeout.", e);
        }
        // close server by force
        PinpointRPCTestUtils.close(serverAcceptor);
        assertClientDisconnected(client);
        PinpointRPCTestUtils.close(client);
    }

    @Test
    public void serverCloseAndWrite() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a client socket should be closed properly.
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort);
        
        PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

        // just close server and request
        PinpointRPCTestUtils.close(serverAcceptor);

        byte[] randomByte = TestByteUtils.createRandomByte(10);
        Future<ResponseMessage> response = client.request(randomByte);
        response.await();
        try {
            response.getResult();
            Assert.fail("expected exception");
        } catch (Exception e) {
        }

        assertClientDisconnected(client);
        PinpointRPCTestUtils.close(client);
    }

    private void assertClientDisconnected(final PinpointClient client) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return !client.isConnected();
            }
        });

        Assert.assertTrue(pass);
    }

    private void assertClientConnected(final PinpointClient client) {
        boolean pass = awaitUtils.await(new TestAwaitTaskUtils() {
            @Override
            public boolean checkCompleted() {
                return client.isConnected();
            }
        });

        Assert.assertTrue(pass);
    }

}
