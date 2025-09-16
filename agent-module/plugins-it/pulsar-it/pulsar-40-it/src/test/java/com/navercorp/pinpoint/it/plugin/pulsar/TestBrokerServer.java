/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.pulsar;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;
import test.pinpoint.plugin.pulsar.TestConsumer;

import java.io.IOException;
import java.util.Properties;

/**
 * @author zhouzixin@apache.org
 */
public class TestBrokerServer implements SharedTestLifeCycle {

    private PulsarContainer container;
    private TestConsumer testConsumer;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        container = new PulsarContainer(DockerImageName.parse("apachepulsar/pulsar:4.0.2"));
        container.start();
        String serviceUrl = container.getPulsarBrokerUrl();
        testConsumer = new TestConsumer(serviceUrl);
        testConsumer.start();

        Properties properties = new Properties();
        properties.setProperty("SERVICE_URL", serviceUrl);
        System.getProperties().putAll(properties);
        return properties;
    }

    @Override
    public void afterAll() {
        if (testConsumer != null) {
            try {
                testConsumer.close();
            } catch (IOException ignored) {
                // Nobody cares me.
            }
        }
        if (container != null) {
            container.stop();
        }
    }
}
