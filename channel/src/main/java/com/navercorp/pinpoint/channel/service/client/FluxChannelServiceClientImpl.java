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
package com.navercorp.pinpoint.channel.service.client;

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import com.navercorp.pinpoint.channel.PubChannel;
import com.navercorp.pinpoint.channel.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class FluxChannelServiceClientImpl<D, S>
        extends AbstractChannelServiceClient<D, S> implements FluxChannelServiceClient<D, S> {

    private static final Disposable EMPTY_DISPOSABLE = new EmptyDisposable();

    private final FluxChannelServiceClientProtocol<D, S> protocol;
    private final Scheduler demandScheduler;

    FluxChannelServiceClientImpl(
            ChannelProviderRepository channelProviderRepository,
            FluxChannelServiceClientProtocol<D, S> protocol,
            Scheduler demandScheduler
    ) {
        super(channelProviderRepository, protocol);
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.demandScheduler = Objects.requireNonNull(demandScheduler, "demandScheduler");
    }

    @Override
    public Flux<S> request(D demand) {
        try {
            byte[] rawDemand = getProtocol().serializeDemand(demand);

            Sinks.Many<S> sink = Sinks.many().multicast().onBackpressureBuffer(this.protocol.getBufferSize());
            Subscription supplySubscription = subscribe(
                    demand,
                    e -> sink.emitNext(e, this.protocol.getFailureHandlerEmitNext()),
                    e -> sink.emitError(e, this.protocol.getFailureHandlerEmitError()),
                    () -> sink.emitComplete(this.protocol.getFailureHandlerEmitComplete())
            );

            PubChannel demandPubChannel = getDemandPubChannel(demand);
            Disposable demandSubscription = scheduleDemandPeriodically(rawDemand, demandPubChannel);

            return sink.asFlux()
                    .doFinally(e -> {
                        supplySubscription.unsubscribe();
                        demandSubscription.dispose();
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to request", e);
        }
    }

    private Disposable scheduleDemandPeriodically(byte[] rawDemand, PubChannel demandPubChannel) {
        demandPubChannel.publish(rawDemand);
        if (this.protocol.getDemandInterval() != Duration.ZERO) {
            return Flux.interval(this.protocol.getDemandInterval())
                    .subscribeOn(this.demandScheduler)
                    .subscribe(t -> demandPubChannel.publish(rawDemand));
        } else {
            return EMPTY_DISPOSABLE;
        }
    }

    private static class EmptyDisposable implements Disposable {
        @Override
        public void dispose() {}
    }

}
