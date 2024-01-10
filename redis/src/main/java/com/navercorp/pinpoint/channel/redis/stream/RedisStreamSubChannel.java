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
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * @author youngjin.kim2
 */
class RedisStreamSubChannel implements SubChannel {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveStreamOperations<String, String, String> streamOps;
    private final String name;
    private final String key;

    RedisStreamSubChannel(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            ReactiveRedisTemplate<String, String> redisTemplate,
            String name,
            String key
    ) {
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.streamOps = this.redisTemplate.opsForStream();
        this.name = Objects.requireNonNull(name, "name");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public Subscription subscribe(SubConsumer subConsumer) {
        String groupName = this.newGroupName();

        Mono<String> group = this.streamOps.createGroup(this.key, groupName);
        Mono<Boolean> expire = this.redisTemplate.expire(this.key, Duration.ofMinutes(30));
        Flux.merge(group, expire).blockLast();

        return new RedisStreamSubscription(this,
                this.listenerContainer.receive(
                        Consumer.from(groupName, groupName),
                        StreamOffset.create(this.key, ReadOffset.lastConsumed()),
                        message -> {
                            byte[] contents = BytesUtils.toBytes(message.getValue().get("content"));
                            if (subConsumer.consume(contents)) {
                                this.streamOps.acknowledge(groupName, message).block();
                            }
                        }
                ),
                groupName
        );
    }

    private String newGroupName() {
        return this.name + '-' + UUID.randomUUID();
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription instanceof RedisStreamSubscription redisStreamSubscription) {
            unsubscribe(redisStreamSubscription);
        }
    }

    private void unsubscribe(RedisStreamSubscription subscription) {
        try {
            this.listenerContainer.remove(subscription.getRedisSubscription());
        } catch (Exception ignored) {
        }
    }

}
