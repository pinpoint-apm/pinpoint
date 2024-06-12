/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author HyunGil Jeong
 * @author Taejin Koo
 */
@Service
public class AgentServiceImpl implements AgentService {

    private static final short DEFAULT_SERVICE_TYPE_CODE = PinpointConstants.DEFAULT_SERVICE_TYPE_CODE;

    private final AgentInfoService agentInfoService;

    public AgentServiceImpl(AgentInfoService agentInfoService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId) {
        long currentTime = System.currentTimeMillis();

        Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationNameWithoutStatus(applicationName, DEFAULT_SERVICE_TYPE_CODE, currentTime);
        for (AgentInfo agentInfo : agentInfos) {
            if (agentInfo == null) {
                continue;
            }
            if (!agentInfo.getApplicationName().equals(applicationName)) {
                continue;
            }
            if (!agentInfo.getAgentId().value().equals(agentId)) {
                continue;
            }

            return buildClusterKey(agentInfo);
        }

        return null;
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp) {
        return getClusterKey(applicationName, agentId, startTimeStamp, false);
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp, boolean checkDB) {
        if (checkDB) {
            long currentTime = System.currentTimeMillis();

            Set<AgentInfo> agentInfos = agentInfoService.getAgentsByApplicationNameWithoutStatus(applicationName, DEFAULT_SERVICE_TYPE_CODE, currentTime);
            for (AgentInfo agentInfo : agentInfos) {
                if (agentInfo == null) {
                    continue;
                }
                if (!agentInfo.getApplicationName().equals(applicationName)) {
                    continue;
                }
                if (!agentInfo.getAgentId().value().equals(agentId)) {
                    continue;
                }
                if (agentInfo.getStartTimestamp() != startTimeStamp) {
                    continue;
                }

                return buildClusterKey(agentInfo);
            }
            return null;
        } else {
            return new ClusterKey(applicationName, agentId, startTimeStamp);
        }
    }

    private static ClusterKey buildClusterKey(AgentInfo info) {
        Objects.requireNonNull(info, "info");
        return new ClusterKey(info.getApplicationName(), info.getAgentId(), info.getStartTimestamp());
    }

}
