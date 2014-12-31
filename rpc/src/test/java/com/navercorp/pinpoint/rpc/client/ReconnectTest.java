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
import com.navercorp.pinpoint.rpc.client.PinpointSocket;
import com.navercorp.pinpoint.rpc.client.PinpointSocketFactory;
import com.navercorp.pinpoint.rpc.client.PinpointSocketReconnectEventListener;
import com.navercorp.pinpoint.rpc.server.PinpointServerSocket;
import com.navercorp.pinpoint.rpc.server.TestSeverMessageListener;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author emeroad
 */
//@Ignore
public class ReconnectTest {

    public static final int PORT = 10234;
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void reconnect() throws IOException, InterruptedException {
        PinpointServerSocket serverSocket = new PinpointServerSocket();
        serverSocket.setMessageListener(new TestSeverMessageListener());
        serverSocket.bind("localhost", PORT);

        final AtomicBoolean reconnectPerformed = new AtomicBoolean(false);

        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);

        PinpointServerSocket newServerSocket = null;
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);
            socket.addPinpointSocketReconnectEventListener(new PinpointSocketReconnectEventListener() {
				
				@Override
				public void reconnectPerformed(PinpointSocket socket) {
					reconnectPerformed.set(true);
				}
				
			});
            
            serverSocket.close();
            logger.info("server.close()---------------------------");
            Thread.sleep(1000);
            try {
                Future<ResponseMessage> response = socket.request(new byte[10]);
                response.await();
                ResponseMessage result = response.getResult();
                Assert.fail("expected:exception");
            } catch (Exception e) {
                // skip because of expected error
            }

            newServerSocket = new PinpointServerSocket();
            newServerSocket.setMessageListener(new TestSeverMessageListener());
            newServerSocket.bind("localhost", 10234);
            logger.info("bind server---------------------------");

            Thread.sleep(3000);
            logger.info("request server---------------------------");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            Future<ResponseMessage> response = socket.request(randomByte);
            response.await();
            ResponseMessage result = response.getResult();
            Assert.assertArrayEquals(result.getMessage(), randomByte);
            socket.close();
        } finally {
            if (newServerSocket != null) {
                newServerSocket.close();
            }
            pinpointSocketFactory.release();
        }
        
        Assert.assertTrue(reconnectPerformed.get());
    }

    @Test
    public void scheduledConnect() throws IOException, InterruptedException {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        PinpointSocket socket = null;
        PinpointServerSocket serverSocket = null;
        try {
            socket = pinpointSocketFactory.scheduledConnect("localhost", 10234);

            serverSocket = new PinpointServerSocket();
            serverSocket.setMessageListener(new TestSeverMessageListener());
            serverSocket.bind("localhost", 10234);

            Thread.sleep(2000);
            logger.info("request server---------------------------");
            byte[] randomByte = TestByteUtils.createRandomByte(10);
            Future<ResponseMessage> response = socket.request(randomByte);
            response.await();
            ResponseMessage result = response.getResult();
            Assert.assertArrayEquals(randomByte, result.getMessage());

        } finally {
            if (socket != null) {
                socket.close();
            }
            pinpointSocketFactory.release();
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    @Test
    public void scheduledConnectAndClosed() throws IOException, InterruptedException {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(100);
        PinpointSocket socket = pinpointSocketFactory.scheduledConnect("localhost", 10234);

        logger.debug("close");
        socket.close();
        pinpointSocketFactory.release();
    }

    @Test
    public void scheduledConnectDelayAndClosed() throws IOException, InterruptedException {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        PinpointSocket socket = pinpointSocketFactory.scheduledConnect("localhost", 10234);

        Thread.sleep(2000);
        logger.debug("close pinpoint socket");
        socket.close();
        pinpointSocketFactory.release();
    }

    @Test
    public void scheduledConnectStateTest() {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        PinpointSocket socket = pinpointSocketFactory.scheduledConnect("localhost", 10234);


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
            Future<ResponseMessage> request = socket.request(new byte[10]);
            request.await();
            request.getResult();
            Assert.fail();
        } catch (PinpointSocketException e) {
        }

        socket.close();
        pinpointSocketFactory.release();
    }

    @Test
    public void serverFirstClose() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a socket should be closed properly.
        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        pinpointSocketFactory.setTimeoutMillis(500);
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

            byte[] randomByte = TestByteUtils.createRandomByte(10);
            Future<ResponseMessage> response = socket.request(randomByte);
            response.await();
            try {
                response.getResult();
            } catch (Exception e) {
                logger.debug("timeout.", e);
            }
            // close server by force
            ss.close();
            Thread.sleep(1000*2);

            socket.close();
        } finally {
            pinpointSocketFactory.release();
        }

    }

    @Test
    public void serverCloseAndWrite() throws IOException, InterruptedException {
        // when abnormal case in which server has been closed first, confirm that a client socket should be closed properly.

        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        pinpointSocketFactory.setTimeoutMillis(500);
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

            byte[] randomByte = TestByteUtils.createRandomByte(10);
            // just close server and request
            ss.close();
            Future<ResponseMessage> response = socket.request(randomByte);
            response.await();
            try {
                response.getResult();
                Assert.fail("expected exception");
            } catch (Exception e) {
            }


            Thread.sleep(1000*3);

            socket.close();
        } finally {
            pinpointSocketFactory.release();
        }

    }


}
