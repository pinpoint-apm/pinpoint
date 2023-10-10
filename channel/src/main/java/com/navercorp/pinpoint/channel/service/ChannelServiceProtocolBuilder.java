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
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
public class ChannelServiceProtocolBuilder<D, S> {

    private Serde<D> demandSerde;
    private Function<D, URI> demandPubChannelURIProvider;
    private URI demandSubChannelURI;
    private Serde<S> supplySerde;
    private Function<D, URI> supplyChannelURIProvider;
    private Duration requestTimeout;
    private Duration demandInterval = Duration.ZERO;
    private int bufferSize = 4;
    private Function<S, ChannelState> channelStateFn;

    ChannelServiceProtocolBuilder() {
    }

    public ChannelServiceProtocolBuilder<D, S> setDemandSerde(Serde<D> demandSerde) {
        this.demandSerde = demandSerde;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setDemandPubChannelURIProvider(Function<D, URI> demandPubChannelURIProvider) {
        this.demandPubChannelURIProvider = demandPubChannelURIProvider;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setDemandSubChannelURI(URI demandSubChannelURI) {
        this.demandSubChannelURI = demandSubChannelURI;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setSupplySerde(Serde<S> supplySerde) {
        this.supplySerde = supplySerde;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setSupplyChannelURIProvider(Function<D, URI> supplyChannelURIProvider) {
        this.supplyChannelURIProvider = supplyChannelURIProvider;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setDemandInterval(Duration demandInterval) {
        this.demandInterval = demandInterval;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public ChannelServiceProtocolBuilder<D, S> setChannelStateFn(Function<S, ChannelState> channelStateFn) {
        this.channelStateFn = channelStateFn;
        return this;
    }

    public FluxChannelServiceProtocol<D, S> buildFlux() {
        return new FluxChannelServiceProtocolImpl<>(
                this.demandSerde,
                this.demandPubChannelURIProvider,
                this.demandSubChannelURI,
                this.supplySerde,
                this.supplyChannelURIProvider,
                this.demandInterval,
                this.bufferSize,
                this.channelStateFn
        );
    }

    public MonoChannelServiceProtocol<D, S> buildMono() {
        return new MonoChannelServiceProtocolImpl<>(
                this.demandSerde,
                this.demandPubChannelURIProvider,
                this.demandSubChannelURI,
                this.supplySerde,
                this.supplyChannelURIProvider,
                this.requestTimeout
        );
    }

}
