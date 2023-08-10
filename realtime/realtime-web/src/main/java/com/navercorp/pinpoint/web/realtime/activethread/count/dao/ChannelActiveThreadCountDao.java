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
package com.navercorp.pinpoint.web.realtime.activethread.count.dao;

import com.navercorp.pinpoint.channel.service.client.FluxChannelServiceClient;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.redis.value.Incrementer;
import reactor.core.publisher.Flux;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ChannelActiveThreadCountDao implements ActiveThreadCountDao {

    private final Incrementer incrementer;
    private final FluxChannelServiceClient<ATCDemand, ATCSupply> client;

    ChannelActiveThreadCountDao(
            Incrementer incrementer,
            FluxChannelServiceClient<ATCDemand, ATCSupply> client
    ) {
        this.incrementer = Objects.requireNonNull(incrementer, "incrementer");
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public Flux<ATCSupply> getSupplies(ClusterKey agentKey) {
        return this.client.request(makeDemand(this.incrementer.get(), agentKey));
    }

    private static ATCDemand makeDemand(long id, ClusterKey key) {
        ATCDemand demand = new ATCDemand();
        demand.setId(id);
        demand.setApplicationName(key.getApplicationName());
        demand.setAgentId(key.getAgentId());
        demand.setStartTimestamp(key.getStartTimestamp());
        return demand;
    }

}
