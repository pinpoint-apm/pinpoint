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
package com.navercorp.pinpoint.channel.redis.kv;

import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.channel.PubChannelProvider;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisKVPubChannelProvider implements PubChannelProvider {

    private final RedisTemplate<String, String> template;

    RedisKVPubChannelProvider(RedisTemplate<String, String> template) {
        this.template = Objects.requireNonNull(template, "template");
    }

    @Override
    public PubChannel getPubChannel(String key) {
        String[] words = key.split(":", 2);
        if (words.length != 2) {
            throw new IllegalArgumentException("the key must contain expire duration");
        }
        Duration expire = Duration.parse(words[0]);
        return new RedisKVPubChannel(this.template, expire.toMillis(), words[1]);
    }

}
