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
package com.navercorp.pinpoint.web.realtime.activethread.dump;

import com.navercorp.pinpoint.channel.service.client.MonoChannelServiceClient;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ChannelActiveThreadDumpDao implements ActiveThreadDumpDao {

    private final MonoChannelServiceClient<ATDDemand, ATDSupply> client;

    public ChannelActiveThreadDumpDao(MonoChannelServiceClient<ATDDemand, ATDSupply> client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public Mono<ATDSupply> dump(ATDDemand demand) {
        return this.client.request(demand);
    }

}
