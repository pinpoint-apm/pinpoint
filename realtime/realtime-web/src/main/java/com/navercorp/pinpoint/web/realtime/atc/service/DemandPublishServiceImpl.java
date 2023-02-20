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
package com.navercorp.pinpoint.web.realtime.atc.service;

import com.google.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.pubsub.PubChannel;
import com.navercorp.pinpoint.realtime.atc.dto.ATCDemand;
import com.navercorp.pinpoint.web.realtime.atc.dao.ATCValueDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.CountingMetricDao;
import com.navercorp.pinpoint.web.realtime.atc.dao.memory.ATCSessionRepository;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author youngjin.kim2
 */
public class DemandPublishServiceImpl implements DemandPublishService {

    private static final Logger logger = LogManager.getLogger(DemandPublishServiceImpl.class);

    private static final long DEFAULT_AGENT_LOOKUP_TIME = TimeUnit.HOURS.toMillis(1);

    private final PubChannel<ATCDemand> pubChannel;
    private final AgentLookupService agentLookupService;
    private final ATCValueDao valueDao;
    private final CountingMetricDao countingMetricDao;
    private final ATCSessionRepository sessionRepository;

    public DemandPublishServiceImpl(
            PubChannel<ATCDemand> pubChannel,
            AgentLookupService agentLookupService,
            ATCValueDao valueDao,
            CountingMetricDao countingMetricDao,
            ATCSessionRepository sessionRepository
    ) {
        this.pubChannel = Objects.requireNonNull(pubChannel, "pubChannel");
        this.agentLookupService = Objects.requireNonNull(agentLookupService, "agentLookupService");
        this.valueDao = Objects.requireNonNull(valueDao, "valueDao");
        this.countingMetricDao = Objects.requireNonNull(countingMetricDao, "countingMetricDao");
        this.sessionRepository = Objects.requireNonNull(sessionRepository, "sessionRepository");
    }

    @Override
    public void demand() {
        final Set<String> applicationNames = this.sessionRepository.getAllDemandedApplicationNames();
        for (final String applicationName: applicationNames) {
            demand(applicationName);
        }
    }

    @Override
    public void demand(String applicationName) {
        try {
            final List<ClusterKey> agents = this.agentLookupService.getRecentAgents(
                    applicationName, DEFAULT_AGENT_LOOKUP_TIME);
            valueDao.saveActiveAgents(applicationName, agents);
            for (final ClusterKey agent: agents) {
                demand(agent);
            }
        } catch (Exception e) {
            logger.error("Failed to demand application: {}", applicationName);
        }
    }

    @VisibleForTesting
    void demand(ClusterKey agent) {
        final ATCDemand demand = makeDemand(agent);
        try {
            pubChannel.publish(demand, null);
            countingMetricDao.incrementCountATCDemand();
        } catch (Exception e) {
            logger.warn("Failed to publish", e);
        }
    }

    private ATCDemand makeDemand(ClusterKey clusterKey) {
        final ATCDemand demand = new ATCDemand();
        demand.setApplicationName(clusterKey.getApplicationName());
        demand.setAgentId(clusterKey.getAgentId());
        demand.setStartTimestamp(clusterKey.getStartTimestamp());
        return demand;
    }

}
