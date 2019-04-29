/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessor;
import com.navercorp.pinpoint.profiler.context.compress.SpanProcessorV1;
import com.navercorp.pinpoint.profiler.context.thrift.DefaultTransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.context.thrift.SpanThriftMessageConverter;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.dto.TSpan;
import com.navercorp.pinpoint.thrift.dto.TSpanChunk;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.thrift.TBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Taejin Koo
 */
public class NioUdpDataSenderTest {

    // The correct maximum UDP message size is 65507, as determined by the following formula:
    // 0xffff - (sizeof(IP Header) + sizeof(UDP Header)) = 65535-(20+8) = 65507
    private static int AcceptedSize = 65507;
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    // port conflict against base port. so increased 5
    private int PORT = SocketUtils.findAvailableUdpPort(61112);
    private DatagramSocket receiver;

    @Before
    public void setUp() throws SocketException {
        receiver = new DatagramSocket(PORT);
        receiver.setSoTimeout(1000);
    }

    @After
    public void setDown()  {
        IOUtils.closeQuietly(receiver);
        // port conflict happens when testcases run continuously so port number is increased.
        PORT = SocketUtils.findAvailableUdpPort(61112);
    }

    @Test
    public void sendTest1() throws Exception {
        NioUDPDataSender sender = newNioUdpDataSender();

        int sendMessageCount = 10;
        TAgentInfo agentInfo = new TAgentInfo();
        for (int i = 0; i < 10; i++) {
            sender.send(agentInfo);
        }

        try {
            waitMessageReceived(sendMessageCount);
        } finally {
            sender.stop();
        }
    }

    private NioUDPDataSender newNioUdpDataSender() {
        TransactionIdEncoder encoder = new DefaultTransactionIdEncoder("agentId", 0);
        SpanProcessor<TSpan, TSpanChunk> spanPostProcessor = new SpanProcessorV1();
        MessageConverter<TBase<?, ?>> messageConverter = new SpanThriftMessageConverter("appName", "agentId",
                0, ServiceType.STAND_ALONE.getCode(), encoder, spanPostProcessor);
        return new NioUDPDataSender("localhost", PORT, "test", 128, 1000, 1024 * 64 * 100, messageConverter);
    }

    @Test(expected = IOException.class)
    public void exceedMessageSendTest() throws IOException {
        String random = RandomStringUtils.randomAlphabetic(ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH + 100);

        TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setAgentId(random);

        NioUDPDataSender sender = newNioUdpDataSender();
        sender.send(agentInfo);

        waitMessageReceived(1);
    }


    private boolean sendMessage_getLimit(TBase tbase, long waitTimeMillis) throws InterruptedException {
        final AtomicBoolean limitCounter = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);

        NioUDPDataSender sender = newNioUdpDataSender();
        try {
            sender.send(tbase);
            latch.await(waitTimeMillis, TimeUnit.MILLISECONDS);
        } finally {
            sender.stop();
        }
        return limitCounter.get();
    }


    private void waitMessageReceived(int expectReceiveMessageCount) throws IOException {
        byte[] receiveData = new byte[65535];
        DatagramPacket datagramPacket = new DatagramPacket(receiveData, 0, receiveData.length);

        int remainCount = expectReceiveMessageCount;

        while (remainCount > 0) {
            remainCount--;
            receiver.receive(datagramPacket);
        }
    }

}
