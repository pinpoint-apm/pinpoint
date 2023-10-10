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
import reactor.core.publisher.MonoSink;

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
        return Mono.<S>create(sink -> request0(demand, sink))
                .timeout(this.protocol.getRequestTimeout())
                .onErrorMap(e -> new RuntimeException("Failed to request", e));
    }

    private void request0(D demand, MonoSink<S> sink) {
        Subscription subscription = subscribe(demand, sink::success, sink::error, () -> {});
        getDemandPubChannel(demand).publish(getProtocol().serializeDemand(demand));
        sink.onDispose(subscription::unsubscribe);
    }

}
