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
package com.navercorp.pinpoint.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.RedisElementReader;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.lang.NonNull;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public abstract class RedisSubChannel<T> implements SubChannel<T> {

    private final RedisMessageListenerContainer container;
    private final RedisSerializationContext<String, T> serContext;

    public RedisSubChannel(RedisMessageListenerContainer container, RedisSerializationContext<String, T> serContext) {
        this.container = Objects.requireNonNull(container, "container");
        this.serContext = Objects.requireNonNull(serContext, "serContext");
    }

    protected abstract String getChannelBase();

    @Override
    public void subscribe(SubConsumer<T> consumer, String postfix) {
        final MessageListener listener = wrapConsumer(consumer, postfix);
        final Topic topic = getTopic(postfix);

        this.container.addMessageListener(listener, topic);
    }

    @Override
    public void unsubscribe(SubConsumer<T> consumer, String postfix) {
        final MessageListener listener = wrapConsumer(consumer, postfix);
        final Topic topic = getTopic(postfix);

        this.container.removeMessageListener(listener, topic);
    }

    private Topic getTopic(String postfix) {
        final String base = getChannelBase();
        final String channel = appendPostfix(base, postfix);
        if (hasAsterisk(channel)) {
            return PatternTopic.of(channel);
        } else {
            return ChannelTopic.of(channel);
        }
    }

    private static String appendPostfix(String prefix, String postfix) {
        if (postfix == null || postfix.length() == 0) {
            return prefix;
        }
        return prefix + ':' + postfix;
    }

    private static boolean hasAsterisk(String postfix) {
        return postfix != null && postfix.indexOf('*') >= 0;
    }

    private MessageListener wrapConsumer(SubConsumer<T> consumer, String postfix) {
        int baseLength = getChannelBase().length();
        if (postfix != null && postfix.length() > 0) {
            baseLength += 1;
        }
        return new SubConsumerWrapper<>(consumer, this.serContext, baseLength);
    }

    private static class SubConsumerWrapper<T> implements MessageListener {

        private final SubConsumer<T> consumer;
        private final RedisSerializationContext<String, T> serContext;
        private final int channelPrefixLength;

        public SubConsumerWrapper(
                SubConsumer<T> consumer,
                RedisSerializationContext<String, T> serContext,
                int channelPrefixLength
        ) {
            this.consumer = consumer;
            this.serContext = serContext;
            this.channelPrefixLength = channelPrefixLength;
        }

        @Override
        public void onMessage(@NonNull Message message, byte[] pattern) {
            final RedisElementReader<T> reader = this.serContext.getValueSerializationPair().getReader();
            final ByteBuffer body = ByteBuffer.wrap(message.getBody());
            final T demand = reader.read(body);
            final String channel = new String(message.getChannel());
            final String postfix = channel.substring(channelPrefixLength);
            this.consumer.consume(demand, postfix);
        }

    }

}
