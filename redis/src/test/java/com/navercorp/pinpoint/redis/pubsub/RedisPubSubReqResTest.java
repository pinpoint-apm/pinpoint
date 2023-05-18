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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static com.navercorp.pinpoint.redis.pubsub.RedisStreamReqResTest.testPubSubServerClient;

/**
 * @author youngjin.kim2
 */
@DisplayName("req/res based on redis pubsub")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RedisPubSubConfig.class})
@Testcontainers
public class RedisPubSubReqResTest {

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379)
            .withReuse(true);

    @Autowired
    private PubSubServerFactory serverFactory;

    @Autowired
    private PubSubClientFactory clientFactory;

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("spring.data.redis.host", redisContainer.getHost());
        System.setProperty("spring.redis.host", redisContainer.getHost());
        System.setProperty("spring.data.redis.port", redisContainer.getMappedPort(6379).toString());
        System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString());
    }

    @DisplayName("req/res based on redis pubsub")
    @Test
    public void testRedisPubSub() {
        testPubSubServerClient(this.serverFactory, this.clientFactory);
    }

}
