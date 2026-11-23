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

package com.navercorp.pinpoint.it.plugin.rabbitmq;

import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycle;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

public class TestBrokerServer implements SharedTestLifeCycle {
    private RabbitMQContainer container;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        container = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"));

        container.start();

        int port = container.getFirstMappedPort();
        Properties properties = new Properties();
        properties.setProperty("PORT", String.valueOf(port));
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
