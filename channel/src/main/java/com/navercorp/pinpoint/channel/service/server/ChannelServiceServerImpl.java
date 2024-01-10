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
package com.navercorp.pinpoint.channel.service.server;

import com.google.common.base.Suppliers;
import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.channel.SubChannel;
import com.navercorp.pinpoint.channel.SubConsumer;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.lang.NonNull;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author youngjin.kim2
 */
class ChannelServiceServerImpl<D, S> implements ChannelServiceServer {

    private static final Logger logger = LogManager.getLogger(ChannelServiceServerImpl.class);

    private final ChannelProviderRepository channelProviderRepository;
    private final ChannelServiceServerProtocol<D, S> protocol;
    private final ChannelServiceMonoBackend<D, S> monoBackend;
    private final ChannelServiceFluxBackend<D, S> fluxBackend;

    ChannelServiceServerImpl(
            ChannelProviderRepository channelProviderRepository,
            ChannelServiceServerProtocol<D, S> protocol,
            ChannelServiceMonoBackend<D, S> monoBackend
    ) {
        this.channelProviderRepository = Objects.requireNonNull(channelProviderRepository, "channelProviderRepository");
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.monoBackend = Objects.requireNonNull(monoBackend, "monoBackend");
        this.fluxBackend = null;
    }

    ChannelServiceServerImpl(
            ChannelProviderRepository channelProviderRepository,
            ChannelServiceServerProtocol<D, S> protocol,
            ChannelServiceFluxBackend<D, S> fluxBackend
    ) {
        this.channelProviderRepository = Objects.requireNonNull(channelProviderRepository, "channelProviderRepository");
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.fluxBackend = Objects.requireNonNull(fluxBackend, "fluxBackend");
        this.monoBackend = null;
    }

    private SubChannel getDemandSubChannel() {
        return this.channelProviderRepository.getSubChannel(this.protocol.getDemandSubChannelURI());
    }

    private PubChannel getSupplyPubChannel(D demand) {
        return this.channelProviderRepository.getPubChannel(this.protocol.getSupplyChannelURI(demand));
    }

    private ChannelServiceServerProtocol<D, S> getProtocol() {
        return this.protocol;
    }

    @Override
    public void listen() {
        this.getDemandSubChannel().subscribe(this.getSubConsumer());
    }

    private SubConsumer getSubConsumer() {
        if (this.monoBackend != null) {
            return new MonoDemandHandler(this.monoBackend);
        } else {
            return new FluxDemandHandler(this.fluxBackend);
        }
    }

    private class MonoDemandHandler implements SubConsumer {

        private final ChannelServiceMonoBackend<D, S> backend;

        public MonoDemandHandler(ChannelServiceMonoBackend<D, S> backend) {
            this.backend = backend;
        }

        @Override
        public boolean consume(byte[] rawDemand) {
            try {
                return responseToDemand(getProtocol().deserializeDemand(rawDemand));
            } catch (Exception e) {
                throw new RuntimeException("Failed to supply for demand: " + BytesUtils.toString(rawDemand), e);
            }
        }

        private boolean responseToDemand(D demand) {
            Mono<S> response = this.backend.demand(demand);
            if (response != null) {
                response
                        .doOnError(e -> logger.debug("Ignored short pubsub demand: {}", demand))
                        .onErrorComplete(IgnoreDemandException.class)
                        .subscribe(new PubChannelProxy(demand));
                return true;
            } else {
                logger.debug("Ignored short pubsub demand: {}", demand);
            }
            return false;
        }

    }

    private class FluxDemandHandler implements SubConsumer {

        private final ChannelServiceFluxBackend<D, S> backend;

        public FluxDemandHandler(ChannelServiceFluxBackend<D, S> backend) {
            this.backend = backend;
        }

        @Override
        public boolean consume(byte[] rawDemand) {
            try {
                return responseToDemand(getProtocol().deserializeDemand(rawDemand));
            } catch (Exception e) {
                throw new RuntimeException("Failed to supply for demand: " + BytesUtils.toString(rawDemand), e);
            }
        }

        private boolean responseToDemand(D demand) {
            Flux<S> response = this.backend.demand(demand);
            if (response != null) {
                response
                        .doOnError(e -> logger.debug("Ignored long pubsub demand: {}", demand))
                        .onErrorComplete(IgnoreDemandException.class)
                        .subscribe(new PubChannelProxy(demand));
                return true;
            } else {
                logger.debug("Ignored long pubsub demand: {}", demand);
            }
            return false;
        }

    }

    private class PubChannelProxy extends BaseSubscriber<S> {

        private final D demand;
        private final Supplier<PubChannel> channelSupplier = Suppliers.memoize(this::buildPubChannel);

        PubChannelProxy(D demand) {
            this.demand = demand;
        }

        @Override
        public void hookOnNext(@NonNull S supply) {
            try {
                byte[] rawResponse = getProtocol().serializeSupply(supply);
                this.channelSupplier.get().publish(rawResponse);
            } catch (Exception e) {
                logger.error("Failed to send", e);
            }
        }

        private PubChannel buildPubChannel() {
            logger.info("Responding pubsub demand ({})", demand);
            return getSupplyPubChannel(this.demand);
        }

    }

}
