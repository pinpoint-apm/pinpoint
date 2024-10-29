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
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.realtime.service.AgentLookupService;
import com.navercorp.pinpoint.web.service.ApplicationAgentListService;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.navercorp.pinpoint.web.service.ApplicationAgentListService.ACTUAL_AGENT_INFO_PREDICATE;

/**
 * @author youngjin.kim2
 */
class AgentLookupServiceImpl implements AgentLookupService {

    private final ApplicationAgentListService applicationAgentListService;
    private final Duration recentness;

    AgentLookupServiceImpl(ApplicationAgentListService applicationAgentListService, Duration recentness) {
        this.applicationAgentListService = Objects.requireNonNull(applicationAgentListService, "applicationAgentListService");
        this.recentness = Objects.requireNonNullElse(recentness, Duration.ZERO);
    }

    @Override
    public List<ClusterKey> getRecentAgents(String applicationName) {
        long now = System.currentTimeMillis();
        long from = now - recentness.toMillis();
        return intoClusterKeyList(this.applicationAgentListService.activeStatisticsAgentList(applicationName, null,
                Range.between(from, now),
                ACTUAL_AGENT_INFO_PREDICATE
        ));
    }

    private static List<ClusterKey> intoClusterKeyList(List<AgentAndStatus> agentAndStatusList) {
        return agentAndStatusList.stream()
                .map(AgentAndStatus::getAgentInfo)
                .map(AgentLookupServiceImpl::intoClusterKey)
                .collect(Collectors.toList());
    }

    private static ClusterKey intoClusterKey(AgentInfo src) {
        return new ClusterKey(src.getApplicationName(), src.getAgentId(), src.getStartTimestamp());
    }

}
