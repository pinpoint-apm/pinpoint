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
import com.navercorp.pinpoint.channel.SubConsumer;
import com.navercorp.pinpoint.channel.Subscription;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisSubChannel implements SubChannel {

    private final RedisMessageListenerContainer container;
    private final Topic topic;

    RedisSubChannel(RedisMessageListenerContainer container,Topic topic) {
        this.container = Objects.requireNonNull(container, "container");
        this.topic = Objects.requireNonNull(topic, "topic");
    }

    @Override
    public Subscription subscribe(SubConsumer consumer) {
        final MessageListener listener = wrapConsumer(consumer);
        this.container.addMessageListener(listener, this.topic);
        return new RedisSubscription(this, listener, this.topic);
    }

    private MessageListener wrapConsumer(SubConsumer consumer) {
        return new SubConsumerWrapper(consumer);
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription instanceof RedisSubscription redisSubscription) {
            unsubscribe(redisSubscription);
        }
    }

    private void unsubscribe(RedisSubscription subscription) {
        this.container.removeMessageListener(subscription.getListener(), subscription.getTopic());
    }

    private static class SubConsumerWrapper implements MessageListener {

        private final SubConsumer consumer;

        public SubConsumerWrapper(SubConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onMessage(@NonNull Message message, byte[] pattern) {
            this.consumer.consume(message.getBody());
        }

    }

}
