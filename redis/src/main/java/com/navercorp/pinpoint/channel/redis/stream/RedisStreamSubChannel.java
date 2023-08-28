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
import com.navercorp.pinpoint.channel.SubConsumer;
import com.navercorp.pinpoint.channel.Subscription;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author youngjin.kim2
 */
class RedisStreamSubChannel implements SubChannel {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveStreamOperations<String, String, String> streamOps;
    private final String name;
    private final AtomicLong idCounter;
    private final String key;

    RedisStreamSubChannel(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            ReactiveRedisTemplate<String, String> redisTemplate,
            String name,
            AtomicLong idCounter,
            String key
    ) {
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.streamOps = this.redisTemplate.opsForStream();
        this.name = Objects.requireNonNull(name, "name");
        this.idCounter = Objects.requireNonNull(idCounter, "idCounter");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public Subscription subscribe(SubConsumer subConsumer) {
        String groupName = this.newGroupName();

        this.streamOps.createGroup(this.key, groupName)
                .flatMap(g -> this.redisTemplate.expire(this.key, Duration.ofMinutes(30)))
                .block();

        return new RedisStreamSubscription(this,
                this.listenerContainer.receive(
                        Consumer.from(groupName, groupName),
                        StreamOffset.create(this.key, ReadOffset.lastConsumed()),
                        message -> {
                            if (subConsumer.consume(message.getValue().get("content").getBytes())) {
                                this.streamOps.acknowledge(groupName, message).block();
                            }
                        }
                ),
                groupName
        );
    }

    private String newGroupName() {
        return this.name + '-' + this.idCounter.incrementAndGet();
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription instanceof RedisStreamSubscription) {
            unsubscribe((RedisStreamSubscription) subscription);
        }
    }

    private void unsubscribe(RedisStreamSubscription subscription) {
        this.listenerContainer.remove(subscription.getRedisSubscription());
        this.streamOps.destroyGroup(this.key, subscription.getGroupName()).subscribe();
    }

}
