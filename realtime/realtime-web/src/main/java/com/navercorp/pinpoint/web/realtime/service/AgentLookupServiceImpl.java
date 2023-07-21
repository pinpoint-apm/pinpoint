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
package com.navercorp.pinpoint.web.realtime.service;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilterChain;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class AgentLookupServiceImpl implements AgentLookupService {

    private final AgentInfoService agentInfoService;
    private final Duration recentness;

    AgentLookupServiceImpl(AgentInfoService agentInfoService, Duration recentness) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.recentness = Objects.requireNonNullElse(recentness, Duration.ZERO);
    }

    @Override
    public List<ClusterKey> getRecentAgents(String applicationName) {
        final long now = System.currentTimeMillis();
        return intoClusterKeyList(this.agentInfoService.getAgentsListByApplicationName(
                new AgentStatusFilterChain(AgentStatusFilter::filterRunning),
                applicationName,
                Range.between(now - recentness.toMillis(), now),
                SortByAgentInfo.Rules.AGENT_NAME_ASC
        ));
    }

    private static List<ClusterKey> intoClusterKeyList(AgentsMapByHost src) {
        final List<ClusterKey> result = new ArrayList<>(src.getAgentsListsList().size());
        for (final InstancesList<AgentStatusAndLink> instancesList: src.getAgentsListsList()) {
            for (final AgentStatusAndLink instance: instancesList.getInstancesList()) {
                result.add(intoClusterKey(instance.getAgentInfo()));
            }
        }
        return result;
    }

    private static ClusterKey intoClusterKey(AgentInfo src) {
        return new ClusterKey(src.getApplicationName(), src.getAgentId(), src.getStartTimestamp());
    }

}
