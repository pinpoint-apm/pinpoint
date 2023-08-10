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
package com.navercorp.pinpoint.web.realtime.echo;

import com.navercorp.pinpoint.channel.service.client.MonoChannelServiceClient;
import com.navercorp.pinpoint.realtime.dto.Echo;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ChannelEchoDao implements EchoDao {

    private final MonoChannelServiceClient<Echo, Echo> client;

    public ChannelEchoDao(MonoChannelServiceClient<Echo, Echo> client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public Mono<Echo> test(Echo echo) {
        return this.client.request(echo);
    }

}
