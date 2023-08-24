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

package test.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.streams.errors.StreamsException;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import static test.pinpoint.plugin.kafka.KafkaITConstants.INPUT_TOPIC;
import static test.pinpoint.plugin.kafka.KafkaITConstants.OUTPUT_TOPIC;

/**
 * Copy of https://github.com/chbatey/kafka-unit/blob/master/src/main/java/info/batey/kafka/unit/KafkaUnit.java
 * Some codes have been modified for testing from the copied code.
 */
public class KafkaStreamsUnitServer implements SharedTestLifeCycle {

    private KafkaContainer container;
    private TestStream TEST_STREAM;

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

        container.start();
        int port = container.getFirstMappedPort();

        String brokerUrl = "localhost:" + port;

        createTopics(brokerUrl);

        TEST_STREAM = new TestStream(brokerUrl);
        TEST_STREAM.start();
        System.out.println();

        Properties properties = new Properties();
        properties.setProperty("PORT", String.valueOf(port));
        return properties;
    }

    private static void createTopics(String brokerUrl) {
        List<NewTopic> toCreate = new ArrayList<>();
        toCreate.add(new NewTopic(INPUT_TOPIC, 1, (short) 1));
        toCreate.add(new NewTopic(OUTPUT_TOPIC, 1, (short) 1));

        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", brokerUrl);

        AdminClient.create(config).createTopics(toCreate);
    }

    @Override
    public void afterAll() {
        if (TEST_STREAM != null) {
            try {
                TEST_STREAM.shutdown();
            } catch (StreamsException e) {
            }
        }

        if (container != null) {
            container.stop();
        }
    }
}
