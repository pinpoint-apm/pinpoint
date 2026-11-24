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
import test.pinpoint.plugin.kafka.TestStream;
import test.pinpoint.plugin.kafka.TestTopics;

import java.util.Properties;

public abstract class KafkaStreamsIT {

    static String brokerUrl;
    static String inputTopic;
    static String outputTopic;

    private static TestStream testStream;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() throws InterruptedException {
        brokerUrl = System.getProperty(KafkaITConstants.BROKER_URL_PROPERTY);
        inputTopic = KafkaITConstants.unique(KafkaITConstants.INPUT_TOPIC);
        outputTopic = KafkaITConstants.unique(KafkaITConstants.OUTPUT_TOPIC);
        TestTopics.create(brokerUrl, inputTopic, outputTopic);

        // The streams application runs here, on the instrumented test classloader, so that its traces are recorded.
        testStream = new TestStream(brokerUrl, inputTopic, outputTopic);
        testStream.start();
    }

    @AfterAll
    public static void afterAll() {
        if (testStream != null) {
            testStream.shutdown();
            testStream = null;
        }
    }

}
