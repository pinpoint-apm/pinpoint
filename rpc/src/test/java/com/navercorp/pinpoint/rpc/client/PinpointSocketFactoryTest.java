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

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelFuture;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.RequestResponseServerMessageListener;
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.server.PinpointServerAcceptor;
import com.navercorp.pinpoint.rpc.server.TestSeverMessageListener;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;


/**
 * @author emeroad
 */
public class PinpointSocketFactoryTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static int bindPort;
    private static PinpointSocketFactory socketFactory;
    
    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = PinpointRPCTestUtils.findAvailablePort();

        socketFactory = new PinpointSocketFactory();
        socketFactory.setPingDelay(100);
    }
    
    @AfterClass
    public static void tearDown() {
        if (socketFactory != null) {
            socketFactory.release();
        }
    }

    @Test
    public void connectFail() {
        try {
            socketFactory.connect("127.0.0.1", bindPort);
            Assert.fail();
        } catch (PinpointSocketException e) {
            Assert.assertTrue(ConnectException.class.isInstance(e.getCause()));
        } 
    }

    @Test
    public void reconnectFail() throws InterruptedException {
        // confirm simplified error message when api called.
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", bindPort);
        ChannelFuture reconnect = socketFactory.reconnect(remoteAddress);
        reconnect.await();
        Assert.assertFalse(reconnect.isSuccess());
        Assert.assertTrue(ConnectException.class.isInstance(reconnect.getCause()));
        
        Thread.sleep(1000);
    }

    @Test
    public void connect() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort);

        try {
            PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);
            PinpointRPCTestUtils.close(socket);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void pingInternal() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort);

        try {
            PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);
            Thread.sleep(1000);
            PinpointRPCTestUtils.close(socket);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void ping() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort);

        try {
            PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);
            socket.sendPing();
            PinpointRPCTestUtils.close(socket);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void pingAndRequestResponse() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new RequestResponseServerMessageListener());

        try {
            PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);
            
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(socket, randomByte);
            
            Assert.assertArrayEquals(randomByte, response);
            PinpointRPCTestUtils.close(socket);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void sendSync() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new TestSeverMessageListener());

        try {
            PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);
            logger.info("send1");
            socket.send(new byte[20]);
            logger.info("send2");
            socket.sendSync(new byte[20]);

            PinpointRPCTestUtils.close(socket);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void requestAndResponse() throws IOException, InterruptedException {
        PinpointServerAcceptor serverAcceptor = PinpointRPCTestUtils.createPinpointServerFactory(bindPort, new TestSeverMessageListener());

        try {
            PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);

            byte[] randomByte = TestByteUtils.createRandomByte(20);
            byte[] response = PinpointRPCTestUtils.request(socket, randomByte);

            Assert.assertArrayEquals(randomByte, response);
            PinpointRPCTestUtils.close(socket);
        } finally {
            PinpointRPCTestUtils.close(serverAcceptor);
        }
    }

    @Test
    public void connectTimeout() {
        int timeout = 1000;

        PinpointSocketFactory pinpointSocketFactory = null;
        try {
            pinpointSocketFactory = new PinpointSocketFactory();
            pinpointSocketFactory.setConnectTimeout(timeout);
            int connectTimeout = pinpointSocketFactory.getConnectTimeout();
            
            Assert.assertEquals(timeout, connectTimeout);
        } finally {
            pinpointSocketFactory.release();
        }
    }
    
}
