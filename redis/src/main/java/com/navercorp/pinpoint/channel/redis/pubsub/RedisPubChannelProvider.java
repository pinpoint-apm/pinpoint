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
import com.navercorp.pinpoint.channel.PubChannelProvider;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisPubChannelProvider implements PubChannelProvider {

    private final ReactiveRedisTemplate<String, String> template;

    RedisPubChannelProvider(ReactiveRedisTemplate<String, String> template) {
        this.template = Objects.requireNonNull(template, "template");
    }

    @Override
    public PubChannel getPubChannel(String key) {
        return new RedisPubChannel(this.template, key);
    }

}
