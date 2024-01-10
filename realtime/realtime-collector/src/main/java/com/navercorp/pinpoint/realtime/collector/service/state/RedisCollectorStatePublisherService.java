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
package com.navercorp.pinpoint.realtime.collector.service.state;

import com.navercorp.pinpoint.channel.serde.Serde;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.realtime.vo.CollectorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisCollectorStatePublisherService implements CollectorStatePublisherService {

    private final Logger logger = LogManager.getLogger(RedisCollectorStatePublisherService.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final Serde<CollectorState> serde;
    private final String key;
    private final Duration ttl;

    public RedisCollectorStatePublisherService(
            RedisTemplate<String, String> redisTemplate,
            Serde<CollectorState> serde,
            String key,
            Duration ttl
    ) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.serde = Objects.requireNonNull(serde, "serde");
        this.key = Objects.requireNonNull(key, "key");
        this.ttl = Objects.requireNonNull(ttl, "ttl");
    }

    @Override
    public void publish(CollectorState state) {
        try {
            byte[] bytes = this.serde.serializeToByteArray(state);
            this.publishBytes(bytes);
        } catch (Exception e) {
            logger.error("Failed to publish collector state {}", state, e);
        }
    }

    private void publishBytes(byte[] bytes) {
        String value = BytesUtils.toString(bytes);
        this.redisTemplate.opsForValue().set(this.key, value);
        this.redisTemplate.expire(this.key, this.ttl);
    }

}
