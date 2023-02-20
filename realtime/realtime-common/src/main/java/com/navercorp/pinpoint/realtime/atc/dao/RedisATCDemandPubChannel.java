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
package com.navercorp.pinpoint.realtime.atc.dao;

import com.navercorp.pinpoint.pubsub.RedisPubChannel;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author youngjin.kim2
 */
public class RedisATCDemandPubChannel extends RedisPubChannel<ATCDemand> {

    public RedisATCDemandPubChannel(ReactiveRedisConnectionFactory connectionFactory) {
        super(connectionFactory, getSerdeContext());
    }

    static RedisSerializationContext<String, ATCDemand> getSerdeContext() {
        final RedisSerializer<ATCDemand> demandSer = new Jackson2JsonRedisSerializer<>(ATCDemand.class);
        return RedisSerializationContext
                .<String, ATCDemand>newSerializationContext()
                .key(RedisSerializer.string())
                .value(demandSer)
                .hashKey(RedisSerializer.string())
                .hashValue(demandSer)
                .build();
    }

    @Override
    protected String getChannelBase() {
        return "demand:atc";
    }

}
