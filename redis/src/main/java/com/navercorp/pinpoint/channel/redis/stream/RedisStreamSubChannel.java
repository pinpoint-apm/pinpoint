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
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisStreamSubChannel implements SubChannel {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveStreamOperations<String, String, String> streamOps;
    private final Consumer consumer;
    private final String key;

    RedisStreamSubChannel(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            ReactiveRedisTemplate<String, String> redisTemplate,
            Consumer consumer,
            String key
    ) {
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.streamOps = this.redisTemplate.opsForStream();
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public Subscription subscribe(SubConsumer subConsumer) {
        try {
            this.streamOps.createGroup(this.key, this.consumer.getGroup()).block();
        } catch (Exception ignored) {}

        this.redisTemplate.expire(this.key, Duration.ofMinutes(30)).subscribe();

        final StreamOffset<String> offset = StreamOffset.create(this.key, ReadOffset.lastConsumed());

        final StreamListener<String, MapRecord<String, String, String>> listener = new StreamListenerWrapper(
                subConsumer,
                this.streamOps,
                this.consumer.getGroup()
        );

        final org.springframework.data.redis.stream.Subscription redisStreamSubscription =
                this.listenerContainer.receive(this.consumer, offset, listener);

        return new RedisStreamSubscription(this, redisStreamSubscription);
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription instanceof RedisStreamSubscription) {
            unsubscribe((RedisStreamSubscription) subscription);
        }
    }

    private void unsubscribe(RedisStreamSubscription subscription) {
        this.listenerContainer.remove(subscription.getRedisSubscription());
    }

    private static class StreamListenerWrapper implements StreamListener<String, MapRecord<String, String, String>> {

        private static final String KEY_CONTENT = "content";

        private final SubConsumer consumer;
        private final ReactiveStreamOperations<String, String, String> streamOps;
        private final String group;

        public StreamListenerWrapper(
                SubConsumer consumer,
                ReactiveStreamOperations<String, String, String> streamOps,
                String group
        ) {
            this.consumer = consumer;
            this.streamOps = streamOps;
            this.group = group;
        }

        @Override
        public void onMessage(MapRecord<String, String, String> message) {
            String content = message.getValue().get(KEY_CONTENT);
            if (this.consumer.consume(content.getBytes())) {
                this.streamOps.acknowledge(group, message).subscribe();
            }
        }

    }

}
