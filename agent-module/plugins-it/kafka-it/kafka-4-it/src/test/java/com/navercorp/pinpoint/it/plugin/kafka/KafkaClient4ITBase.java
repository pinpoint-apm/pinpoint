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

import com.navercorp.pinpoint.test.plugin.api.SharedTestBeforeAllResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import test.pinpoint.plugin.kafka.KafkaITConstants;
import test.pinpoint.plugin.kafka.TestConsumer;
import test.pinpoint.plugin.kafka.TestTopics;

import java.util.Properties;

public abstract class KafkaClient4ITBase {

    static String brokerUrl;
    // consumed with group.protocol=classic (the default, ClassicKafkaConsumer)
    static String topic;
    // consumed with group.protocol=consumer (KIP-848, AsyncKafkaConsumer)
    static String consumerProtocolTopic;

    private static TestConsumer testConsumer;
    private static TestConsumer consumerProtocolTestConsumer;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() {
        brokerUrl = System.getProperty(KafkaITConstants.BROKER_URL_PROPERTY);
        topic = KafkaITConstants.unique(KafkaITConstants.TOPIC);
        consumerProtocolTopic = KafkaITConstants.unique(KafkaITConstants.TOPIC);
        TestTopics.create(brokerUrl, topic, consumerProtocolTopic);

        // The consumers run here, on the instrumented test classloader, so that their traces are recorded.
        testConsumer = new TestConsumer(brokerUrl, topic, "classic");
        testConsumer.start();
        consumerProtocolTestConsumer = new TestConsumer(brokerUrl, consumerProtocolTopic, "consumer");
        consumerProtocolTestConsumer.start();
    }

    @AfterAll
    public static void afterAll() throws InterruptedException {
        if (testConsumer != null) {
            testConsumer.shutdown();
            testConsumer = null;
        }
        if (consumerProtocolTestConsumer != null) {
            consumerProtocolTestConsumer.shutdown();
            consumerProtocolTestConsumer = null;
        }
    }
}
