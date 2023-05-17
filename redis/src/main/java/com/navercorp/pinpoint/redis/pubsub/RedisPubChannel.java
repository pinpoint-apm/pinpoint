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

import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.redis.exception.RedisSerializationException;
import org.springframework.core.serializer.Serializer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.io.IOException;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisPubChannel<T> implements PubChannel<T> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final Serializer<T> serializer;
    private final String key;

    RedisPubChannel(
            ReactiveRedisTemplate<String, String> redisTemplate,
            Serializer<T> serializer,
            String key
    ) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.serializer = Objects.requireNonNull(serializer, "serializer");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public void publish(T item) {
        try {
            final byte[] bytes = this.serializer.serializeToByteArray(item);
            final String content = new String(bytes);
            this.redisTemplate.convertAndSend(this.key, content).subscribe();
        } catch (IOException e) {
            throw new RedisSerializationException("Invalid serialization for publishing");
        }
    }

}
