/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.common.bo.AgentEventBo;
import com.navercorp.pinpoint.common.util.AgentEventMessageDeserializer;
import com.navercorp.pinpoint.common.util.AgentEventType;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * @author HyunGil Jeong
 */
@Service
public class AgentEventServiceImpl implements AgentEventService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentEventDao agentEventDao;

    @Autowired
    private AgentEventMessageDeserializer agentEventMessageDeserializer;

    @Override
    public List<AgentEvent> getAgentEvents(String agentId, Range range) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        final boolean includeEventMessage = false;
        List<AgentEventBo> agentEventBos = this.agentEventDao.getAgentEvents(agentId, range);
        List<AgentEvent> agentEvents = new ArrayList<>(agentEventBos.size());
        for (AgentEventBo agentEventBo : agentEventBos) {
            if (agentEventBo != null) {
                agentEvents.add(createAgentEvent(agentEventBo, includeEventMessage));
            }
        }
        Collections.sort(agentEvents, AgentEvent.EVENT_TIMESTAMP_DESC_COMPARATOR);
        return agentEvents;
    }

    @Override
    public AgentEvent getAgentEvent(String agentId, long eventTimestamp, int eventTypeCode) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (eventTimestamp < 0) {
            throw new IllegalArgumentException("eventTimeTimestamp must not be less than 0");
        }
        final AgentEventType eventType = AgentEventType.getTypeByCode(eventTypeCode);
        if (eventType == null) {
            throw new IllegalArgumentException("invalid eventTypeCode [" + eventTypeCode + "]");
        }
        final boolean includeEventMessage = true;
        AgentEventBo agentEventBo = this.agentEventDao.getAgentEvent(agentId, eventTimestamp, eventType);
        if (agentEventBo != null) {
            return createAgentEvent(agentEventBo, includeEventMessage);
        }
        return null;
    }

    private AgentEvent createAgentEvent(AgentEventBo agentEventBo, boolean includeEventMessage) {
        final String agentId = agentEventBo.getAgentId();
        final long eventTimestamp = agentEventBo.getEventTimestamp();
        final AgentEventType eventType = agentEventBo.getEventType();
        AgentEvent agentEvent = new AgentEvent(agentId, eventTimestamp, eventType);
        agentEvent.setStartTimestamp(agentEventBo.getStartTimestamp());
        if (includeEventMessage) {
            try {
                agentEvent.setEventMessage(this.agentEventMessageDeserializer.deserialize(eventType,
                        agentEventBo.getEventBody()));
            } catch (UnsupportedEncodingException e) {
                logger.warn("error deserializing event message", e);
            }
        }
        return agentEvent;
    }

}
