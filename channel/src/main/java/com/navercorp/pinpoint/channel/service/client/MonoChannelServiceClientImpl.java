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
import com.navercorp.pinpoint.channel.Subscription;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class MonoChannelServiceClientImpl<D, S>
        extends AbstractChannelServiceClient<D, S> implements MonoChannelServiceClient<D, S> {

    private final MonoChannelServiceClientProtocol<D, S> protocol;

    MonoChannelServiceClientImpl(
            ChannelProviderRepository channelProviderRepository,
            MonoChannelServiceClientProtocol<D, S> protocol
    ) {
        super(channelProviderRepository, protocol);
        this.protocol = Objects.requireNonNull(protocol, "protocol");
    }

    @Override
    public Mono<S> request(D demand) {
        try {
            byte[] rawDemand = getProtocol().serializeDemand(demand);

            Sinks.One<S> sink = Sinks.one();
            Subscription subscription = subscribe(
                    demand,
                    e -> sink.emitValue(e, Sinks.EmitFailureHandler.FAIL_FAST),
                    e -> sink.emitError(e, Sinks.EmitFailureHandler.busyLooping(Duration.ofSeconds(1))),
                    () -> {}
            );

            getDemandPubChannel(demand).publish(rawDemand);

            return sink.asMono()
                    .doOnNext(e -> subscription.unsubscribe())
                    .doFinally(e -> subscription.unsubscribe())
                    .timeout(this.protocol.getRequestTimeout());
        } catch (Exception e) {
            throw new RuntimeException("Failed to request", e);
        }
    }

}
