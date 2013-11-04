package com.nhn.pinpoint.profiler.sender;

import com.nhn.pinpoint.thrift.dto.TApiMetaData;
import com.nhn.pinpoint.rpc.packet.RequestPacket;
import com.nhn.pinpoint.rpc.packet.SendPacket;
import com.nhn.pinpoint.rpc.packet.StreamPacket;
import com.nhn.pinpoint.rpc.server.PinpointServerSocket;
import com.nhn.pinpoint.rpc.server.ServerMessageListener;
import com.nhn.pinpoint.rpc.server.ServerStreamChannel;
import com.nhn.pinpoint.rpc.server.SocketChannel;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TcpDataSenderTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final int PORT = 10050;
    public static final String HOST = "127.0.0.1";

    private PinpointServerSocket server;
    private CountDownLatch sendLatch;

    @Before
    public void serverStart() {
        server = new PinpointServerSocket();
        server.setMessageListener(new ServerMessageListener() {

            @Override
            public void handleSend(SendPacket sendPacket, SocketChannel channel) {
                logger.info("handleSend:{}", sendPacket);
                if (sendLatch != null) {
                    sendLatch.countDown();
                }
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
    }

    @After
    public void serverShutdown() {
        if (server != null) {
            server.close();
        }
    }

    @Test
    public void connectAndSend() throws InterruptedException {
        this.sendLatch = new CountDownLatch(2);

        TcpDataSender sender = new TcpDataSender(HOST, PORT);
        try {
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));
            sender.send(new TApiMetaData("test", System.currentTimeMillis(), 1, "TestApi"));


            boolean received = sendLatch.await(1000, TimeUnit.MILLISECONDS);
            Assert.assertTrue(received);
        } finally {
            sender.stop();
        }


    }
}
