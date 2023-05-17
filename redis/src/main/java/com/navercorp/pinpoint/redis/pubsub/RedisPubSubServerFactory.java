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
import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServer;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServerFactory;
import com.navercorp.pinpoint.pubsub.endpoint.SupplyMessage;
import com.navercorp.pinpoint.serde.GsonDeserializer;
import com.navercorp.pinpoint.serde.GsonSerializer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
class RedisPubSubServerFactory implements PubSubServerFactory {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    RedisPubSubServerFactory(
            ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
            RedisMessageListenerContainer redisMessageListenerContainer
    ) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    @Override
    public <D, S> PubSubServer build(Function<D, Mono<S>> service, PubSubMonoServiceDescriptor<D, S> descriptor) {
        final String name = descriptor.getName();
        final Class<D> demandClass = descriptor.getDemandClass();
        final Class<S> supplyClass = descriptor.getSupplyClass();
        return PubSubServer.build(
                service,
                makeDemandChannel(name, TypeToken.getParameterized(DemandMessage.class, demandClass).getType()),
                new RedisSupplyRouter<>(this.reactiveRedisTemplate, name, supplyClass)
        );
    }

    @Override
    public <D, S> PubSubServer build(Function<D, Flux<S>> service, PubSubFluxServiceDescriptor<D, S> descriptor) {
        final String name = descriptor.getName();
        final Class<D> demandClass = descriptor.getDemandClass();
        final Class<S> supplyClass = descriptor.getSupplyClass();
        return PubSubServer.buildLongTerm(
                service,
                makeDemandChannel(name, TypeToken.getParameterized(DemandMessage.class, demandClass).getType()),
                new RedisSupplyRouter<>(this.reactiveRedisTemplate, name, supplyClass)
        );
    }

    private <D> SubChannel<DemandMessage<D>> makeDemandChannel(String channelName, Type demandType) {
        return new RedisSubChannel<>(
                redisMessageListenerContainer,
                new GsonDeserializer<>(demandType),
                PatternTopic.of("demand:" + channelName + ":*")
        );
    }

    private static class RedisSupplyRouter<S> implements Function<Identifier, PubChannel<SupplyMessage<S>>> {

        private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
        private final String channelName;
        private final Type supplyType;

        private RedisSupplyRouter(
                ReactiveRedisTemplate<String, String> reactiveRedisTemplate,
                String channelName,
                Class<S> supplyClass
        ) {
            this.reactiveRedisTemplate = reactiveRedisTemplate;
            this.channelName = channelName;
            this.supplyType = TypeToken.getParameterized(SupplyMessage.class, supplyClass).getType();
        }

        @Override
        public PubChannel<SupplyMessage<S>> apply(Identifier identifier) {
            return new RedisPubChannel<>(
                    this.reactiveRedisTemplate,
                    new GsonSerializer<>(this.supplyType),
                    "supply:" + this.channelName + ':' + identifier
            );
        }

    }

}
