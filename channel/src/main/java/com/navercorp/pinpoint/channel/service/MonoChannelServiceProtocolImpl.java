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
class MonoChannelServiceProtocolImpl<D, S>
        extends AbstractChannelServiceProtocol<D, S> implements MonoChannelServiceProtocol<D, S> {

    private final Duration requestTimeout;

    MonoChannelServiceProtocolImpl(
            Serde<D> demandSerde,
            Function<D, URI> demandPubChannelURIProvider,
            URI demandSubChannelURI,
            Serde<S> supplySerde,
            Function<D, URI> supplyChannelURIProvider,
            Duration requestTimeout
    ) {
        super(demandSerde, demandPubChannelURIProvider, demandSubChannelURI, supplySerde, supplyChannelURIProvider);
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
    }

    @Override
    public Duration getRequestTimeout() {
        return this.requestTimeout;
    }

    @Override
    public ChannelState getChannelState(S supply) {
        return ChannelState.SENT_LAST_MESSAGE;
    }

}
