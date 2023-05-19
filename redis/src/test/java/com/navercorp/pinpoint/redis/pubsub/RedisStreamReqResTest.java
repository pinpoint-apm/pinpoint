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
import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServerFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServiceDescriptor;
import com.navercorp.pinpoint.redis.stream.RedisStreamConfig;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author youngjin.kim2
 */
@DisplayName("req/res based on redis stream")
@ContextConfiguration(classes = {RedisStreamConfig.class})
@SpringBootTest
@Testcontainers
public class RedisStreamReqResTest {

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

    @DisplayName("req/res based on redis stream")
    @Test
    public void testRedisStreamPubSub() {
        testPubSubServerClient(this.serverFactory, this.clientFactory);
    }

    static void testPubSubServerClient(PubSubServerFactory serverFactory, PubSubClientFactory clientFactory) {
        final PubSubMonoServiceDescriptor<String, String> greeterService =
                PubSubServiceDescriptor.mono("greeter", String.class, String.class);
        serverFactory.build(name -> Mono.just("Hello, " + name), greeterService).afterPropertiesSet();
        assertThat(syncRequestMono(clientFactory, greeterService, "World")).isEqualTo("Hello, World");

        final PubSubMonoServiceDescriptor<Integer, Integer> squareService =
                PubSubServiceDescriptor.mono("square", Integer.class, Integer.class);
        serverFactory.build(el -> Mono.just(el * el), squareService).afterPropertiesSet();
        assertThat(syncRequestMono(clientFactory, squareService, 22)).isEqualTo(484);

        final PubSubFluxServiceDescriptor<Integer, Integer> rangeService =
                PubSubServiceDescriptor.flux("range", Integer.class, Integer.class);
        serverFactory.build(el -> Flux.range(0, el), rangeService).afterPropertiesSet();
        assertThat(syncRequestFlux(clientFactory, rangeService, 5)).isEqualTo(List.of(0, 1, 2, 3, 4));
        assertThat(syncRequestFlux(clientFactory, rangeService, 3)).isEqualTo(List.of(0, 1, 2));
    }

    static <D, S> S syncRequestMono(
            PubSubClientFactory clientFactory,
            PubSubMonoServiceDescriptor<D, S> descriptor,
            D demand
    ) {
        return clientFactory.build(descriptor)
                .request(demand)
                .block();
    }

    static <D, S> List<S> syncRequestFlux(
            PubSubClientFactory clientFactory,
            PubSubFluxServiceDescriptor<D, S> descriptor,
            D demand
    ) {
        return clientFactory.build(descriptor)
                .request(demand)
                .collectList()
                .timeout(Duration.ofSeconds(30))
                .block();
    }

}
