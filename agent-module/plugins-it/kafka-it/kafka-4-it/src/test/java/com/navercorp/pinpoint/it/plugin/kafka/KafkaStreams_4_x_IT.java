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
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.kafka.Kafka4UnitServer;
import test.pinpoint.plugin.kafka.TestProducer;

import java.util.Random;

import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_MULTI_RECORDS;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;

/**
 * kafka-streams 4.x is Java 11+ bytecode (Java 17+ for the broker side), hence the separate JDK 17 module.
 * kafka-clients follows the kafka-streams version.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-kafka-client.config")
@Dependency({
        "org.apache.kafka:kafka-streams:[4.0.0,4.max]",
        "org.apache.kafka:kafka-clients"
})
@SharedDependency({
        TestcontainersOption.TEST_CONTAINER,
        TestcontainersOption.KAFKA
})
@SharedTestLifeCycleClass(Kafka4UnitServer.class)
public class KafkaStreams_4_x_IT extends KafkaStreamsIT {
    Random random = new Random();

    @Test
    public void streamsProducerSendTest() throws NoSuchMethodException {
        int messageCount = random.nextInt(5) + 1;
        final TestProducer producer = new TestProducer(brokerUrl);

        producer.sendMessage(inputTopic, messageCount, TRACE_TYPE_RECORD);
        KafkaStreamsITBase.verifyStreams(brokerUrl, inputTopic, outputTopic, messageCount);
    }

    @Test
    public void streamsConsumeTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer(brokerUrl);

        producer.sendMessage(inputTopic, 1, TRACE_TYPE_MULTI_RECORDS);
        KafkaStreamsITBase.verifyStreams(brokerUrl, inputTopic, outputTopic, 1);
    }
}
