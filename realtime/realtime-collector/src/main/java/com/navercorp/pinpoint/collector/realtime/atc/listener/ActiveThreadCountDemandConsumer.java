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
package com.navercorp.pinpoint.collector.realtime.atc.listener;

import com.navercorp.pinpoint.collector.realtime.atc.service.ActiveThreadCountService;
import com.navercorp.pinpoint.collector.realtime.atc.service.SupplyPublishService;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.pubsub.SubConsumer;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadCountDemandConsumer implements SubConsumer<ATCDemand> {

    private static final Logger logger = LogManager.getLogger(ActiveThreadCountDemandConsumer.class);

    private final ActiveThreadCountService activeThreadCountService;
    private final SupplyPublishService supplyPublishService;

    public ActiveThreadCountDemandConsumer(
            ActiveThreadCountService activeThreadCountService,
            SupplyPublishService supplyPublishService
    ) {
        this.activeThreadCountService = Objects.requireNonNull(activeThreadCountService, "atcService");
        this.supplyPublishService = Objects.requireNonNull(supplyPublishService, "supplyPublishService");
    }

    @Override
    public void consume(ATCDemand demand, String postfix) {
        try {
            final ClusterKey clusterKey = getClusterKeyFrom(demand);
            this.activeThreadCountService.requestAsync(
                    clusterKey,
                    values -> this.supplyPublishService.publish(clusterKey, values)
            );
        } catch (Exception e) {
            logger.error("Failed to open atc stream", e);
        }
    }

    private static ClusterKey getClusterKeyFrom(ATCDemand demand) {
        final String applicationName = demand.getApplicationName();
        final String agentId = demand.getAgentId();
        final long startTimestamp = demand.getStartTimestamp();
        return new ClusterKey(applicationName, agentId, startTimestamp);
    }

}
