package com.nhn.pinpoint.common.io.rpc.client;

import com.google.common.primitives.Bytes;
import com.nhn.pinpoint.common.io.rpc.*;
import com.nhn.pinpoint.common.io.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.common.io.rpc.server.TestSeverMessageListener;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;


/**
 *
 */
public class PinpointSocketFactoryTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void connectFail() {
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        try {
            pinpointSocketFactory.connect("localhost", 10234);
            Assert.fail();
        } catch (PinpointSocketException e) {

        } finally {
            pinpointSocketFactory.release();
        }
    }


    @Test
    public void connect() throws IOException, InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
//        ss.setPipelineFactory(new DiscardPipelineFactory());
        ss.bind("localhost", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);

            socket.close();
        } finally {
            pinpointSocketFactory.release();
            ss.close();
        }

    }

    @Test
    public void sendSync() throws IOException, InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
//        ss.setPipelineFactory(new DiscardPipelineFactory());
        ss.setMessageListener(new TestSeverMessageListener());
        ss.bind("localhost", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);

            socket.send(new byte[20]);
            socket.sendSync(new byte[20]);



            socket.close();
        } finally {
            pinpointSocketFactory.release();
            ss.close();
        }

    }

    @Test
    public void requestAndResponse() throws IOException, InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
//        ss.setPipelineFactory(new DiscardPipelineFactory());
        ss.setMessageListener(new TestSeverMessageListener());
        ss.bind("localhost", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);

            byte[] bytes = TestByteUtils.createRandomByte(20);
            Future<ResponseMessage> request = socket.request(bytes);
            request.await();
            ResponseMessage message = request.getResult();
            Assert.assertArrayEquals(message.getMessage(), bytes);

            socket.close();
        } finally {
            pinpointSocketFactory.release();
            ss.close();
        }

    }




    @Test
    public void stream() throws IOException, InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();

        TestSeverMessageListener testSeverMessageListener = new TestSeverMessageListener();
        ss.setMessageListener(testSeverMessageListener);
        ss.bind("localhost", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);


            StreamChannel streamChannel = socket.createStreamChannel();
            byte[] openBytes = TestByteUtils.createRandomByte(30);

            // 현재 서버에서 3번 보내게 되어 있음.
            RecordedStreamChannelMessageListener clientListener = new RecordedStreamChannelMessageListener(3);
            streamChannel.setStreamChannelMessageListener(clientListener);

            Future<StreamCreateResponse> open = streamChannel.open(openBytes);
            open.await();
            StreamCreateResponse response = open.getResult();
            Assert.assertTrue(response.isSuccess());
            Assert.assertArrayEquals(response.getMessage(), openBytes);
            // stream 메시지를 대기함.
            clientListener.getLatch().await();
            List<byte[]> receivedMessage = clientListener.getReceivedMessage();
            List<byte[]> sendMessage = testSeverMessageListener.getSendMessage();

            Assert.assertEquals(receivedMessage.size(), sendMessage.size());
            for(int i =0; i<receivedMessage.size(); i++) {
                Assert.assertArrayEquals(receivedMessage.get(i), sendMessage.get(i));
            }

            socket.close();
        } finally {
            pinpointSocketFactory.release();
            ss.close();
        }

    }





    @Test
    public void connectTimeout() {
        PinpointSocketFactory pinpointSocketFactory = null;
        try {
            int timeout = 1000;
            pinpointSocketFactory = new PinpointSocketFactory();
            pinpointSocketFactory.setConnectTimeout(timeout);
            int connectTimeout = pinpointSocketFactory.getConnectTimeout();
            Assert.assertEquals(timeout, connectTimeout);
        } finally {
            pinpointSocketFactory.release();
        }


    }
}
