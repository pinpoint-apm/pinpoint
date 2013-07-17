package com.nhn.pinpoint.common.rpc.client;

import com.nhn.pinpoint.common.rpc.server.PinpointServerSocket;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 *
 */
@Ignore
public class ReconnectTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());




    @Test
    public void reconnect() throws IOException, InterruptedException {
        PinpointServerSocket serverSocket = new PinpointServerSocket();
//        ss.setPipelineFactory(new DiscardPipelineFactory());
        serverSocket.bind("localhost", 10234);

        final PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        try {
            PinpointSocket socket = pinpointSocketFactory.connect("localhost", 10234);
            serverSocket.close();
            Thread.sleep(1000 * 10);
            System.out.println("----------------------");
        } finally {
            Thread.sleep(1000*5);
            System.out.println("----------------------");
            pinpointSocketFactory.release();
        }



    }







}
