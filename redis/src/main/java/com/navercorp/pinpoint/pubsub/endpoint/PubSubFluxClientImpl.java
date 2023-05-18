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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class PubSubFluxClientImpl<D, S> extends PubSubClient<D, S> implements PubSubFluxClient<D, S> {

    public PubSubFluxClientImpl(
            PubSubClientOptions options,
            IdentifierFactory identifierFactory,
            Function<Identifier, PubChannel<DemandMessage<D>>> demandRouter,
            Function<Identifier, SubChannel<SupplyMessage<S>>> supplyRouter
    ) {
        super(options, identifierFactory, demandRouter, supplyRouter);
    }

    @Override
    public Flux<S> request(D demand) {
        final Sinks.Many<S> sink = Sinks.many().multicast().onBackpressureBuffer(4);
        final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>(null);

        final Identifier id = identifierFactory.get();
        final SubChannel<SupplyMessage<S>> supplyChannel = supplyRouter.apply(id);
        final SubConsumer<SupplyMessage<S>> subConsumer = new LongTermSubConsumer<>(sink, id, subscriptionRef);
        subscriptionRef.set(supplyChannel.subscribe(subConsumer));

        demandRouter.apply(id).publish(DemandMessage.ok(id, demand));

        return sink.asFlux();
    }

    static class LongTermSubConsumer<S> implements SubConsumer<SupplyMessage<S>> {

        private static final Comparator<SupplyMessage<?>> supplyComparator =
                Comparator.comparing(el -> el.getSequence());

        final Sinks.Many<S> sink;
        final Identifier demandId;
        final AtomicReference<Subscription> subscriptionRef;

        final Queue<SupplyMessage<S>> supplies = new PriorityQueue<>(supplyComparator);
        int nextSequence = 0;

        LongTermSubConsumer(
                Sinks.Many<S> sink,
                Identifier demandId,
                AtomicReference<Subscription> subscriptionRef
        ) {
            this.sink = sink;
            this.demandId = demandId;
            this.subscriptionRef = subscriptionRef;
        }

        @Override
        public boolean consume(SupplyMessage<S> supply) {
            if (!supply.getDemandId().equals(this.demandId)) {
                return false;
            }

            synchronized (supplies) {
                if (supply.getSequence() == nextSequence) {
                    consume0(supply);
                    nextSequence += 1;
                    while (supplies.peek() != null && supplies.peek().getSequence() == nextSequence) {
                        final SupplyMessage<S> pended = supplies.poll();
                        if (pended != null) {
                            consume0(pended);
                            nextSequence += 1;
                        }
                    }
                } else {
                    supplies.add(supply);
                }
            }
            return true;
        }

        private void consume0(SupplyMessage<S> supply) {
            if (supply.isTerminated()) {
                sink.emitComplete(Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(3)));
                close();
            } else {
                sink.emitNext(supply.getContent(), Sinks.EmitFailureHandler.FAIL_FAST);
            }
        }

        private void close() {
            subscriptionRef.get().unsubscribe();
        }

    }

}
