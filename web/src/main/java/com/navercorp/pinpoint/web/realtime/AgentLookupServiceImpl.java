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
package com.navercorp.pinpoint.web.realtime;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.realtime.activethread.count.dto.ClusterKeyAndMetadata;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.service.ApplicationAgentListService;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.service.ServiceNotFoundException;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.navercorp.pinpoint.web.service.ApplicationAgentListService.ACTUAL_AGENT_INFO_PREDICATE;

/**
 * @author youngjin.kim2
 */
class AgentLookupServiceImpl implements AgentLookupService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ApplicationAgentListService applicationAgentListService;
    private final ServiceModelResolver serviceModelResolver;
    private final Duration recentness;

    AgentLookupServiceImpl(ApplicationAgentListService applicationAgentListService,
                           ServiceModelResolver serviceModelResolver,
                           Duration recentness) {
        this.applicationAgentListService = Objects.requireNonNull(applicationAgentListService, "applicationAgentListService");
        this.serviceModelResolver = Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
        this.recentness = Objects.requireNonNullElse(recentness, Duration.ZERO);
    }

    @Override
    public List<ClusterKeyAndMetadata> getRecentAgents(String serviceName, String applicationName) {
        final Service service;
        try {
            service = serviceModelResolver.getService(serviceName);
        } catch (ServiceNotFoundException e) {
            logger.warn("Service not found. serviceName: {}, applicationName: {}", serviceName, applicationName);
            return List.of();
        }

        long now = System.currentTimeMillis();
        long from = now - recentness.toMillis();
        Range between = Range.between(from, now);
        TimeWindow timeWindow = new TimeWindow(between);

        return intoClusterKeyAndMetadataList(service.getServiceName(),
                this.applicationAgentListService.activeStatisticsAgentList(service, applicationName, null,
                        timeWindow,
                        ACTUAL_AGENT_INFO_PREDICATE
                ));
    }

    private static List<ClusterKeyAndMetadata> intoClusterKeyAndMetadataList(String serviceName, List<AgentAndStatus> agentAndStatusList) {
        return agentAndStatusList.stream()
                .map(AgentAndStatus::getAgentInfo)
                .map(agentInfo -> intoClusterKeyAndMetadata(serviceName, agentInfo))
                .collect(Collectors.toList());
    }

    private static ClusterKeyAndMetadata intoClusterKeyAndMetadata(String serviceName, AgentInfo src) {
        return new ClusterKeyAndMetadata(
                new ClusterKey(serviceName, src.getApplicationName(), src.getAgentId(), src.getStartTimestamp()),
                src.getAgentName()
        );
    }

}
