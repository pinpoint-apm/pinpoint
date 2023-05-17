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

import com.navercorp.pinpoint.pubsub.endpoint.IdentifierFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientOptions;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServerFactory;
import com.navercorp.pinpoint.redis.RedisBasicConfig;
import com.navercorp.pinpoint.redis.RedisUtils;
import com.navercorp.pinpoint.redis.value.RedisValueConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RedisBasicConfig.class, RedisValueConfig.class })
public class RedisPubSubConfig {

    @Value("${pinpoint.redis.pubsub.client.timeout.ms:5000}")
    long clientTimeoutMs;

    @Bean("redisPubSubMessageExecutor")
    ExecutorService redisPubSubMessageExecutor() {
        final int processors = Runtime.getRuntime().availableProcessors();
        return RedisUtils.newFixedThreadPool(processors, "RedisPubSubMessageExecutor");
    }

    @Bean("redisPubSubClientOptions")
    PubSubClientOptions redisPubSubClientOptions() {
        return PubSubClientOptions.builder()
                .setRequestTimeout(Duration.ofMillis(clientTimeoutMs))
                .build();
    }

    @Bean("redisMessageListenerContainer")
    RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            @Qualifier("redisPubSubMessageExecutor") ExecutorService executor
    ) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.setTaskExecutor(executor);
        return container;
    }

    @Bean("redisPubSubClientFactory")
    PubSubClientFactory redisPubSubClientFactory(
            @Qualifier("redisPubSubClientOptions") PubSubClientOptions options,
            ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
            RedisMessageListenerContainer redisMessageListenerContainer,
            IdentifierFactory identifierFactory
    ) {
        return new RedisPubSubClientFactory(
                options,
                reactiveRedisTemplate,
                redisMessageListenerContainer,
                identifierFactory
        );
    }

    @Bean("redisPubSubServerFactory")
    PubSubServerFactory redisPubSubServerFactory(
            ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
            RedisMessageListenerContainer redisMessageListenerContainer
    ) {
        return new RedisPubSubServerFactory(reactiveRedisTemplate, redisMessageListenerContainer);
    }

}
