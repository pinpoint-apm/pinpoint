package com.nhn.pinpoint.rpc;

import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public final class ClassPreLoader {


    public static void preload() {
        try {
            preload(65535);
        } catch (Exception e) {
        }
    }

    public static void preload(int port) {
        PinpointServerSocket serverSocket = null;
        PinpointSocket socket = null;
        PinpointSocketFactory socketFactory = null;
        try {
            serverSocket = new PinpointServerSocket();
            serverSocket.bind("127.0.0.1", port);

            socketFactory = new PinpointSocketFactory();
            socket = socketFactory.connect("127.0.0.1", port);
            socket.sendSync(new byte[0]);


        } catch (Exception ex) {

            System.err.print("preLoad error Caused:" + ex.getMessage());
            ex.printStackTrace();

            final Logger logger = LoggerFactory.getLogger(ClassPreLoader.class);
            logger.warn("preLoad error Caused:{}", ex.getMessage(), ex);
            if (ex instanceof PinpointSocketException) {
                throw (PinpointSocketException)ex;
            } else {
                throw new PinpointSocketException(ex.getMessage(), ex);
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if(socketFactory != null) {
                try {
                    socketFactory.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

}
