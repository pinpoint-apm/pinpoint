package com.nhn.pinpoint.common.rpc.client;

import com.nhn.pinpoint.common.rpc.Future;
import com.nhn.pinpoint.common.rpc.ResponseMessage;
import com.nhn.pinpoint.common.rpc.server.PinpointServerSocket;
import junit.framework.Assert;
import org.junit.Ignore;
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
        serverSocket.bind("localhost", PORT);

        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(1000);

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
            newServerSocket.bind("localhost", 10234);
            logger.info("bind server---------------------------");

            Thread.sleep(3000);
            logger.info("request server---------------------------");
            Future<ResponseMessage> response = socket.request(new byte[10]);
            response.await();
            ResponseMessage result = response.getResult();
        } finally {
            if (newServerSocket != null) {
                newServerSocket.close();
            }
            pinpointSocketFactory.release();
        }

    }

    @Test
    public void reconnect2() throws IOException, InterruptedException {
        PinpointServerSocket serverSocket = new PinpointServerSocket();
        serverSocket.bind("localhost", PORT);

        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setReconnectDelay(1000);

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
            newServerSocket.bind("localhost", 10234);
            logger.info("bind server---------------------------");

            Thread.sleep(3000);
            logger.info("request server---------------------------");
            Future<ResponseMessage> response = socket.request(new byte[10]);
            response.await();
            ResponseMessage result = response.getResult();
        } finally {
            if (newServerSocket != null) {
                newServerSocket.close();
            }
            pinpointSocketFactory.release();
        }

    }








}
