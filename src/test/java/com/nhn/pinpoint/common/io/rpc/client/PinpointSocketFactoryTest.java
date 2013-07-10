package com.nhn.pinpoint.common.io.rpc.client;

import com.nhn.pinpoint.common.io.rpc.*;
import com.nhn.pinpoint.common.io.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.common.io.rpc.server.TestSeverMessageListener;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;


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
    public void connect2() throws IOException, InterruptedException {
        PinpointServerSocket ss = new PinpointServerSocket();
//        ss.setPipelineFactory(new DiscardPipelineFactory());
        ss.setMessageListener(new TestSeverMessageListener());
        ss.bind("localhost", 10234);
        PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);

            socket.send(new byte[20]);
            byte[] bytes = new byte[10];
            bytes[0] = 1;
            Future<ResponseMessage> request = socket.request(bytes);
            request.await();
            ResponseMessage message = request.getResult();
            Assert.assertArrayEquals(message.getMessage(), bytes);
            socket.sendSync(new byte[20]);


            StreamChannel streamChannel = socket.createStreamChannel();
            byte[] openBytes = new byte[31];
            // 현재 서버에서 3번 보내게 되어 있음.
            final CountDownLatch latch = new CountDownLatch(3);
            final List<byte[]> list = Collections.synchronizedList(new ArrayList<byte[]>());
            streamChannel.setStreamChannelMessageListener(new StreamChannelMessageListener() {
                @Override
                public void handleStream(StreamChannel streamChannel, byte[] bytes) {
                    list.add(bytes);
                    latch.countDown();
                }
            });
            Future<StreamCreateResponse> open = streamChannel.open(new byte[31]);
            open.await();
            StreamCreateResponse response = open.getResult();
            Assert.assertTrue(response.isSuccess());
            Assert.assertArrayEquals(response.getMessage(), openBytes);
//            streamChannel.setStreamResponseListener(new Stream);
            latch.await();
            Assert.assertEquals(list.size(), 3);

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
