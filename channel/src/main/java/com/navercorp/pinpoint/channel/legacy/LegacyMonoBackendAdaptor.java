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

import com.navercorp.pinpoint.channel.service.server.ChannelServiceMonoBackend;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class LegacyMonoBackendAdaptor<D, S> implements ChannelServiceMonoBackend<DemandMessage<D>, SupplyMessage<S>> {

    private final ChannelServiceMonoBackend<D, S> delegate;

    public LegacyMonoBackendAdaptor(ChannelServiceMonoBackend<D, S> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Nullable
    @Override
    public Mono<SupplyMessage<S>> demand(DemandMessage<D> demand) {
        Identifier id = demand.getId();
        Mono<S> supply = this.delegate.demand(demand.getContent());
        if (supply == null) {
            return null;
        }
        return supply.map(el -> wrapSupplyMessage(el, id));
    }

    private static <T> SupplyMessage<T> wrapSupplyMessage(T content, Identifier id) {
        SupplyMessage<T> msg = new SupplyMessage<>();
        msg.setContent(content);
        msg.setTerminated(false);
        msg.setDemandId(id);
        msg.setSequence(0);
        return msg;
    }

}
