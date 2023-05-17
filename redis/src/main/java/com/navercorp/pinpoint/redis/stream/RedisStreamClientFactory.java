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

import com.google.gson.reflect.TypeToken;
import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.pubsub.endpoint.DemandMessage;
import com.navercorp.pinpoint.pubsub.endpoint.Identifier;
import com.navercorp.pinpoint.pubsub.endpoint.IdentifierFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientFactory;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubClientOptions;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxClient;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxClientImpl;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoClient;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoClientImpl;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.SupplyMessage;
import com.navercorp.pinpoint.serde.GsonDeserializer;
import com.navercorp.pinpoint.serde.GsonSerializer;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class RedisStreamClientFactory implements PubSubClientFactory {

    private final PubSubClientOptions options;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveStreamOperations<String, String, String> streamOps;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final IdentifierFactory identifierFactory;
    private final Consumer consumer;

    public RedisStreamClientFactory(
            PubSubClientOptions options,
            ReactiveRedisTemplate<String, String> redisTemplate,
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            IdentifierFactory identifierFactory,
            Consumer consumer
    ) {
        this.options = Objects.requireNonNull(options, "options");
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.streamOps = this.redisTemplate.opsForStream();
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
        this.identifierFactory = Objects.requireNonNull(identifierFactory, "identifierFactory");
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public <D, S> PubSubMonoClient<D, S> build(PubSubMonoServiceDescriptor<D, S> descriptor) {
        final String name = descriptor.getName();
        final Class<D> demandClass = descriptor.getDemandClass();
        final Class<S> supplyClass = descriptor.getSupplyClass();
        return new PubSubMonoClientImpl<>(
                this.options,
                this.identifierFactory,
                new RedisDemandRouter<>(streamOps, name, demandClass),
                new RedisSupplyRouter<>(listenerContainer, redisTemplate, consumer, name, supplyClass)
        );
    }

    @Override
    public <D, S> PubSubFluxClient<D, S> build(PubSubFluxServiceDescriptor<D, S> descriptor) {
        final String name = descriptor.getName();
        final Class<D> demandClass = descriptor.getDemandClass();
        final Class<S> supplyClass = descriptor.getSupplyClass();
        return new PubSubFluxClientImpl<>(
                this.options,
                this.identifierFactory,
                new RedisDemandRouter<>(streamOps, name, demandClass),
                new RedisSupplyRouter<>(listenerContainer, redisTemplate, consumer, name, supplyClass)
        );
    }

    private static class RedisDemandRouter<D> implements Function<Identifier, PubChannel<DemandMessage<D>>> {

        private final PubChannel<DemandMessage<D>> channel;

        public RedisDemandRouter(
                ReactiveStreamOperations<String, String, String> streamOps,
                String channelName,
                Class<D> demandClass
        ) {
            this.channel = new RedisStreamPubChannel<>(
                    streamOps,
                    new GsonSerializer<>(TypeToken.getParameterized(DemandMessage.class, demandClass).getType()),
                    "demand:" + channelName
            );
        }

        @Override
        public PubChannel<DemandMessage<D>> apply(Identifier identifier) {
            return this.channel;
        }

    }

    private static class RedisSupplyRouter<S> implements Function<Identifier, SubChannel<SupplyMessage<S>>> {

        private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
        private final ReactiveRedisTemplate<String, String> redisTemplate;
        private final Consumer consumer;
        private final String channelName;
        private final Type supplyType;

        private RedisSupplyRouter(
                StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
                ReactiveRedisTemplate<String, String> redisTemplate,
                Consumer consumer,
                String channelName,
                Class<S> supplyClass
        ) {
            this.listenerContainer = listenerContainer;
            this.redisTemplate = redisTemplate;
            this.consumer = consumer;
            this.channelName = channelName;
            this.supplyType = TypeToken.getParameterized(SupplyMessage.class, supplyClass).getType();
        }

        @Override
        public SubChannel<SupplyMessage<S>> apply(Identifier identifier) {
            return new RedisStreamSubChannel<>(
                    this.listenerContainer,
                    this.redisTemplate,
                    this.consumer,
                    new GsonDeserializer<>(this.supplyType),
                    "supply:" + this.channelName + ':' + identifier
            );
        }

    }

}
