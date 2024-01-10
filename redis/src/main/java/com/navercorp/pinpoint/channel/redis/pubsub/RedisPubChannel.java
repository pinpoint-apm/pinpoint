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

import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisPubChannel implements PubChannel {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final String key;

    RedisPubChannel(ReactiveRedisTemplate<String, String> redisTemplate, String key) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public void publish(byte[] bytes) {
        final String content = BytesUtils.toString(bytes);
        this.redisTemplate.convertAndSend(this.key, content).subscribe();
    }

}
