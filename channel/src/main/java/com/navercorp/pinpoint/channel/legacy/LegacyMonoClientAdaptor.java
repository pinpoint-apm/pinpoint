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

import com.navercorp.pinpoint.channel.service.client.MonoChannelServiceClient;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class LegacyMonoClientAdaptor<D, S> implements MonoChannelServiceClient<D, S> {

    private final MonoChannelServiceClient<DemandMessage<D>, SupplyMessage<S>> delegate;
    private final Function<D, Long> idFn;

    public LegacyMonoClientAdaptor(
            MonoChannelServiceClient<DemandMessage<D>, SupplyMessage<S>> delegate,
            Function<D, Long> idFn
    ) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.idFn = Objects.requireNonNull(idFn, "idFn");
    }

    @Override
    public Mono<S> request(D demand) {
        return this.delegate.request(wrapDemand(demand)).mapNotNull(SupplyMessage::getContent);
    }

    private DemandMessage<D> wrapDemand(D demand) {
        Identifier id = new Identifier();
        id.setValue(this.idFn.apply(demand));

        DemandMessage<D> msg = new DemandMessage<>();
        msg.setId(id);
        msg.setContent(demand);
        return msg;
    }

}
