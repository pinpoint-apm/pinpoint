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
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.cluster.ClusterKeyAndStatus;
import com.navercorp.pinpoint.web.service.AgentService;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author youngjin.kim2
 */
class AgentLookupServiceImpl implements AgentLookupService {

    private static final Logger logger = LogManager.getLogger(AgentLookupServiceImpl.class);

    private final AgentService agentService;

    AgentLookupServiceImpl(AgentService agentService) {
        this.agentService = Objects.requireNonNull(agentService, "agentService");
    }

    @Override
    public List<ClusterKey> getRecentAgents(String applicationName, long timeDiffMs) {
        final List<ClusterKeyAndStatus> keyAndStatuses = getRecentAgentInfoList0(applicationName, timeDiffMs);
        final List<ClusterKey> keys = new ArrayList<>(keyAndStatuses.size());
        for (final ClusterKeyAndStatus keyAndStatus: keyAndStatuses) {
            if (isActive(keyAndStatus.getStatus())) {
                keys.add(keyAndStatus.getClusterKey());
            }
        }
        return keys;
    }

    private List<ClusterKeyAndStatus> getRecentAgentInfoList0(String applicationName, long timeDiffMs) {
        try {
            return agentService.getRecentAgentInfoList(applicationName, timeDiffMs);
        } catch (Exception e) {
            logger.warn("Failed to get recent agent info list for {}", applicationName, e);
        }
        return Collections.emptyList();
    }

    private boolean isActive(AgentStatus agentStatus) {
        return agentStatus != null && agentStatus.getState() != AgentLifeCycleState.UNKNOWN;
    }

}
