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

import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinderInitializer;
import com.navercorp.pinpoint.rpc.PinpointDatagramSocket;
import com.navercorp.pinpoint.rpc.PinpointOioDatagramSocketFactory;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.thrift.TBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.SocketUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author emeroad
 */
public class UdpDataSenderTest {
    private final int PORT = SocketUtils.findAvailableUdpPort(9009);
    @BeforeClass
    public static void before() {
        Slf4jLoggerBinderInitializer.beforeClass();
    }

    @AfterClass
    public static void after() {
        Slf4jLoggerBinderInitializer.afterClass();
    }



    @Test
    public void sendAndFlushCheck() throws InterruptedException {
        PinpointOioDatagramSocketFactory datagramSocketFactory = new PinpointOioDatagramSocketFactory();
        PinpointDatagramSocket datagramSocket = datagramSocketFactory.createSocket(1000, 1024*64*100);
        datagramSocket.connect(new InetSocketAddress("localhost", PORT));

        UdpDataSender sender = new UdpDataSender(datagramSocket, "test", 128);

        TAgentInfo agentInfo = new TAgentInfo();
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.send(agentInfo);
        sender.stop();
    }

//    @Test
//    public void sendAndLarge() throws InterruptedException {
//        String random = RandomStringUtils.randomAlphabetic(UdpDataSender.UDP_MAX_PACKET_LENGTH);
//        TAgentInfo agentInfo = new TAgentInfo();
//        agentInfo.setAgentId(random);
//        boolean limit = sendMessage_getLimit(agentInfo, 5000);
//        Assert.assertTrue("limit overflow",limit);
//
//        boolean noLimit = sendMessage_getLimit(new TAgentInfo(), 5000);
//        Assert.assertFalse("success", noLimit);
//    }

    @Test
    public void sendExceedData() throws InterruptedException {
        String random = RandomStringUtils.randomAlphabetic(UdpDataSender.UDP_MAX_PACKET_LENGTH + 100);
        TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setAgentId(random);
        boolean limit = sendMessage_getLimit(agentInfo, 1000);

        // do not execute.
        Assert.assertFalse(limit);
    }

    
    private boolean sendMessage_getLimit(TBase tbase, long waitTimeMillis) throws InterruptedException {
        final AtomicBoolean limitCounter = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);

        PinpointOioDatagramSocketFactory datagramSocketFactory = new PinpointOioDatagramSocketFactory();
        PinpointDatagramSocket datagramSocket = datagramSocketFactory.createSocket(1000, 1024*64*100);
        datagramSocket.connect(new InetSocketAddress("localhost", PORT));

        UdpDataSender sender = new UdpDataSender(datagramSocket, "test", 128) {
            @Override
            protected boolean isLimit(int interBufferSize) {
                boolean limit = super.isLimit(interBufferSize);
                limitCounter.set(limit);
                latch.countDown();
                return limit;
            }
        };
        try {
            sender.send(tbase);
            latch.await(waitTimeMillis, TimeUnit.MILLISECONDS);
        } finally {
            sender.stop();
        }
        return limitCounter.get();
    }

}
