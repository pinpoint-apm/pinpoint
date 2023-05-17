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
import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServer;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServerFactory;
import com.navercorp.pinpoint.pubsub.endpoint.SupplyMessage;
import com.navercorp.pinpoint.serde.GsonDeserializer;
import com.navercorp.pinpoint.serde.GsonSerializer;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStreamOperations;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class RedisStreamServerFactory implements PubSubServerFactory {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReactiveStreamOperations<String, String, String> streamOps;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private final Consumer consumer;

    public RedisStreamServerFactory(
            ReactiveRedisTemplate<String, String> redisTemplate,
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer,
            Consumer consumer
    ) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate");
        this.streamOps = this.redisTemplate.opsForStream();
        this.listenerContainer = Objects.requireNonNull(listenerContainer, "listenerContainer");
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public <D, S> PubSubServer build(Function<D, Mono<S>> service, PubSubMonoServiceDescriptor<D, S> descriptor) {
        final String name = descriptor.getName();
        final Class<D> demandClass = descriptor.getDemandClass();
        final Class<S> supplyClass = descriptor.getSupplyClass();
        return PubSubServer.build(
                service,
                makeDemandChannel(name, TypeToken.getParameterized(DemandMessage.class, demandClass).getType()),
                new RedisSupplyRouter<>(this.streamOps, name, supplyClass)
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
                new RedisSupplyRouter<>(this.streamOps, name, supplyClass)
        );
    }

    private <D> SubChannel<DemandMessage<D>> makeDemandChannel(String channelName, Type demandType) {
        return new RedisStreamSubChannel<>(
                this.listenerContainer,
                this.redisTemplate,
                this.consumer,
                new GsonDeserializer<>(demandType),
                "demand:" + channelName
        );
    }

    private static class RedisSupplyRouter<S> implements Function<Identifier, PubChannel<SupplyMessage<S>>> {

        private final ReactiveStreamOperations<String, String, String> streamOps;
        private final String channelName;
        private final Type supplyType;

        private RedisSupplyRouter(
                ReactiveStreamOperations<String, String, String> streamOps,
                String channelName,
                Class<S> supplyClass
        ) {
            this.streamOps = streamOps;
            this.channelName = channelName;
            this.supplyType = TypeToken.getParameterized(SupplyMessage.class, supplyClass).getType();
        }

        @Override
        public PubChannel<SupplyMessage<S>> apply(Identifier identifier) {
            return new RedisStreamPubChannel<>(
                    this.streamOps,
                    new GsonSerializer<>(this.supplyType),
                    "supply:" + this.channelName + ':' + identifier
            );
        }

    }

}
