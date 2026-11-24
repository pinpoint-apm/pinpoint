/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.kafka;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.api.PluginTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.kafka.Kafka4UnitServer;
import test.pinpoint.plugin.kafka.TestProducer;

import java.util.List;
import java.util.Random;

import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_MULTI_RECORDS;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;

/**
 * kafka-clients 4.x is Java 11+ bytecode and has no kafka_2.12 artifact, hence the separate JDK 17 module.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-kafka-client.config")
@Dependency({
        "org.apache.kafka:kafka-clients:[4.0.0,4.max]"
})
@SharedDependency({
        TestcontainersOption.TEST_CONTAINER,
        TestcontainersOption.KAFKA
})
@SharedTestLifeCycleClass(Kafka4UnitServer.class)
public class KafkaClient_4_x_IT extends KafkaClient4ITBase {
    Random random = new Random();

    @Test
    public void producerSendTest() throws NoSuchMethodException {
        int messageCount = random.nextInt(5) + 1;
        final TestProducer producer = new TestProducer(brokerUrl);

        List<Long> offsets = producer.sendMessage(topic, messageCount, TRACE_TYPE_RECORD);
        KafkaClientITBase.verifyProducerSend(brokerUrl, topic, offsets);
    }

    @Test
    public void recordEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer(brokerUrl);

        List<Long> offsets = producer.sendMessage(topic, 1, TRACE_TYPE_RECORD);
        KafkaClientITBase.verifySingleConsumerEntryPoint(brokerUrl, topic, offsets.get(0));
    }

    @Test
    public void recordMultiEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer(brokerUrl);

        producer.sendMessage(topic, 1, TRACE_TYPE_MULTI_RECORDS);
        KafkaClientITBase.verifyMultiConsumerEntryPoint(brokerUrl, topic);
    }

    /**
     * group.protocol=consumer (KIP-848, GA since 4.0) is served by AsyncKafkaConsumer, whose network I/O runs on a
     * background thread instead of the polling thread.
     * <p>
     * Disabled: the plugin does not record the local address for this consumer, the span endPoint falls back to the
     * broker address ("localhost:33026" instead of "[127.0.0.1:2190, ...]"). Everything else is recorded as expected.
     * Enable once the plugin handles AsyncKafkaConsumer.
     */
    @Test
    @Disabled("endPoint of group.protocol=consumer falls back to the broker address")
    public void consumerGroupProtocolRecordEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer(brokerUrl);

        List<Long> offsets = producer.sendMessage(consumerProtocolTopic, 1, TRACE_TYPE_RECORD);
        KafkaClientITBase.verifySingleConsumerEntryPoint(brokerUrl, consumerProtocolTopic, offsets.get(0));
    }

    @Test
    @Disabled("endPoint of group.protocol=consumer falls back to the broker address")
    public void consumerGroupProtocolRecordMultiEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer(brokerUrl);

        producer.sendMessage(consumerProtocolTopic, 1, TRACE_TYPE_MULTI_RECORDS);
        KafkaClientITBase.verifyMultiConsumerEntryPoint(brokerUrl, consumerProtocolTopic);
    }
}
