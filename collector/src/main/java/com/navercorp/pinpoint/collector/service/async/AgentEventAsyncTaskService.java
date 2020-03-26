/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.service.async;

import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 * @author jaehong.kim - Remove AgentEventMessageSerializer
 */
@Service
public class AgentEventAsyncTaskService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentEventService agentEventService;

    public AgentEventAsyncTaskService(AgentEventService agentEventService) {
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
    }

    @Async("agentEventWorker")
    public void handleEvent(final AgentProperty agentProperty, long eventTimestamp, AgentEventType eventType) {
        Objects.requireNonNull(agentProperty, "agentProperty");
        Objects.requireNonNull(eventType, "eventType");

        final String agentId = agentProperty.getAgentId();
        final long startTimestamp = agentProperty.getStartTime();
        final AgentEventBo agentEventBo = newAgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
        this.agentEventService.insert(agentEventBo);
    }

    private AgentEventBo newAgentEventBo(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType) {
        final AgentEventBo agentEventBo = new AgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
        agentEventBo.setEventBody(new byte[0]);
        return agentEventBo;
    }
}