package com.nhn.pinpoint.common.io.rpc;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


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
        } catch (SocketException e) {

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

            socket.send(new byte[20]);
            MessageFuture request = socket.request(new byte[10]);
            socket.sendSync(new byte[20]);

            StreamChannelFuture streamChannel = socket.createStreamChannel();

            socket.close();
        } finally {
            pinpointSocketFactory.release();
            ss.release();
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
