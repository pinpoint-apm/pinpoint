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
package com.navercorp.pinpoint.redis.stream;

import com.navercorp.pinpoint.pubsub.endpoint.IdentifierFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientOptions;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServerFactory;
import com.navercorp.pinpoint.redis.RedisBasicConfig;
import com.navercorp.pinpoint.redis.value.RedisValueConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RedisBasicConfig.class, RedisValueConfig.class })
public class RedisStreamConfig {

    @Value("${pinpoint.redis.stream.client.timeout.ms:5000}")
    long clientTimeoutMs;

    @Value("${pinpoint.redis.stream.consumer.group:default}")
    String consumerGroup;

    @Value("${pinpoint.redis.stream.consumer.name:default}")
    String consumerName;

    @Bean("redisStreamMessageExecutor")
    Executor redisPubSubMessageExecutor() {
        return new SimpleAsyncTaskExecutor("redis-stream-message-executor");
    }

    @Bean("redisPubSubClientOptions")
    PubSubClientOptions redisPubSubClientOptions() {
        return PubSubClientOptions.builder()
                .setRequestTimeout(Duration.ofMillis(clientTimeoutMs))
                .build();
    }

    @Bean
    StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            @Qualifier("redisStreamMessageExecutor") Executor executor
    ) {
        final StreamMessageListenerContainer.StreamMessageListenerContainerOptionsBuilder
                <String, MapRecord<String, String, String>> builder =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder();
        builder.serializer(StringRedisSerializer.UTF_8);
        if (executor != null) {
            builder.executor(executor);
        }
        builder.pollTimeout(Duration.ofMillis(100));
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer =
                StreamMessageListenerContainer.create(redisConnectionFactory, builder.build());
        listenerContainer.start();
        return listenerContainer;
    }

    @Bean("redisPubSubClientFactory")
    PubSubClientFactory redisStreamClientFactory(
            @Qualifier("redisPubSubClientOptions") PubSubClientOptions options,
            ReactiveRedisTemplate<String, String> redisTemplate,
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            IdentifierFactory identifierFactory
    ) {
        return new RedisStreamClientFactory(
                options,
                redisTemplate,
                listenerContainer,
                identifierFactory,
                Consumer.from(consumerGroup, consumerName)
        );
    }

    @Bean("redisStreamServerFactory")
    PubSubServerFactory redisStreamServerFactory(
            ReactiveRedisTemplate<String, String> redisTemplate,
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer
    ) {
        return new RedisStreamServerFactory(
                redisTemplate,
                listenerContainer,
                Consumer.from(consumerGroup, consumerName)
        );
    }

}
