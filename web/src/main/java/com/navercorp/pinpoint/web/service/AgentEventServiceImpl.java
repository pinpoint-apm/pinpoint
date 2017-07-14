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

import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializer;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.DurationalAgentEvent;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

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
    public List<AgentEvent> getAgentEvents(String agentId, Range range, int... excludeEventTypeCodes) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        Set<AgentEventType> excludeEventTypes = EnumSet.noneOf(AgentEventType.class);
        for (int excludeEventTypeCode : excludeEventTypeCodes) {
            AgentEventType excludeEventType = AgentEventType.getTypeByCode(excludeEventTypeCode);
            if (excludeEventType != null) {
                excludeEventTypes.add(excludeEventType);
            }
        }
        List<AgentEventBo> agentEventBos = this.agentEventDao.getAgentEvents(agentId, range, excludeEventTypes);
        List<AgentEvent> agentEvents = createAgentEvents(agentEventBos);
        Collections.sort(agentEvents, AgentEvent.EVENT_TIMESTAMP_ASC_COMPARATOR);
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

    private List<AgentEvent> createAgentEvents(List<AgentEventBo> agentEventBos) {
        if (CollectionUtils.isEmpty(agentEventBos)) {
            return Collections.emptyList();
        }
        List<AgentEvent> agentEvents = new ArrayList<>(agentEventBos.size());
        PriorityQueue<DurationalAgentEvent> durationalAgentEvents = new PriorityQueue<>(agentEventBos.size(), AgentEvent.EVENT_TIMESTAMP_ASC_COMPARATOR);
        for (AgentEventBo agentEventBo : agentEventBos) {
            if (agentEventBo.getEventType().isCategorizedAs(AgentEventTypeCategory.DURATIONAL)) {
                durationalAgentEvents.add(createDurationalAgentEvent(agentEventBo, false));
            } else {
                boolean hasMessage = !ArrayUtils.isEmpty(agentEventBo.getEventBody());
                agentEvents.add(createAgentEvent(agentEventBo, hasMessage));
            }
        }
        long durationStartTimestamp = DurationalAgentEvent.UNKNOWN_TIMESTAMP;
        while (!durationalAgentEvents.isEmpty()) {
            DurationalAgentEvent currentEvent = durationalAgentEvents.remove();
            if (durationStartTimestamp == DurationalAgentEvent.UNKNOWN_TIMESTAMP) {
                durationStartTimestamp = currentEvent.getEventTimestamp();
            }
            currentEvent.setDurationStartTimestamp(durationStartTimestamp);
            DurationalAgentEvent nextEvent = durationalAgentEvents.peek();
            if (nextEvent != null) {
                long nextEventTimestamp = nextEvent.getEventTimestamp();
                currentEvent.setDurationEndTimestamp(nextEventTimestamp);
                durationStartTimestamp = nextEventTimestamp;
            }
            agentEvents.add(currentEvent);
        }
        return agentEvents;
    }

    private AgentEvent createAgentEvent(AgentEventBo agentEventBo, boolean includeEventMessage) {
        AgentEvent agentEvent = new AgentEvent(agentEventBo);
        if (includeEventMessage) {
            agentEvent.setEventMessage(deserializeEventMessage(agentEventBo));
        }
        return agentEvent;
    }

    @Deprecated
    private DurationalAgentEvent createDurationalAgentEvent(AgentEventBo agentEventBo, boolean includeEventMessage) {
        DurationalAgentEvent durationalAgentEvent = new DurationalAgentEvent(agentEventBo);
        if (includeEventMessage) {
            durationalAgentEvent.setEventMessage(deserializeEventMessage(agentEventBo));
        }
        return durationalAgentEvent;
    }

    private Object deserializeEventMessage(AgentEventBo agentEventBo) {
        try {
            return this.agentEventMessageDeserializer.deserialize(agentEventBo.getEventType(), agentEventBo.getEventBody());
        } catch (UnsupportedEncodingException e) {
            logger.warn("error deserializing event message", e);
            return null;
        }
    }

}
