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

import com.navercorp.pinpoint.channel.ChannelProviderRepository;
import org.springframework.context.SmartLifecycle;

/**
 * Serves the demands of a channel, started and stopped by the context as a {@link SmartLifecycle}.
 * <p>
 * {@link #start()} subscribes the demand channel after all singletons are ready, and {@link #stop()}
 * unsubscribes it before the other beans are destroyed.
 *
 * @author youngjin.kim2
 */
public interface ChannelServiceServer extends SmartLifecycle {

    static <D, S> ChannelServiceServer buildMono(
            ChannelProviderRepository channelProviderRepository,
            ChannelServiceServerProtocol<D, S> protocol,
            ChannelServiceMonoBackend<D, S> backend
    ) {
        return new ChannelServiceServerImpl<>(channelProviderRepository, protocol, backend);
    }

    static <D, S> ChannelServiceServer buildFlux(
            ChannelProviderRepository channelProviderRepository,
            ChannelServiceServerProtocol<D, S> protocol,
            ChannelServiceFluxBackend<D, S> backend
    ) {
        return new ChannelServiceServerImpl<>(channelProviderRepository, protocol, backend);
    }

}
