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
package com.navercorp.pinpoint.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author youngjin.kim2
 */
public class MemoryChannelProvider implements ChannelProvider {

    private final Map<String, Channel> repo = new HashMap<>();

    @Override
    public PubChannel getPubChannel(String key) {
        return this.getChannel(key);
    }

    @Override
    public SubChannel getSubChannel(String key) {
        return this.getChannel(key);
    }

    private Channel getChannel(String key) {
        return repo.computeIfAbsent(key, k -> new MemChannel());
    }

    private static class MemChannel implements Channel {

        private final Set<SubConsumer> consumers = new HashSet<>();

        @Override
        public void publish(byte[] content) {
            for (SubConsumer consumer: this.consumers) {
                consumer.consume(content);
            }
        }

        @Override
        public Subscription subscribe(SubConsumer consumer) {
            this.consumers.add(consumer);
            return new MemChannelSubscription(this, consumer);
        }

        @Override
        public void unsubscribe(Subscription subscription) {
            if (subscription instanceof MemChannelSubscription memSubscription) {
                this.consumers.remove(memSubscription.getConsumer());
            } else {
                throw new IllegalArgumentException("Illegal subscription");
            }
        }

        private static class MemChannelSubscription extends AbstractSubscription {
            private final SubConsumer consumer;

            public MemChannelSubscription(SubChannel channel, SubConsumer consumer) {
                super(channel);
                this.consumer = consumer;
            }

            public SubConsumer getConsumer() {
                return consumer;
            }

        }

    }

}
