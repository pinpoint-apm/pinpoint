package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.thrift.dto.TApiMetaData;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.StreamPacket;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.ServerStreamChannel;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TcpDataSenderReconnectTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = 10050;
    public static final String HOST = "127.0.0.1";

    private int send;

    public PinpointServerSocket serverStart() {
        PinpointServerSocket server = new PinpointServerSocket();
        server.setMessageListener(new ServerMessageListener() {

            @Override
            public void handleSend(SendPacket sendPacket, SocketChannel channel) {
                logger.info("handleSend:{}", sendPacket);
                send++;
            }

            @Override
            public void handleRequest(RequestPacket requestPacket, SocketChannel channel) {
                logger.info("handleRequest:{}", requestPacket);
            }

            @Override
            public void handleStream(StreamPacket streamPacket, ServerStreamChannel streamChannel) {
                logger.info("handleStreamPacket:{}", streamPacket);
            }
        });
        server.bind(HOST, PORT);
        return server;
    }


    @Test
    public void connectAndSend() throws InterruptedException {
        PinpointServerSocket old = serverStart();
        TcpDataSender sender = new TcpDataSender(HOST, PORT);
        Thread.sleep(500);
        old.close();

        Thread.sleep(500);
        logger.info("Server start------------------");
        PinpointServerSocket pinpointServerSocket = serverStart();

        Thread.sleep(5000);
        logger.info("sendMessage------------------");
        sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));

        Thread.sleep(500);
        logger.info("sender stop------------------");
        sender.stop();

        pinpointServerSocket.close();
    }
}
