package com.profiler.socket;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpSocketTest {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private static int PORT = 9993;


    private DatagramSocket receiver;
    private DatagramSocket sender;

    @Before
    public void setUp() throws SocketException {
        receiver = new DatagramSocket(PORT);
        sender = new DatagramSocket();
        sender.connect(new InetSocketAddress("localhost", PORT));
    }

    @After
    public void setDown() {
        close(sender);
        close(receiver);
    }

    private void close(DatagramSocket socket) {
        if (socket == null) {
            return;
        }
        socket.close();
    }

    @Test
    public void testChunkSize() throws IOException {

        byte[] bytes1 = new byte[1000];
        DatagramPacket packet1 = new DatagramPacket(bytes1, 1000);
        sender.send(packet1);

        byte[] bytes2 = new byte[500];
        DatagramPacket packet2 = new DatagramPacket(bytes2, 500);
        sender.send(packet2);

        DatagramPacket r1 = new DatagramPacket(new byte[2000], 2000);
        receiver.receive(r1);
        Assert.assertEquals(r1.getLength(), 1000);

        DatagramPacket r2 = new DatagramPacket(new byte[2000], 2000);
        receiver.receive(r2);
        Assert.assertEquals(r2.getLength(), 500);

    }

    @Test
    public void testDatagramSendFail() {
        int size = 70000;
        byte[] bytes1 = new byte[size];
        DatagramPacket packet1 = new DatagramPacket(bytes1, size);
        try {
            sender.send(packet1);
            Assert.fail("실패해야 정상인데 성공.");
        } catch (IOException e) {
            logger.log(Level.INFO, "메시지가 너무 크다. " + e.getMessage(), e);
        }
    }
     @Test
     public void testDatagramMaxSend() throws IOException {
        // The correct maximum UDP message size is 65507, as determined by the following formula:
        // 0xffff - (sizeof(IP Header) + sizeof(UDP Header)) = 65535-(20+8) = 65507
        int size = 65507;
        byte[] bytes1 = new byte[size];
        DatagramPacket packet1 = new DatagramPacket(bytes1, size);
        sender.send(packet1);

        DatagramPacket r1 = new DatagramPacket(new byte[size], size);
        receiver.receive(r1);
         Assert.assertEquals(r1.getLength(), size);
    }


    @Test
    public void testMaxBytes() throws IOException {

        byte[] bytes1 = new byte[500000];
        DatagramPacket packet1 = new DatagramPacket(bytes1, 500000);
        sender.send(packet1);



        DatagramPacket r1 = new DatagramPacket(new byte[50000], 50000);
        receiver.receive(r1);

        logger.info(String.valueOf(r1.getLength()));



    }

    // 원격지 테스트시 풀어서 확인한다.
    //@Test
    public void testReceive() {
        while(true) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[70000], 70000);
            try {
                receiver.receive(datagramPacket);
                logger.info("data size:" + datagramPacket.getLength());
            } catch (IOException e) {
                logger.log(Level.WARNING, "receive error:" + e.getMessage(), e);
            }
        }
    }
}
