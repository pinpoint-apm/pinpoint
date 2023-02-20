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
package com.navercorp.pinpoint.web.realtime.atc.dao.redis;

import com.navercorp.pinpoint.web.realtime.atc.dao.CountingMetricDao;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class RedisCountingMetricDao implements CountingMetricDao {

    private static final String countATCDemandKey = "metric:count:atc:demand";

    private final ReactiveValueOperations<String, String> opsForValue;

    public RedisCountingMetricDao(ReactiveRedisConnectionFactory connectionFactory) {
        Objects.requireNonNull(connectionFactory, "connectionFactory");
        final RedisSerializationContext<String, String> serContext = RedisSerializationContext.string();
        this.opsForValue = new ReactiveRedisTemplate<>(connectionFactory, serContext).opsForValue();
    }

    @Override
    public void incrementCountATCDemand() {
        this.opsForValue.increment(countATCDemandKey).subscribe();
    }

}
