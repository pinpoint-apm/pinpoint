/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.service;


import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AgentStatusServiceImpl implements AgentStatusService {

    private final AgentStatDao<JvmGcBo> jvmGcDao;
    private final AgentEventService agentEventService;

    public AgentStatusServiceImpl(AgentStatDao<JvmGcBo> jvmGcDao, AgentEventService agentEventService) {
        this.jvmGcDao = Objects.requireNonNull(jvmGcDao, "jvmGcDao");
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
    }

    @Override
    public boolean isActiveAgent(String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        boolean dataExists = this.jvmGcDao.agentStatExists(agentId, range);
        if (dataExists) {
            return true;
        }

        List<AgentEvent> agentEvents = this.agentEventService.getAgentEvents(agentId, range);
        return agentEvents.stream().anyMatch(e -> e.getEventTypeCode() == AgentEventType.AGENT_PING.getCode());
    }

}

