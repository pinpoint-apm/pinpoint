package com.nhn.pinpoint.rpc.client;

import com.nhn.pinpoint.rpc.Future;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.ResponseMessage;
import com.nhn.pinpoint.rpc.TestByteUtils;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.TestSeverMessageListener;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 *
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


        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(500);

        PinpointServerSocket newServerSocket = null;
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);
            serverSocket.close();
            logger.info("server.close()---------------------------");
            Thread.sleep(1000);
            try {
                Future<ResponseMessage> response = socket.request(new byte[10]);
                response.await();
                ResponseMessage result = response.getResult();
                Assert.fail("expected:exception");
            } catch (Exception e) {
                // 기대된 에러라 skip
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

    }

    @Test
    public void scheduledConnect() throws IOException, InterruptedException {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(2000);
        PinpointSocket socket = null;
        try {
            socket = pinpointSocketFactory.scheduledConnect("localhost", 10234);

            PinpointServerSocket serverSocket = new PinpointServerSocket();
            serverSocket.setMessageListener(new TestSeverMessageListener());
            serverSocket.bind("localhost", 10234);

            Thread.sleep(3000);
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
        }
    }

    @Test
    public void scheduledConnectAndClosed() throws IOException, InterruptedException {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(100);
        PinpointSocket socket = pinpointSocketFactory.scheduledConnect("localhost", 10234);

        logger.debug("close");
        socket.close();
        Thread.sleep(500);
    }

    @Test
    public void scheduledConnectDelayAndClosed() throws IOException, InterruptedException {
        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        PinpointSocket socket = pinpointSocketFactory.scheduledConnect("localhost", 10234);

        Thread.sleep(2000);
        logger.debug("close");
        socket.close();
        Thread.sleep(1000);
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

    }



}
