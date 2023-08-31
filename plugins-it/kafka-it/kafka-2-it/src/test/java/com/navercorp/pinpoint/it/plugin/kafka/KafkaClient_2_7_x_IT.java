/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.kafka;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.kafka.Kafka2UnitServer;
import test.pinpoint.plugin.kafka.TestProducer;

import java.util.Random;

import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_MULTI_RECORDS;
import static test.pinpoint.plugin.kafka.KafkaITConstants.TRACE_TYPE_RECORD;


/**
 * @author Younsung Hwang
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-kafka-client.config")
@Dependency({
        "org.apache.kafka:kafka_2.12:[2.7.0]", "log4j:log4j:[1.2.17]", "commons-io:commons-io:[2.5.0]",
        "org.apache.kafka:kafka-clients:[2.7.0,2.7.max]"
})
@SharedDependency({"org.apache.kafka:kafka_2.12:2.6.0", "org.apache.zookeeper:zookeeper:3.8.1", "io.dropwizard.metrics:metrics-core:4.1.12.1"})
@SharedTestLifeCycleClass(Kafka2UnitServer.class)
public class KafkaClient_2_7_x_IT extends KafkaClient2ITBase {

    @Test
    public void producerSendTest() throws NoSuchMethodException {
        int messageCount = new Random().nextInt(5) + 1;
        final TestProducer producer = new TestProducer();
        producer.sendMessage(brokerUrl, messageCount);
        KafkaClientITBase.verifyProducerSend(brokerUrl, messageCount);
    }

    @Disabled
    @Test
    public void recordEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer();
        producer.sendMessage(brokerUrl, 1, TRACE_TYPE_RECORD);
        KafkaClientITBase.verifySingleConsumerEntryPoint(brokerUrl, offset);
    }

    @Test
    public void recordMultiEntryPointTest() throws NoSuchMethodException {
        final TestProducer producer = new TestProducer();
        producer.sendMessage(brokerUrl, 1, TRACE_TYPE_MULTI_RECORDS);
        KafkaClientITBase.verifyMultiConsumerEntryPoint(brokerUrl);
    }

}
