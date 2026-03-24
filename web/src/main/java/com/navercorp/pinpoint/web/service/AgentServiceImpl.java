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

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 * @author Taejin Koo
 */
@Service
public class AgentServiceImpl implements AgentService {

    private final AgentInfoService agentInfoService;

    public AgentServiceImpl(AgentInfoService agentInfoService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId) {
        long currentTime = System.currentTimeMillis();

        AgentInfo agentInfo = agentInfoService.findAgentInfo(agentId, currentTime);
        if (agentInfo == null) {
            return null;
        }
        if (!agentInfo.getApplicationName().equals(applicationName)) {
            return null;
        }
        if (!agentInfo.getAgentId().equals(agentId)) {
            return null;
        }
        return buildClusterKey(agentInfo);
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp) {
        return getClusterKey(applicationName, agentId, startTimeStamp, false);
    }

    @Override
    public ClusterKey getClusterKey(String applicationName, String agentId, long startTimeStamp, boolean checkDB) {
        if (checkDB) {
            AgentInfo agentInfo = agentInfoService.getAgentInfo(agentId, startTimeStamp);
            if (agentInfo == null) {
                return null;
            }
            if (!agentInfo.getApplicationName().equals(applicationName)) {
                return null;
            }
            if (!agentInfo.getAgentId().equals(agentId)) {
                return null;
            }
            if (agentInfo.getStartTimestamp() != startTimeStamp) {
                return null;
            }
            return buildClusterKey(agentInfo);
        } else {
            return new ClusterKey(ServiceUid.DEFAULT_SERVICE_UID_NAME, applicationName, agentId, startTimeStamp);
        }
    }

    private static ClusterKey buildClusterKey(AgentInfo info) {
        Objects.requireNonNull(info, "info");
        return new ClusterKey(ServiceUid.DEFAULT_SERVICE_UID_NAME, info.getApplicationName(), info.getAgentId(), info.getStartTimestamp());
    }

}
