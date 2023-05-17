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
package com.navercorp.pinpoint.redis.value;

import com.navercorp.pinpoint.pubsub.endpoint.Identifier;
import com.navercorp.pinpoint.pubsub.endpoint.IdentifierFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * @author youngjin.kim2
 */
public class RedisIdentifierFactory implements IdentifierFactory {

    private static final String KEY_IDENTIFIER = "lastIdentifier";

    private final ValueOperations<String, String> valueOps;

    public RedisIdentifierFactory(RedisTemplate<String, String> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    @Override
    public Identifier get() {
        return Identifier.of(Long.toString(getUniqueLong()));
    }

    private long getUniqueLong() {
        final Long id = valueOps.increment(KEY_IDENTIFIER);
        if (id == null) {
            return System.nanoTime();
        }
        return id;
    }

}
