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
package com.navercorp.pinpoint.channel.redis.stream;

import com.navercorp.pinpoint.channel.SubChannel;
import com.navercorp.pinpoint.channel.SubChannelProvider;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisStreamSubChannelProvider implements SubChannelProvider, InitializingBean {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final String name;

    RedisStreamSubChannelProvider(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            ReactiveRedisTemplate<String, String> redisTemplate,
            String name
    ) {
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.name = Objects.requireNonNull(name, "name");
    }

    @Override
    public SubChannel getSubChannel(String key) {
        return new RedisStreamSubChannel(
                this.listenerContainer,
                this.redisTemplate,
                this.name,
                key
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.listenerContainer.start();
    }

}
