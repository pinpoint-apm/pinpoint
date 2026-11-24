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

package test.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycle;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.kafka.KafkaContainer;

import java.util.Properties;

/**
 * Starts the broker only.
 * <p>
 * The shared classloader this class runs on is not instrumented by the agent, so the consumer and the streams
 * application under test must NOT be started here. They are started by the IT class itself
 * (see {@link TestConsumer}, {@link TestStream}).
 */
public class Kafka4UnitServer implements SharedTestLifeCycle {

    private KafkaContainer container;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        container = new KafkaContainer("apache/kafka:4.2.2");
        container.start();

        Properties properties = new Properties();
        properties.setProperty(KafkaITConstants.BROKER_URL_PROPERTY, container.getBootstrapServers());
        System.getProperties().putAll(properties);
        return properties;
    }

    @Override
    public void afterAll() {
        if (container != null) {
            container.stop();
        }
    }
}
