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
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;
import com.navercorp.pinpoint.test.server.TestPinpointServerAcceptor;
import com.navercorp.pinpoint.test.server.TestServerMessageListenerFactory;
import com.navercorp.pinpoint.test.utils.TestAwaitTaskUtils;
import com.navercorp.pinpoint.test.utils.TestAwaitUtils;
import org.jboss.netty.channel.ChannelFuture;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


/**
 * @author emeroad
 */
public class PinpointClientFactoryTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static DefaultPinpointClientFactory clientFactory;
    
    @BeforeClass
    public static void setUp() throws IOException {
        clientFactory = new DefaultPinpointClientFactory();
        clientFactory.setPingDelay(100);
    }
    
    @AfterClass
    public static void tearDown() {
        if (clientFactory != null) {
            clientFactory.release();
        }
    }

    @Test
    public void connectFail() {
        try {
            int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
            clientFactory.connect("127.0.0.1", availableTcpPort);
            Assert.fail();
        } catch (PinpointSocketException e) {
            Assert.assertTrue(ConnectException.class.isInstance(e.getCause()));
        } 
    }

    @Test
    public void reconnectFail() throws InterruptedException {
        // confirm simplified error message when api called.
        int availableTcpPort = SocketUtils.findAvailableTcpPort(47000);
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", availableTcpPort);
        ChannelFuture reconnect = clientFactory.reconnect(remoteAddress);
        reconnect.await();
        Assert.assertFalse(reconnect.isSuccess());
        Assert.assertTrue(ConnectException.class.isInstance(reconnect.getCause()));
        
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
    public void pingInternal() throws IOException, InterruptedException {
        TestServerMessageListenerFactory testServerMessageListenerFactory = new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX, true);
        final TestServerMessageListenerFactory.TestServerMessageListener serverMessageListener = testServerMessageListenerFactory.create();

        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(testServerMessageListenerFactory);
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            boolean await = TestAwaitUtils.await(new TestAwaitTaskUtils() {
                @Override
                public boolean checkCompleted() {
                    return serverMessageListener.hasReceivedPing();
                }
            }, 100, 3000);
            Assert.assertTrue(await);
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
    public void pingAndRequestResponse() throws IOException, InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX));
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);
            
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);
            
            Assert.assertArrayEquals(randomByte, response);
            PinpointRPCTestUtils.close(client);
        } finally {
            testPinpointServerAcceptor.close();
        }
    }

    @Test
    public void sendSync() throws IOException, InterruptedException {
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
    public void requestAndResponse() throws IOException, InterruptedException {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor(new TestServerMessageListenerFactory(TestServerMessageListenerFactory.HandshakeType.DUPLEX));
        int bindPort = testPinpointServerAcceptor.bind();

        try {
            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            byte[] randomByte = TestByteUtils.createRandomByte(20);
            byte[] response = PinpointRPCTestUtils.request(client, randomByte);

            Assert.assertArrayEquals(randomByte, response);
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
            
            Assert.assertEquals(timeout, connectTimeout);
        } finally {
            pinpointClientFactory.release();
        }
    }

    @Test(expected = PinpointSocketException.class)
    @Ignore
    public void throwWriteBufferFullExceptionTest() {
        TestPinpointServerAcceptor testPinpointServerAcceptor = new TestPinpointServerAcceptor();
        int bindPort = testPinpointServerAcceptor.bind();

        int defaultWriteBufferHighWaterMark = clientFactory.getWriteBufferHighWaterMark();
        int defaultWriteBufferLowWaterMark = clientFactory.getWriteBufferLowWaterMark();
        try {
            clientFactory.setWriteBufferHighWaterMark(2);
            clientFactory.setWriteBufferLowWaterMark(1);

            PinpointClient client = clientFactory.connect("127.0.0.1", bindPort);

            List<Future> futureList = new ArrayList();
            for (int i = 0; i < 30; i++) {
                Future<ResponseMessage> requestFuture = client.request(new byte[20]);
                futureList.add(requestFuture);
            }

            for (Future future : futureList) {
                future.getResult();
            }

            PinpointRPCTestUtils.close(client);
        } finally {
            clientFactory.setWriteBufferHighWaterMark(defaultWriteBufferHighWaterMark);
            clientFactory.setWriteBufferLowWaterMark(defaultWriteBufferLowWaterMark);

            testPinpointServerAcceptor.close();
        }
    }


}
