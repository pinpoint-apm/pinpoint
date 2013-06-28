package com.nhn.pinpoint.common.io.rpc;

import org.junit.Test;

import java.net.Socket;

/**
 *
 */
public class PinpointServerSocketTest {
    @Test
    public void testBind() throws Exception {
        PinpointServerSocket pinpointServerSocket = new PinpointServerSocket();
        pinpointServerSocket.setPipelineFactory(new DiscardPipelineFactory());

        pinpointServerSocket.bind("127.0.0.1", 22234);

        java.net.Socket socket = new Socket("127.0.0.1", 22234);
        socket.getOutputStream().write(new byte[10]);
        socket.getOutputStream().flush();
        socket.close();

        Thread.sleep(1000);
        pinpointServerSocket.release();
    }


}
