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

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author youngjin.kim2
 */
abstract class AbstractChannelServiceProtocol<D, S> implements ChannelServiceProtocol<D, S> {

    private final Serde<D> demandSerde;
    private final Function<D, URI> demandPubChannelURIProvider;
    private final URI demandSubChannelURI;
    private final Serde<S> supplySerde;
    private final Function<D, URI> supplyChannelURIProvider;

    protected AbstractChannelServiceProtocol(
            Serde<D> demandSerde,
            Function<D, URI> demandPubChannelURIProvider,
            URI demandSubChannelURI,
            Serde<S> supplySerde,
            Function<D, URI> supplyChannelURIProvider
    ) {
        this.demandSerde = Objects.requireNonNull(demandSerde, "demandSerde");
        this.demandPubChannelURIProvider =
                Objects.requireNonNull(demandPubChannelURIProvider, "demandPubChannelURIProvider");
        this.demandSubChannelURI = Objects.requireNonNull(demandSubChannelURI, "demandSubChannelURI");
        this.supplySerde = Objects.requireNonNull(supplySerde, "supplySerde");
        this.supplyChannelURIProvider = Objects.requireNonNull(supplyChannelURIProvider, "supplyChannelURIProvider");
    }

    @Override
    public byte[] serializeDemand(D demand) {
        try {
            return this.demandSerde.serializeToByteArray(demand);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize demand at " + this.demandSubChannelURI);
        }
    }
    @Override
    public D deserializeDemand(byte[] bytes) {
        try {
            return this.demandSerde.deserializeFromByteArray(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize demand at " + this.demandSubChannelURI);
        }
    }
    @Override
    public byte[] serializeSupply(S supply) {
        try {
            return this.supplySerde.serializeToByteArray(supply);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize supply at " + this.demandSubChannelURI);
        }
    }

    @Override
    public S deserializeSupply(byte[] bytes) {
        try {
            return this.supplySerde.deserializeFromByteArray(bytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize supply at " + this.demandSubChannelURI);
        }
    }

    @Override
    public URI getDemandPubChannelURI(D demand) {
        return this.demandPubChannelURIProvider.apply(demand);
    }

    @Override
    public URI getDemandSubChannelURI() {
        return demandSubChannelURI;
    }

    @Override
    public URI getSupplyChannelURI(D demand) {
        return supplyChannelURIProvider.apply(demand);
    }

}
