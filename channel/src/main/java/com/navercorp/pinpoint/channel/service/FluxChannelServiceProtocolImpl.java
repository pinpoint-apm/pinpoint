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
package com.navercorp.pinpoint.channel.service;

import com.navercorp.pinpoint.channel.serde.Serde;
import com.navercorp.pinpoint.channel.service.client.ChannelState;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
class FluxChannelServiceProtocolImpl<D, S>
        extends AbstractChannelServiceProtocol<D, S> implements FluxChannelServiceProtocol<D, S> {

    private final Duration demandInterval;
    private final int bufferSize;
    private final Function<S, ChannelState> channelStateFn;

    FluxChannelServiceProtocolImpl(
            Serde<D> demandSerde,
            Function<D, URI> demandPubChannelURIProvider,
            URI demandSubChannelURI,
            Serde<S> supplySerde,
            Function<D, URI> supplyChannelURIProvider,
            Duration demandInterval,
            int bufferSize,
            Function<S, ChannelState> channelStateFn
    ) {
        super(demandSerde, demandPubChannelURIProvider, demandSubChannelURI, supplySerde, supplyChannelURIProvider);
        this.demandInterval = Objects.requireNonNull(demandInterval, "demandInterval");
        this.bufferSize = bufferSize;
        this.channelStateFn = Objects.requireNonNull(channelStateFn, "channelStateFn");
    }

    @Override
    public Duration getDemandInterval() {
        return this.demandInterval;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public ChannelState getChannelState(S supply) {
        return this.channelStateFn.apply(supply);
    }

}
