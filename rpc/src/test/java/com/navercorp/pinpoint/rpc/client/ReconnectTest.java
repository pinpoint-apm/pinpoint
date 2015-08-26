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
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.PinpointSocketException;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.TestByteUtils;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.TestSeverMessageListener;
import com.navercorp.pinpoint.rpc.util.PinpointRPCTestUtils;


/**
 * @author emeroad
 */
//@Ignore
public class ReconnectTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static int bindPort;
    private static PinpointSocketFactory socketFactory;
    
    @BeforeClass
    public static void setUp() throws IOException {
        bindPort = PinpointRPCTestUtils.findAvailablePort();
        
        socketFactory = new PinpointSocketFactory();
        socketFactory.setReconnectDelay(200);
        socketFactory.setPingDelay(100);
        socketFactory.setTimeoutMillis(200);
    }
    
    @AfterClass
    public static void tearDown() {
        if (socketFactory != null) {
            socketFactory.release();
        }
    }


    @Test
    public void reconnect() throws IOException, InterruptedException {
        PinpointServerSocket serverSocket = PinpointRPCTestUtils.createServerSocket(bindPort, new TestSeverMessageListener());
        
        final AtomicBoolean reconnectPerformed = new AtomicBoolean(false);

        PinpointServerSocket newServerSocket = null;
        try {
            PinpointSocket socket = socketFactory.connect("localhost", bindPort);
            socket.addPinpointSocketReconnectEventListener(new PinpointSocketReconnectEventListener() {

                @Override
                public void reconnectPerformed(PinpointSocket socket) {
                    reconnectPerformed.set(true);
                }

            });
            
            PinpointRPCTestUtils.close(serverSocket);

            logger.info("server.close()---------------------------");
            Thread.sleep(1000);
            try {
                byte[] response = PinpointRPCTestUtils.request(socket, new byte[10]);
                Assert.fail("expected:exception");
            } catch (Exception e) {
                // skip because of expected error
            }

            newServerSocket = PinpointRPCTestUtils.createServerSocket(bindPort, new TestSeverMessageListener());
            logger.info("bind server---------------------------");

            Thread.sleep(3000);
            logger.info("request server---------------------------");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(socket, randomByte);
            
            Assert.assertArrayEquals(randomByte, response);
            
            PinpointRPCTestUtils.close(socket);
        } finally {
            PinpointRPCTestUtils.close(newServerSocket);
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
            logger.info((i + 1) + "th's start.");
            
            PinpointServerSocket serverSocket = PinpointRPCTestUtils.createServerSocket(bindPort, new TestSeverMessageListener());
            PinpointSocket socket = socketFactory.connect("localhost", bindPort);
            PinpointRPCTestUtils.close(serverSocket);

            logger.info("server.close()---------------------------");
            Thread.sleep(10000);

            serverSocket = PinpointRPCTestUtils.createServerSocket(bindPort, new TestSeverMessageListener());
            logger.info("bind server---------------------------");

            Thread.sleep(10000);
            logger.info("request server---------------------------");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(socket, randomByte);

            Assert.assertArrayEquals(randomByte, response);

            PinpointRPCTestUtils.close(socket);
            PinpointRPCTestUtils.close(serverSocket);
        }
        
        Thread.sleep(10000);

        Assert.assertEquals(threadCount, tbean.getThreadCount());
    }


    @Test
    public void scheduledConnect() throws IOException, InterruptedException {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        PinpointSocket socket = null;
        PinpointServerSocket serverSocket = null;
        try {
            socket = pinpointSocketFactory.scheduledConnect("localhost", bindPort);

            serverSocket = PinpointRPCTestUtils.createServerSocket(bindPort, new TestSeverMessageListener());

            Thread.sleep(2000);
            logger.info("request server---------------------------");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            byte[] response = PinpointRPCTestUtils.request(socket, randomByte);

            Assert.assertArrayEquals(randomByte, response);
        } finally {
            PinpointRPCTestUtils.close(socket);
            pinpointSocketFactory.release();
            PinpointRPCTestUtils.close(serverSocket);
        }
    }

    @Test
    public void scheduledConnectAndClosed() throws IOException, InterruptedException {
        PinpointSocket socket = socketFactory.scheduledConnect("localhost", bindPort);

        logger.debug("close");
        PinpointRPCTestUtils.close(socket);
    }

    @Test
    public void scheduledConnectDelayAndClosed() throws IOException, InterruptedException {
        PinpointSocket socket = socketFactory.scheduledConnect("localhost", bindPort);

        Thread.sleep(2000);
        logger.debug("close pinpoint socket");
        PinpointRPCTestUtils.close(socket);
    }

    @Test
    public void scheduledConnectStateTest() {
        PinpointSocket socket = socketFactory.scheduledConnect("localhost", bindPort);

        socket.send(new byte[10]);

        try {
            Future future = socket.sendAsync(new byte[10]);
            future.await();
            future.getResult();
            Assert.fail();
        } catch (PinpointSocketException e) {
        }

        try {
            socket.sendSync(new byte[10]);
            Assert.fail();
        } catch (PinpointSocketException e) {
        }

        try {
            PinpointRPCTestUtils.request(socket, new byte[10]);
            Assert.fail();
        } catch (PinpointSocketException e) {
        }

        PinpointRPCTestUtils.close(socket);
    }

    @Test
    public void serverFirstClose() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a socket should be closed properly.
        PinpointServerSocket serverSocket = PinpointRPCTestUtils.createServerSocket(bindPort);
        PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);

        byte[] randomByte = TestByteUtils.createRandomByte(10);
        Future<ResponseMessage> response = socket.request(randomByte);
        response.await();
        try {
            response.getResult();
        } catch (Exception e) {
            logger.debug("timeout.", e);
        }
        // close server by force
        PinpointRPCTestUtils.close(serverSocket);
        Thread.sleep(1000*2);
        PinpointRPCTestUtils.close(socket);
    }

    @Test
    public void serverCloseAndWrite() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a client socket should be closed properly.
        PinpointServerSocket serverSocket = PinpointRPCTestUtils.createServerSocket(bindPort);
        
        PinpointSocket socket = socketFactory.connect("127.0.0.1", bindPort);

        // just close server and request
        PinpointRPCTestUtils.close(serverSocket);

        byte[] randomByte = TestByteUtils.createRandomByte(10);
        Future<ResponseMessage> response = socket.request(randomByte);
        response.await();
        try {
            response.getResult();
            Assert.fail("expected exception");
        } catch (Exception e) {
        }

        Thread.sleep(1000 * 3);
        PinpointRPCTestUtils.close(socket);
    }

}
