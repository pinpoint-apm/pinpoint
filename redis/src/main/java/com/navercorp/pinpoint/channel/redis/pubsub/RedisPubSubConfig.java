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
package com.navercorp.pinpoint.channel.redis.pubsub;

import com.navercorp.pinpoint.channel.ChannelProvider;
import com.navercorp.pinpoint.channel.ChannelProviderRegistry;
import com.navercorp.pinpoint.channel.PubChannelProvider;
import com.navercorp.pinpoint.channel.SubChannelProvider;
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
    public ExecutorService redisPubSubMessageExecutor() {
        final int processors = Runtime.getRuntime().availableProcessors();
        return RedisUtils.newFixedThreadPool(processors, "RedisPubSubMessageExecutor");
    }

    @Bean("redisMessageListenerContainer")
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            @Qualifier("redisPubSubMessageExecutor") ExecutorService executor
    ) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.setTaskExecutor(executor);
        return container;
    }

    @Bean("redisPubSubChannelProvider")
    public ChannelProviderRegistry redisPubSubChannelProvider(
            ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
            RedisMessageListenerContainer redisMessageListenerContainer
    ) {
        PubChannelProvider pub = new RedisPubChannelProvider(reactiveRedisTemplate);
        SubChannelProvider sub = new RedisSubChannelProvider(redisMessageListenerContainer);
        return ChannelProviderRegistry.of(RedisPubSubConstants.SCHEME, ChannelProvider.pair(pub, sub));
    }

}
