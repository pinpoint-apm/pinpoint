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
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
class RedisPubSubClientFactory implements PubSubClientFactory {

    private final PubSubClientOptions options;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final IdentifierFactory identifierFactory;

    RedisPubSubClientFactory(
            PubSubClientOptions options,
            ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
            RedisMessageListenerContainer redisMessageListenerContainer,
            IdentifierFactory identifierFactory
    ) {
        this.options = options;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.identifierFactory = identifierFactory;
    }

    @Override
    public <D, S> PubSubMonoClient<D, S> build(PubSubMonoServiceDescriptor<D, S> descriptor) {
        final String name = descriptor.getName();
        final Class<D> demandClass = descriptor.getDemandClass();
        final Class<S> supplyClass = descriptor.getSupplyClass();
        return new PubSubMonoClientImpl<>(
                this.options,
                this.identifierFactory,
                new RedisDemandRouter<>(reactiveRedisTemplate, name, demandClass),
                new RedisSupplyRouter<>(redisMessageListenerContainer, name, supplyClass)
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
                new RedisDemandRouter<>(reactiveRedisTemplate, name, demandClass),
                new RedisSupplyRouter<>(redisMessageListenerContainer, name, supplyClass)
        );
    }

    private static class RedisDemandRouter<D> implements Function<Identifier, PubChannel<DemandMessage<D>>> {

        private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
        private final String channelName;
        private final Type demandType;

        public RedisDemandRouter(
                ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
                String channelName,
                Class<D> demandClass
        ) {
            this.reactiveRedisTemplate = reactiveRedisTemplate;
            this.channelName = channelName;
            this.demandType = TypeToken.getParameterized(DemandMessage.class, demandClass).getType();
        }

        @Override
        public PubChannel<DemandMessage<D>> apply(Identifier identifier) {
            return new RedisPubChannel<>(
                    this.reactiveRedisTemplate,
                    new GsonSerializer<>(this.demandType),
                    "demand:" + this.channelName + ':' + identifier
            );
        }

    }

    private static class RedisSupplyRouter<S> implements Function<Identifier, SubChannel<SupplyMessage<S>>> {

        private final RedisMessageListenerContainer redisMessageListenerContainer;
        private final String channelName;
        private final Type supplyType;

        private RedisSupplyRouter(
                RedisMessageListenerContainer redisMessageListenerContainer,
                String channelName,
                Class<S> supplyClass
        ) {
            this.redisMessageListenerContainer = redisMessageListenerContainer;
            this.channelName = channelName;
            this.supplyType = TypeToken.getParameterized(SupplyMessage.class, supplyClass).getType();
        }

        @Override
        public SubChannel<SupplyMessage<S>> apply(Identifier identifier) {
            return new RedisSubChannel<>(
                    this.redisMessageListenerContainer,
                    new GsonDeserializer<>(this.supplyType),
                    ChannelTopic.of("supply:" + this.channelName + ':' + identifier)
            );
        }

    }

}
