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

package com.navercorp.pinpoint.collector.receiver.thrift.udp;

import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.io.NetworkAvailabilityCheckPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * @author emeroad
 */
public class NetworkAvailabilityCheckPacketFilterTest {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private TBaseFilter<? super SocketAddress> filter;
    private DatagramSocket senderSocket;
    private DatagramSocket receiverSocket;

    @BeforeEach
    public void setUp() throws Exception {
        filter = new NetworkAvailabilityCheckPacketFilter();
        senderSocket = new DatagramSocket(0);
        receiverSocket = new DatagramSocket(0);
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            senderSocket.close();
        } catch (Exception e) {
        }
        try {
            receiverSocket.close();
        } catch (Exception e) {
        }
    }

    @Test
    public void testFilter() throws Exception {

        SocketAddress localSocketAddress = senderSocket.getLocalSocketAddress();
        logger.debug("localSocket:{}", localSocketAddress);

        NetworkAvailabilityCheckPacket packet = new NetworkAvailabilityCheckPacket();
        SocketAddress inetSocketAddress = new InetSocketAddress("localhost", senderSocket.getLocalPort());
        boolean skipResult = filter.filter(receiverSocket, packet, inetSocketAddress);

        Assertions.assertEquals(skipResult, TBaseFilter.BREAK);

        DatagramPacket receivePacket = new DatagramPacket(new byte[100], 100);
        senderSocket.receive(receivePacket);

        Assertions.assertEquals(receivePacket.getLength(), NetworkAvailabilityCheckPacket.DATA_OK.length);
        Assertions.assertArrayEquals(Arrays.copyOf(receivePacket.getData(), NetworkAvailabilityCheckPacket.DATA_OK.length), NetworkAvailabilityCheckPacket.DATA_OK);
    }


    @Test
    public void testFilter_Continue() {

        SocketAddress localSocketAddress = senderSocket.getLocalSocketAddress();
        logger.debug("localSocket:{}", localSocketAddress);

        TSpan skip = new TSpan();
        boolean skipResult = filter.filter(receiverSocket, skip, null);

        Assertions.assertEquals(skipResult, TBaseFilter.CONTINUE);


    }
}