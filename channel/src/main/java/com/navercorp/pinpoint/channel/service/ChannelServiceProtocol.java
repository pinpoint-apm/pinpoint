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

import com.navercorp.pinpoint.channel.service.client.ChannelServiceClientProtocol;
import com.navercorp.pinpoint.channel.service.server.ChannelServiceServerProtocol;

/**
 * @author youngjin.kim2
 *
 * This protocol should contain the information which must be shared between server and client
 * for communication through channel.
 * <br>
 * At the channel service, there always be a demand from client, and supplies from server. The demand are
 * carried through channel, and listened by server. The supplies are sent through channel, and received by client.
 * <br>
 * By the number of supplies, the channel service can be classified into two types: Mono and Flux.
 * Mono is a channel service which sends only one supply, and Flux is a channel service which sends multiple supplies.
 *
 * @see ChannelServiceServerProtocol
 * @see ChannelServiceClientProtocol
 */
public interface ChannelServiceProtocol<D, S> extends
        ChannelServiceServerProtocol<D, S>, ChannelServiceClientProtocol<D, S> {

    static <D, S> ChannelServiceProtocolBuilder<D, S> builder() {
        return new ChannelServiceProtocolBuilder<>();
    }

}
