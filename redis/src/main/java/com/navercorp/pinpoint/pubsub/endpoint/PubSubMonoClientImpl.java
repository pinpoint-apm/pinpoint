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
package com.navercorp.pinpoint.pubsub.endpoint;

import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.pubsub.SubChannel;
import com.navercorp.pinpoint.pubsub.SubConsumer;
import com.navercorp.pinpoint.pubsub.Subscription;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class PubSubMonoClientImpl<D, S> extends PubSubClient<D, S> implements PubSubMonoClient<D, S> {

    public PubSubMonoClientImpl(
            PubSubClientOptions options,
            IdentifierFactory identifierFactory,
            Function<Identifier, PubChannel<DemandMessage<D>>> demandRouter,
            Function<Identifier, SubChannel<SupplyMessage<S>>> supplyRouter
    ) {
        super(options, identifierFactory, demandRouter, supplyRouter);
    }

    @Override
    public Mono<S> request(D demand) {
        final Sinks.One<S> sink = Sinks.one();
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>(null);

        final Identifier id = identifierFactory.get();
        final SubChannel<SupplyMessage<S>> supplyChannel = supplyRouter.apply(id);
        final SubConsumer<SupplyMessage<S>> subConsumer = new ShortTermSubConsumer<>(sink, id, subscriptionRef);
        subscriptionRef.set(supplyChannel.subscribe(subConsumer));

        demandRouter.apply(id).publish(DemandMessage.ok(id, demand));

        return sink.asMono().timeout(this.options.getRequestTimeout());
    }

    static class ShortTermSubConsumer<S> implements SubConsumer<SupplyMessage<S>> {

        final Sinks.One<S> sink;
        final Identifier demandId;
        final AtomicReference<Subscription> subscriptionRef;

        ShortTermSubConsumer(
                Sinks.One<S> sink,
                Identifier demandId,
                AtomicReference<Subscription> subscriptionRef
        ) {
            this.sink = sink;
            this.demandId = demandId;
            this.subscriptionRef = subscriptionRef;
        }

        @Override
        public boolean consume(SupplyMessage<S> supply) {
            if (supply.getDemandId().equals(this.demandId)) {
                sink.emitValue(supply.getContent(), Sinks.EmitFailureHandler.FAIL_FAST);
                close();
            }
            return true;
        }

        private void close() {
            subscriptionRef.get().unsubscribe();
        }

    }

}
