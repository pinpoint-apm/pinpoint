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
package com.navercorp.pinpoint.pubsub;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public abstract class RedisPubChannel<T> implements PubChannel<T> {

    private final ReactiveRedisTemplate<String, T> redisTemplate;

    public RedisPubChannel(
            ReactiveRedisConnectionFactory connectionFactory,
            RedisSerializationContext<String, T> serContext
    ) {
        Objects.requireNonNull(connectionFactory, "connectionFactory");
        Objects.requireNonNull(serContext, "serContext");

        this.redisTemplate = new ReactiveRedisTemplate<>(connectionFactory, serContext);
    }

    protected abstract String getChannelBase();

    @Override
    public void publish(T content, String postfix) {
        final String destination = append(getChannelBase(), postfix);
        this.redisTemplate.convertAndSend(destination, content).subscribe();
    }

    private static String append(String dst, String target) {
        if (target == null || target.length() == 0) {
            return dst;
        }
        return dst + ':' + target;
    }

}
