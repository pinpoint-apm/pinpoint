/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.common.util.IOUtils;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * @author emeroad
 */
@Ignore
public class UdpSocketTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    // port conflict against base port. so increased 5
    private int PORT = SocketUtils.findAvailableUdpPort(61112);
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
    public void setDown() throws InterruptedException {
        IOUtils.closeQuietly(sender);
        IOUtils.closeQuietly(receiver);
        // port conflict happens when testcases run continuously so port number is increased.
        PORT = SocketUtils.findAvailableUdpPort(61112);
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
            Assert.fail("expected fail, but succeed");
        } catch (IOException ignore) {
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

        DatagramPacket packet1 = newDatagramPacket(50000);
        sender.send(packet1);

        DatagramPacket r1 = newDatagramPacket(50000);
        receiver.receive(r1);

        logger.debug("packetSize:{}", r1.getLength());


    }

    // uncomment when remote test
    // @Test
    public void testRemoteReceive() {
        while (true) {
            DatagramPacket datagramPacket = newDatagramPacket(70000);
            try {
                receiver.receive(datagramPacket);
                logger.debug("data size:{}", datagramPacket.getLength());
            } catch (IOException e) {
                logger.warn("receive error:{}", e.getMessage(), e);
            }
        }
    }

    // @Test
    public void testRemoteSend() throws IOException {
        DatagramSocket so = new DatagramSocket();
        so.connect(new InetSocketAddress("10.66.18.78", PORT));

        so.send(newDatagramPacket(1500));

        so.send(newDatagramPacket(10000));

        so.send(newDatagramPacket(20000));

        so.send(newDatagramPacket(50000));

        so.send(newDatagramPacket(60000));


        so.send(newDatagramPacket(AcceptedSize));

        try {
            so.send(newDatagramPacket(AcceptedSize + 1));
            Assert.fail("failed");
        } catch (IOException ignore) {
        }

        try {
            so.send(newDatagramPacket(70000));
            Assert.fail("failed");
        } catch (IOException ignore) {
        }

        so.close();
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
        so.close();
    }
}
