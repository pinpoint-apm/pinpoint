/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

class RedisServerTest {
    private static GenericContainer<?> redisServer;

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        redisServer = new GenericContainer<>(DockerImageName.parse("redis:5.0.14-alpine"))
                .withExposedPorts(6379);
        redisServer.start();

        Properties properties = new Properties();
        properties.setProperty("HOST", redisServer.getHost());
        properties.setProperty("PORT", String.valueOf(redisServer.getMappedPort(6379)));
        System.out.println(properties);
    }

    @AfterAll
    public static void afterAll() {
        if (redisServer != null) {
            redisServer.close();
        }
    }

    @Test
    public void test() throws Exception {
        System.out.println("TEST");
    }
}