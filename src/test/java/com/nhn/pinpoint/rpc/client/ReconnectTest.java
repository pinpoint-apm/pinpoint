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


        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);

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
        // 서버가 먼저 닫힌 상황에서 비정상적인 상황에서 client socket이 잘 닫히는지 테스트

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
            //  강제로 서버를 close함.
            ss.close();
            Thread.sleep(1000*2);

            socket.close();
        } finally {
            pinpointSocketFactory.release();
        }

    }

    @Test
    public void serverCloseAndWrite() throws IOException, InterruptedException {
        // 서버가 먼저 닫힌 상황에서 비정상적인 상황에서 client socket이 잘 닫히는지 테스트

        PinpointServerSocket ss = new PinpointServerSocket();
        ss.bind("127.0.0.1", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(200);
        pinpointSocketFactory.setTimeoutMillis(500);
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("127.0.0.1", 10234);

            byte[] randomByte = TestByteUtils.createRandomByte(10);
            // 서버를 그냥 닫고 request
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
