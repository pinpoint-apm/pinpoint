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
package com.navercorp.pinpoint.redis.pubsub;

import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.pubsub.SubConsumer;
import com.navercorp.pinpoint.pubsub.Subscription;
import com.navercorp.pinpoint.redis.exception.RedisSerializationException;
import org.springframework.core.serializer.Deserializer;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisSubChannel<T> implements SubChannel<T> {

    private final RedisMessageListenerContainer container;
    private final Deserializer<T> deserializer;
    private final Topic topic;

    RedisSubChannel(
            RedisMessageListenerContainer container,
            Deserializer<T> deserializer,
            Topic topic
    ) {
        this.container = Objects.requireNonNull(container, "container");
        this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
        this.topic = Objects.requireNonNull(topic, "topic");
    }

    @Override
    public Subscription subscribe(SubConsumer<T> consumer) {
        final MessageListener listener = wrapConsumer(consumer);
        this.container.addMessageListener(listener, this.topic);
        return new RedisSubscription(this, listener, this.topic);
    }

    private MessageListener wrapConsumer(SubConsumer<T> consumer) {
        return new SubConsumerWrapper<>(consumer, this.deserializer);
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription instanceof RedisSubscription) {
            unsubscribe((RedisSubscription) subscription);
        }
    }

    private void unsubscribe(RedisSubscription subscription) {
        this.container.removeMessageListener(subscription.getListener(), subscription.getTopic());
    }

    private static class SubConsumerWrapper<T> implements MessageListener {

        private final SubConsumer<T> consumer;
        private final Deserializer<T> deserializer;

        public SubConsumerWrapper(
                SubConsumer<T> consumer,
                Deserializer<T> deserializer
        ) {
            this.consumer = consumer;
            this.deserializer = deserializer;
        }

        @Override
        public void onMessage(@NonNull Message message, byte[] pattern) {
            try {
                final T data = deserializer.deserializeFromByteArray(message.getBody());
                this.consumer.consume(data);
            } catch (IOException e) {
                throw new RedisSerializationException("Invalid deserialization");
            }
        }

    }

}
