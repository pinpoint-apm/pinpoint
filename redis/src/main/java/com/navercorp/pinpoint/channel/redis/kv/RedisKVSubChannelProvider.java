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

import com.navercorp.pinpoint.channel.SubChannel;
import com.navercorp.pinpoint.channel.SubChannelProvider;
import com.navercorp.pinpoint.common.util.KeyValueTokenizer;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisKVSubChannelProvider implements SubChannelProvider {

    private final RedisTemplate<String, String> template;
    private final Scheduler scheduler;

    RedisKVSubChannelProvider(RedisTemplate<String, String> template, Scheduler scheduler) {
        this.template = Objects.requireNonNull(template, "template");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    @Override
    public SubChannel getSubChannel(String key) {
        KeyValueTokenizer.KeyValue keyValue = KeyValueTokenizer.tokenize(key, ":");
        if (keyValue == null) {
            throw new IllegalArgumentException("the key must contain ':' key:" + key);
        }
        Duration period = Duration.parse(keyValue.getKey());
        return new RedisKVSubChannel(this.template, this.scheduler, period, keyValue.getValue());
    }

}
