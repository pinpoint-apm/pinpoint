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
package com.navercorp.pinpoint.channel.redis.kv;

import com.navercorp.pinpoint.channel.SubChannel;
import com.navercorp.pinpoint.channel.SubConsumer;
import com.navercorp.pinpoint.channel.Subscription;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class RedisKVSubChannel implements SubChannel {

    private final RedisTemplate<String, String> template;
    private final Scheduler scheduler;
    private final Duration pollPeriod;
    private final String key;

    private final List<KVSubscription> subscriptions = new ArrayList<>(2);
    private volatile Disposable disposePolling = null;

    RedisKVSubChannel(RedisTemplate<String, String> template, Scheduler scheduler, Duration pollPeriod, String key) {
        this.template = Objects.requireNonNull(template, "template");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.pollPeriod = Objects.requireNonNull(pollPeriod, "pollPeriod");
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public Subscription subscribe(SubConsumer consumer) {
        Objects.requireNonNull(consumer, "consumer");
        KVSubscription subscription = new KVSubscription(consumer);
        synchronized (this.subscriptions) {
            this.subscriptions.add(subscription);
            startPolling();
        }
        return null;
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        if (subscription instanceof KVSubscription kvSubscription) {
            unsubscribe0(kvSubscription);
        }
    }

    private void unsubscribe0(KVSubscription subscription) {
        synchronized (this.subscriptions) {
            this.subscriptions.remove(subscription);
            stopPolling();
        }
    }

    private void startPolling() {
        if (disposePolling != null) {
            return;
        }
        this.disposePolling = Flux.interval(this.pollPeriod, this.scheduler).subscribe(this::broadcast);
    }

    private void stopPolling() {
        if (this.disposePolling != null) {
            this.disposePolling.dispose();
            this.disposePolling = null;
        }
    }

    private void broadcast(long tick) {
        String value = this.template.opsForValue().get(this.key);
        if (value == null) {
            return;
        }
        byte[] bytes = BytesUtils.toBytes(value);
        for (SubConsumer consumer: this.getConsumers()) {
            consumer.consume(bytes);
        }
    }

    private List<SubConsumer> getConsumers() {
        synchronized (this.subscriptions) {
            List<SubConsumer> consumers = new ArrayList<>(this.subscriptions.size());
            for (KVSubscription subscription: this.subscriptions) {
                consumers.add(subscription.getConsumer());
            }
            return consumers;
        }
    }

    private class KVSubscription implements Subscription {

        private final SubConsumer consumer;

        KVSubscription(SubConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void unsubscribe() {
            RedisKVSubChannel.this.unsubscribe(this);
        }

        public SubConsumer getConsumer() {
            return this.consumer;
        }

    }

}
