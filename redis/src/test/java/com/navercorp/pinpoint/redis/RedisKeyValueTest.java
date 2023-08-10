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
package com.navercorp.pinpoint.redis;

import com.navercorp.pinpoint.channel.ChannelSpringConfig;
import com.navercorp.pinpoint.redis.value.RedisValueConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
@DisplayName("redis key-value test")
@ContextConfiguration(classes = {ChannelSpringConfig.class, RedisValueConfig.class})
@SpringBootTest
@Testcontainers
public class RedisKeyValueTest {

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7.0"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    @Autowired
    private RedisTemplate<String, String> template;

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
        registry.add("spring.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @DisplayName("redis key-value should work")
    @Test
    public void testRedisKeyValue() {
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set("hello", "world");
        String world = ops.get("hello");
        assertThat(world).isEqualTo("world");
    }

}
