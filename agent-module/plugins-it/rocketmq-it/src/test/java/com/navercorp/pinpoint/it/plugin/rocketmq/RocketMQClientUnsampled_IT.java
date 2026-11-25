/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.rocketmq;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.api.PluginTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.rocketmq.MessageHolder;
import test.pinpoint.plugin.rocketmq.RocketMQITConstants;
import test.pinpoint.plugin.rocketmq.TestConcurrentlyListener;

import java.nio.charset.StandardCharsets;

import static test.pinpoint.plugin.rocketmq.RocketMQITConstants.MAX_MESSAGE_WAIT_TIME;

/**
 * Sampling is disabled: the trace of the test method is not sampled, so a synchronous send adds Pinpoint-Sampled=s0
 * only. The first version of each range of the other ITs, and the last 4.x and 5.x.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-rocketmq-client-unsampled.config")
@Dependency({"org.apache.rocketmq:rocketmq-client:[4.0.0-incubating],[4.1.0-incubating],[4.3.0],[4.9.8],[5.0.0],[5.1.0],[5.1.4],[5.5.1]"})
@SharedDependency({TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(TestBrokerServer.class)
public class RocketMQClientUnsampled_IT {

    @Test
    public void syncSend_unsampled() throws Exception {
        final String namesrvAddr = System.getProperty(RocketMQITConstants.NAMESRV_ADDR_PROPERTY);
        final String id = Long.toString(System.currentTimeMillis(), 36);
        final String topic = "pinpoint-it-topic-unsampled-" + id;

        final DefaultMQProducer producer = new DefaultMQProducer("pinpoint-it-producer-unsampled-" + id);
        producer.setNamesrvAddr(namesrvAddr);
        producer.setInstanceName("producer-unsampled-" + id);
        final DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("pinpoint-it-consumer-unsampled-" + id);
        try {
            producer.start();
            RocketMQClientITBase.createTopic(producer, topic);

            final MessageHolder messageHolder = new MessageHolder();
            consumer.setNamesrvAddr(namesrvAddr);
            consumer.setInstanceName("consumer-unsampled-" + id);
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            consumer.subscribe(topic, "*");
            consumer.registerMessageListener(new TestConcurrentlyListener(messageHolder));
            consumer.start();

            final SendResult sendResult = producer.send(new Message(topic, "unsampled".getBytes(StandardCharsets.UTF_8)));
            Assertions.assertEquals(SendStatus.SEND_OK, sendResult.getSendStatus());

            final MessageExt received = messageHolder.poll(MAX_MESSAGE_WAIT_TIME);
            Assertions.assertNotNull(received, "message not consumed within " + MAX_MESSAGE_WAIT_TIME + "ms");
            Assertions.assertEquals("s0", received.getUserProperty("Pinpoint-Sampled"), "properties=" + received.getProperties());
            Assertions.assertNull(received.getUserProperty("Pinpoint-TraceID"), "properties=" + received.getProperties());
            Assertions.assertNull(received.getUserProperty("Pinpoint-SpanID"), "properties=" + received.getProperties());

            final PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();
            verifier.verifyTraceCount(0);
        } finally {
            consumer.shutdown();
            producer.shutdown();
        }
    }
}
