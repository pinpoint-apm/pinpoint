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
import org.springframework.lang.NonNull;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
class PubSubServerImpl<D, S> implements PubSubServer {

    private final Function<D, Mono<S>> monoService;
    private final Function<D, Flux<S>> fluxService;
    private final SubChannel<DemandMessage<D>> demandChannel;
    private final Function<Identifier, PubChannel<SupplyMessage<S>>> supplyRouter;

    PubSubServerImpl(
            Function<D, Mono<S>> monoService,
            Function<D, Flux<S>> fluxService,
            SubChannel<DemandMessage<D>> demandChannel,
            Function<Identifier, PubChannel<SupplyMessage<S>>> supplyRouter
    ) {
        this.monoService = monoService;
        this.fluxService = fluxService;
        this.demandChannel = Objects.requireNonNull(demandChannel, "demandChannel");
        this.supplyRouter = Objects.requireNonNull(supplyRouter, "supplyRouter");
    }

    @Override
    public void start() {
        if (this.fluxService != null) {
            this.demandChannel.subscribe(new LongDemandConsumer());
        } else {
            this.demandChannel.subscribe(new ShortDemandConsumer());
        }
    }

    private class ShortDemandConsumer implements SubConsumer<DemandMessage<D>> {

        @Override
        public boolean consume(DemandMessage<D> demand) {
            try {
                return responseToDemand(demand);
            } catch (Exception e) {
                throw new RuntimeException("Failed to supply for demand: " + demand.getContent(), e);
            }
        }

        private boolean responseToDemand(DemandMessage<D> demand) {
            final Mono<S> mono = monoService.apply(demand.getContent());
            if (mono != null) {
                final Identifier demandId = demand.getId();
                mono.subscribe(new ShortResponseSubscriber(supplyRouter.apply(demandId), demandId));
                return true;
            }
            return false;
        }

    }

    private class ShortResponseSubscriber extends BaseSubscriber<S> {

        protected final PubChannel<SupplyMessage<S>> pubChannel;
        protected final Identifier demandId;

        ShortResponseSubscriber(PubChannel<SupplyMessage<S>> pubChannel, Identifier demandId) {
            this.pubChannel = pubChannel;
            this.demandId = demandId;
        }

        @Override
        protected void hookOnNext(@NonNull S supply) {
            this.pubChannel.publish(SupplyMessage.ok(this.demandId, 0, supply));
        }

    }

    private class LongDemandConsumer implements SubConsumer<DemandMessage<D>> {

        @Override
        public boolean consume(DemandMessage<D> demand) {
            try {
                return responseToDemand(demand);
            } catch (Exception e) {
                throw new RuntimeException("Failed to supply for demand: " + demand.getContent(), e);
            }
        }

        private boolean responseToDemand(DemandMessage<D> demand) {
            final Flux<S> flux = fluxService.apply(demand.getContent());
            if (flux != null) {
                final Identifier demandId = demand.getId();
                flux.subscribe(new LongResponseSubscriber(supplyRouter.apply(demandId), demandId));
                return true;
            }
            return false;
        }

    }

    private class LongResponseSubscriber extends ShortResponseSubscriber {

        private final AtomicInteger sequenceCounter = new AtomicInteger(0);

        LongResponseSubscriber(PubChannel<SupplyMessage<S>> pubChannel, Identifier demandId) {
            super(pubChannel, demandId);
        }

        @Override
        protected void hookOnNext(@NonNull S supply) {
            final int sequence = sequenceCounter.getAndIncrement();
            this.pubChannel.publish(SupplyMessage.ok(this.demandId, sequence, supply));
        }

        @Override
        protected void hookFinally(@NonNull SignalType type) {
            final int sequence = sequenceCounter.getAndIncrement();
            this.pubChannel.publish(SupplyMessage.terminated(this.demandId, sequence));
        }

    }

}
