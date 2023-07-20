/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.kafka.KafkaStreamsUnitServer;
import test.pinpoint.plugin.kafka.TestProducer;

import java.util.Random;

import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_MULTI_RECORDS;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;

@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-kafka-client.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-kafka-plugin"})
@Dependency({
        "org.apache.kafka:kafka_2.12:[2.5.0]", "log4j:log4j:[1.2.17]", "commons-io:commons-io:[2.5.0]",
        "org.apache.kafka:kafka-clients:[2.5.0]", "org.apache.kafka:kafka-streams:[2.5.0,2.5.max]",
        TestcontainersOption.TEST_CONTAINER, TestcontainersOption.KAFKA
})
@JvmVersion(8)
@SharedTestLifeCycleClass(KafkaStreamsUnitServer.class)
public class KafkaStreams_2_5_x_IT extends KafkaStreamsIT {
    @Test
    public void streamsProducerSendTest() throws NoSuchMethodException {
        int messageCount = new Random().nextInt(5) + 1;
        final TestProducer producer = new TestProducer();

        producer.sendMessageForStream(brokerUrl, messageCount, TRACE_TYPE_RECORD);
        KafkaStreamsITBase.verifyProducerSend(brokerUrl, messageCount);
    }

    @Test
    public void streamsConsumeTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer();
        producer.sendMessageForStream(brokerUrl, 1, TRACE_TYPE_MULTI_RECORDS);
        KafkaStreamsITBase.verifyMultiConsumerEntryPoint(brokerUrl);
    }
}
