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
package com.navercorp.pinpoint.redis.stream;

import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.pubsub.SubConsumer;
import com.navercorp.pinpoint.pubsub.Subscription;
import com.navercorp.pinpoint.redis.exception.RedisSerializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.serializer.Deserializer;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisStreamSubChannel<T> implements SubChannel<T>, InitializingBean {

    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveStreamOperations<String, String, String> streamOps;
    private final Consumer consumer;
    private final Deserializer<T> deserializer;
    private final String key;

    RedisStreamSubChannel(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            ReactiveRedisTemplate<String, String> redisTemplate,
            Consumer consumer,
            Deserializer<T> deserializer,
            String key
    ) {
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.streamOps = this.redisTemplate.opsForStream();
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public void afterPropertiesSet() {
        this.listenerContainer.start();
    }

    @Override
    public Subscription subscribe(SubConsumer<T> subConsumer) {
        try {
            this.streamOps.createGroup(this.key, this.consumer.getGroup()).block();
        } catch (Exception ignored) {}

        this.redisTemplate.expire(this.key, Duration.ofMinutes(30)).subscribe();

        final StreamOffset<String> offset = StreamOffset.create(this.key, ReadOffset.lastConsumed());

        final StreamListener<String, MapRecord<String, String, String>> listener = new StreamListenerWrapper<>(
                subConsumer,
                this.deserializer,
                this.streamOps,
                this.consumer.getGroup());

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

    private static class StreamListenerWrapper<V> implements StreamListener<String, MapRecord<String, String, String>> {

        private static final String KEY_CONTENT = "content";

        private final SubConsumer<V> consumer;
        private final Deserializer<V> deserializer;
        private final ReactiveStreamOperations<String, String, String> streamOps;
        private final String group;

        public StreamListenerWrapper(
                SubConsumer<V> consumer,
                Deserializer<V> deserializer,
                ReactiveStreamOperations<String, String, String> streamOps,
                String group
        ) {
            this.consumer = consumer;
            this.deserializer = deserializer;
            this.streamOps = streamOps;
            this.group = group;
        }

        @Override
        public void onMessage(MapRecord<String, String, String> message) {
            try {
                onMessage0(message);
            } catch (IOException e) {
                throw new RedisSerializationException("Invalid deserialization");
            }
        }

        private void onMessage0(MapRecord<String, String, String> message) throws IOException {
            final String content = message.getValue().get(KEY_CONTENT);
            final V item = this.deserializer.deserializeFromByteArray(content.getBytes());
            if (this.consumer.consume(item)) {
                this.streamOps.acknowledge(group, message).subscribe();
            }
        }

    }

}
