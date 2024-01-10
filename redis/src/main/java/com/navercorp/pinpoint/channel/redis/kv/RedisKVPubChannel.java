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
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
class RedisKVPubChannel implements PubChannel {

    private final RedisTemplate<String, String> template;
    private final long expireMs;
    private final String key;

    RedisKVPubChannel(RedisTemplate<String, String> template, long expireMs, String key) {
        this.template = Objects.requireNonNull(template, "template");
        this.expireMs = expireMs;
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public void publish(byte[] content) {
        this.template.opsForValue().set(this.key, BytesUtils.toString(content));
        this.template.expire(this.key, expireMs, TimeUnit.MILLISECONDS);
    }

}
