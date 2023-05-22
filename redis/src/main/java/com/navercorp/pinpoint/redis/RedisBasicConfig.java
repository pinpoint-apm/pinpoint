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

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;

/**
 * @author youngjin.kim2
 */
@Configuration(proxyBeanMethods = false)
@Import({ RedisPropertySources.class })
public class RedisBasicConfig {

    @Value("${spring.data.redis.username:default}")
    String username = "default";

    @Value("${spring.data.redis.password:@null}")
    String password;

    @Value("${spring.data.redis.host:localhost}")
    String host;

    @Value("${spring.data.redis.port:6379}")
    int port;

    @Value("${spring.data.redis.cluster.nodes}")
    List<String> clusterNodes;
    @Value("${spring.data.redis.lettuce.client.io-thread-pool-size:8}")
    int lettuceIOThreadPoolSize;
    @Value("${spring.data.redis.lettuce.client.computation-thread-pool-size:8}")
    int lettuceComputationThreadPoolSize;
    @Value("${spring.data.redis.lettuce.client.name:lettuceClient}")
    String lettuceClientName;
    @Value("${spring.data.redis.lettuce.client.request-queue-size:1024}")
    int lettuceRequestQueueSize;

    @Bean
    RedisTemplate<String, String> redisStringToStringTemplate(RedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.string());
        return template;
    }

    @Bean("reactiveRedisTemplate")
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisTemplate<>(connectionFactory, RedisSerializationContext.string());
    }

    @Bean
    RedisConfiguration redisConfiguration() {
        if (clusterNodes == null || clusterNodes.isEmpty()) {
            Assert.hasText(host, "host is required for redis-standalone mode");

            final RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
            config.setUsername(username);
            config.setPassword(password);
            return config;
        }

        final RedisClusterConfiguration config = new RedisClusterConfiguration(clusterNodes);
        config.setUsername(username);
        config.setPassword(password);
        return config;
    }

    @Bean
    LettuceClientConfiguration lettuceClientConfiguration() {
        final ClientResources clientResources = ClientResources.builder()
                .ioThreadPoolSize(lettuceIOThreadPoolSize)
                .computationThreadPoolSize(lettuceComputationThreadPoolSize)
                .build();

        final SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        final TimeoutOptions timeoutOptions = TimeoutOptions.builder()
                .connectionTimeout()
                .build();

        final ClientOptions clientOptions = ClientOptions.builder()
                .autoReconnect(true)
                .publishOnScheduler(false)
                .requestQueueSize(lettuceRequestQueueSize)
                .socketOptions(socketOptions)
                .timeoutOptions(timeoutOptions)
                .build();

        return LettuceClientConfiguration.builder()
                .clientName(lettuceClientName)
                .clientResources(clientResources)
                .clientOptions(clientOptions)
                .build();
    }

    @Bean
    LettuceConnectionFactory redisConnectionFactory(
            RedisConfiguration redisConfig,
            LettuceClientConfiguration clientConfiguration
    ) {
        return new LettuceConnectionFactory(redisConfig, clientConfiguration);
    }

}
