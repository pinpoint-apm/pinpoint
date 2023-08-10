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
package com.navercorp.pinpoint.channel.legacy;

import com.navercorp.pinpoint.channel.service.server.ChannelServiceFluxBackend;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author youngjin.kim2
 */
public class LegacyFluxBackendAdaptor<D, S> implements ChannelServiceFluxBackend<DemandMessage<D>, SupplyMessage<S>> {

    private final ChannelServiceFluxBackend<D, S> delegate;

    public LegacyFluxBackendAdaptor(ChannelServiceFluxBackend<D, S> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Nullable
    @Override
    public Flux<SupplyMessage<S>> demand(DemandMessage<D> demand) {
        Identifier id = demand.getId();
        Flux<S> supply = this.delegate.demand(demand.getContent());
        if (supply == null) {
            return null;
        }
        AtomicInteger seqGen = new AtomicInteger(0);
        return supply.map(el -> wrapSupplyMessage(el, id, seqGen.getAndIncrement(), false))
                .concatWith(Mono.defer(() -> Mono.just(wrapSupplyMessage(null, id, seqGen.getAndIncrement(), true))));
    }

    private static <T> SupplyMessage<T> wrapSupplyMessage(T content, Identifier id, int seq, boolean terminated) {
        SupplyMessage<T> msg = new SupplyMessage<>();
        msg.setContent(content);
        msg.setTerminated(terminated);
        msg.setDemandId(id);
        msg.setSequence(seq);
        return msg;
    }

}
