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
import org.springframework.data.redis.core.ValueOperations;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisKVPubChannel implements PubChannel {

    private final ValueOperations<String, String> ops;
    private final String key;

    RedisKVPubChannel(ValueOperations<String, String> ops, String key) {
        this.ops = Objects.requireNonNull(ops, "ops");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public void publish(byte[] content) {
        this.ops.set(this.key, new String(content));
    }

}
