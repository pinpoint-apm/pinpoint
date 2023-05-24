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

package com.navercorp.pinpoint.thrift.sender;

import com.navercorp.pinpoint.common.profiler.message.BypassMessageConverter;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.logging.Log4j2LoggerBinderInitializer;
import com.navercorp.pinpoint.testcase.util.SocketUtils;
import com.navercorp.pinpoint.thrift.dto.TAgentInfo;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;

/**
 * @author emeroad
 */
public class UdpDataSenderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String APP_NAME = "appName";
    private static final String AGENT_ID = "agentid";
    private static final int AGENT_START_TIME = 0;
    private static final ServiceType APP_SERVICE_TYPE = ServiceType.STAND_ALONE;

    private final int PORT = SocketUtils.findAvailableUdpPort(9009);

    private final BiPredicate<byte[], TBase<?, ?>> maxBytesLengthPredicate = new MaxBytesLengthPredicate<>(logger, ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH);

    @BeforeAll
    public static void before() {
        Log4j2LoggerBinderInitializer.beforeClass();
    }

    @AfterAll
    public static void after() {
        Log4j2LoggerBinderInitializer.afterClass();
    }


    @Test
    public void sendAndFlushCheck() {
        final MessageConverter<TBase<?, ?>, TBase<?, ?>> messageConverter = new BypassMessageConverter<>();
        SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH);
        final MessageSerializer<TBase<?, ?>, ByteMessage> thriftMessageSerializer = new ThriftUdpMessageSerializer<>(messageConverter, serializerFactory.createSerializer());
        UdpDataSender<TBase<?, ?>> sender = new UdpDataSender<>("localhost", PORT, "test", 128, 1000, 1024 * 64 * 100,
                thriftMessageSerializer);

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
//        Assertions.assertTrue("limit overflow",limit);
//
//        boolean noLimit = sendMessage_getLimit(new TAgentInfo(), 5000);
//        Assertions.assertFalse("success", noLimit);
//    }

    @Test
    public void sendExceedData() throws InterruptedException {
        String random = RandomStringUtils.randomAlphabetic(ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH + 100);
        TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setAgentId(random);
        boolean limit = sendMessage_getLimit(agentInfo, 1000);

        // do not execute.
        Assertions.assertTrue(limit);
    }

    @Test
    public void sendData() throws InterruptedException {
        String random = RandomStringUtils.randomAlphabetic(100);
        TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setAgentId(random);
        boolean limit = sendMessage_getLimit(agentInfo, 1000);

        // do not execute.
        Assertions.assertFalse(limit);
    }


    private boolean sendMessage_getLimit(TBase<?, ?> tbase, long waitTimeMillis) throws InterruptedException {
        final AtomicBoolean limitCounter = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        final MessageConverter<TBase<?, ?>, TBase<?, ?>> messageConverter = new BypassMessageConverter<>();

        SerializerFactory<HeaderTBaseSerializer> serializerFactory = new HeaderTBaseSerializerFactory(
                ThriftUdpMessageSerializer.UDP_MAX_PACKET_LENGTH,
                HeaderTBaseSerializerFactory.DEFAULT_TBASE_LOCATOR);

        BiPredicate<byte[], TBase<?, ?>> filter = (bytes, tBase) -> {
            boolean limit = !maxBytesLengthPredicate.test(bytes, tBase);
            limitCounter.set(limit);
            latch.countDown();
            return limit;
        };

        final MessageSerializer<Object, ByteMessage> serializer = new ThriftUdpMessageSerializer(messageConverter, serializerFactory.createSerializer(), filter);

        UdpDataSender<Object> sender = new UdpDataSender<>("localhost", PORT, "test",
                128, 1000, 1024 * 64 * 100,
                serializer);
        try {
            sender.send(tbase);
            latch.await(waitTimeMillis, TimeUnit.MILLISECONDS);
        } finally {
            sender.stop();
        }
        return limitCounter.get();
    }

}
