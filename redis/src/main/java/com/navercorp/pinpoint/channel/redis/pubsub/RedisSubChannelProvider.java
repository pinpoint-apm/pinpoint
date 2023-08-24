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

import com.navercorp.pinpoint.channel.SubChannel;
import com.navercorp.pinpoint.channel.SubChannelProvider;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisSubChannelProvider implements SubChannelProvider {

    private final RedisMessageListenerContainer listenerContainer;

    RedisSubChannelProvider(RedisMessageListenerContainer listenerContainer) {
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
    }

    @Override
    public SubChannel getSubChannel(String key) {
        return new RedisSubChannel(this.listenerContainer, topicOf(key));
    }

    private static Topic topicOf(String key) {
        if (key.indexOf('*') >= 0) {
            return PatternTopic.of(key);
        }
        return ChannelTopic.of(key);
    }

}
