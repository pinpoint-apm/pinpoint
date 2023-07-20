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

package com.navercorp.pinpoint.plugin.lettuce;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

public class RedisServer implements SharedTestLifeCycle {
    private GenericContainer<?> redisServer;

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        this.redisServer = new GenericContainer<>(DockerImageName.parse("redis:5.0.14-alpine"))
                .withExposedPorts(6379);
        redisServer.start();

        Properties properties = new Properties();
        properties.setProperty("HOST", redisServer.getHost());
        properties.setProperty("PORT", String.valueOf(redisServer.getMappedPort(6379)));
        return properties;
    }

    @Override
    public void afterAll() {
        if (redisServer != null) {
            redisServer.close();
        }
    }
}
