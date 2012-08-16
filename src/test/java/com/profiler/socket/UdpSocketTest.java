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
    // The correct maximum UDP message size is 65507, as determined by the following formula:
    // 0xffff - (sizeof(IP Header) + sizeof(UDP Header)) = 65535-(20+8) = 65507
    private static int AcceptedSize = 65507;


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

    private DatagramPacket newDatagramPacket(int size) {
        return new DatagramPacket(new byte[size], size);
    }

    @Test
    public void testChunkSize() throws IOException {

        DatagramPacket packet1 = newDatagramPacket(1000);
        sender.send(packet1);

        DatagramPacket packet2 = newDatagramPacket(500);
        sender.send(packet2);

        DatagramPacket r1 = newDatagramPacket(2000);
        receiver.receive(r1);
        Assert.assertEquals(r1.getLength(), 1000);

        DatagramPacket r2 = newDatagramPacket(2000);
        receiver.receive(r2);
        Assert.assertEquals(r2.getLength(), 500);

    }

    @Test
    public void testDatagramSendFail() {
        int size = 70000;
        DatagramPacket packet1 = newDatagramPacket(size);
        try {
            sender.send(packet1);
            Assert.fail("실패해야 정상인데 성공.");
        } catch (IOException e) {
            logger.log(Level.INFO, "메시지가 너무 크다. " + e.getMessage(), e);
        }
    }

    @Test
    public void testDatagramMaxSend() throws IOException {

        DatagramPacket packet1 = newDatagramPacket(AcceptedSize);
        sender.send(packet1);

        DatagramPacket r1 = newDatagramPacket(AcceptedSize);
        receiver.receive(r1);
        Assert.assertEquals(r1.getLength(), AcceptedSize);
    }


    @Test
    public void testMaxBytes() throws IOException {

        DatagramPacket packet1 = newDatagramPacket(500000);
        sender.send(packet1);


        DatagramPacket r1 = newDatagramPacket(50000);
        receiver.receive(r1);

        logger.info(String.valueOf(r1.getLength()));


    }

    // 원격지 테스트시 풀어서 확인한다.
    //@Test
    public void testRemoteReceive() {
        while (true) {
            DatagramPacket datagramPacket = newDatagramPacket(70000);
            try {
                receiver.receive(datagramPacket);
                logger.info("data size:" + datagramPacket.getLength());
            } catch (IOException e) {
                logger.log(Level.WARNING, "receive error:" + e.getMessage(), e);
            }
        }
    }

//    @Test
    public void testRemoteSend() throws IOException, InterruptedException {
        DatagramSocket send = new DatagramSocket();
        send.connect(new InetSocketAddress("10.66.18.78", PORT));

        send.send(newDatagramPacket(1500));

        send.send(newDatagramPacket(10000));

        send.send(newDatagramPacket(20000));

        send.send(newDatagramPacket(50000));

        send.send(newDatagramPacket(60000));


        send.send(newDatagramPacket(AcceptedSize));

        try {
            send.send(newDatagramPacket(AcceptedSize+1));
            Assert.fail("실패");
        } catch (IOException e) {
        }

        try {
            send.send(newDatagramPacket(70000));
            Assert.fail("실패");
        } catch (IOException e) {
        }

        Thread.sleep(1000*3);
    }

//    @Test
    public void createUdpSocket() throws IOException {
        DatagramSocket so = new DatagramSocket();
//        so.bind(new InetSocketAddress("localhost", 8081));
//        DatagramSocket receiver = new DatagramSocket(new InetSocketAddress("localhost", 8082));
//        receiver.bind(new InetSocketAddress("localhost", 8082));

        so.connect(new InetSocketAddress("localhost", 8082));
        so.send(new DatagramPacket(new byte[10], 10));

//        receiver.receive(newDatagramPacket(1000));
    }
}
