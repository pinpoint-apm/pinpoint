/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.redis.pubsub;

import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static com.navercorp.pinpoint.redis.pubsub.RedisStreamReqResTest.testPubSubServerClient;

/**
 * @author youngjin.kim2
 */
@DisplayName("req/res based on redis pubsub")
@ContextConfiguration(classes = {RedisPubSubConfig.class})
@SpringBootTest
@Testcontainers
public class RedisPubSubReqResTest {

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379)
            .withReuse(true);

    @Autowired
    private PubSubServerFactory serverFactory;

    @Autowired
    private PubSubClientFactory clientFactory;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
        registry.add("spring.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @DisplayName("req/res based on redis pubsub")
    @Test
    public void testRedisPubSub() {
        testPubSubServerClient(this.serverFactory, this.clientFactory);
    }

}
