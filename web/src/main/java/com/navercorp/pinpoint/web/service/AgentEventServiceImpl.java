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
import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializerV1;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.web.dao.AgentEventDao;
import com.navercorp.pinpoint.web.service.component.AgentEventQuery;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.DurationalAgentEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * @author HyunGil Jeong
 * @author jaehong.kim - Add agentEventMessageDeserializerV1
 */
@Service
public class AgentEventServiceImpl implements AgentEventService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentEventDao agentEventDao;

    private final AgentEventMessageDeserializer agentEventMessageDeserializer;

    private final AgentEventMessageDeserializerV1 agentEventMessageDeserializerV1;

    public AgentEventServiceImpl(AgentEventDao agentEventDao,
                                 AgentEventMessageDeserializer agentEventMessageDeserializer,
                                 AgentEventMessageDeserializerV1 agentEventMessageDeserializerV1) {
        this.agentEventDao = Objects.requireNonNull(agentEventDao, "agentEventDao");
        this.agentEventMessageDeserializer = Objects.requireNonNull(agentEventMessageDeserializer, "agentEventMessageDeserializer");
        this.agentEventMessageDeserializerV1 = Objects.requireNonNull(agentEventMessageDeserializerV1, "agentEventMessageDeserializerV1");
    }

    @Override
    public List<AgentEvent> getAgentEvents(String agentId, Range range) {
        return getAgentEvents(agentId, range, AgentEventQuery.all());
    }

    @Override
    public List<AgentEvent> getAgentEvents(String agentId, Range range, AgentEventQuery query) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(query, "query");

        List<AgentEventBo> agentEventBos = this.agentEventDao.getAgentEvents(agentId, range, query);

        List<AgentEvent> agentEvents = createAgentEvents(agentEventBos);
        agentEvents.sort(AgentEvent.EVENT_TIMESTAMP_ASC_COMPARATOR);
        return agentEvents;
    }

    @Override
    public AgentEvent getAgentEvent(String agentId, long eventTimestamp, AgentEventType eventType) {
        Objects.requireNonNull(agentId, "agentId");
        if (eventTimestamp < 0) {
            throw new IllegalArgumentException("eventTimeTimestamp must not be less than 0");
        }
        Objects.requireNonNull(eventType, "eventType");

        AgentEventBo agentEventBo = this.agentEventDao.getAgentEvent(agentId, eventTimestamp, eventType);
        if (agentEventBo != null) {
            return createAgentEvent(agentEventBo, true);
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
                boolean hasMessage = ArrayUtils.hasLength(agentEventBo.getEventBody());
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
        if (includeEventMessage) {
            return AgentEvent.withEventMessage(agentEventBo, deserializeEventMessage(agentEventBo));
        }
        return AgentEvent.from(agentEventBo);
    }

    @Deprecated
    private DurationalAgentEvent createDurationalAgentEvent(AgentEventBo agentEventBo, boolean includeEventMessage) {
        if (includeEventMessage) {
            return new DurationalAgentEvent(agentEventBo, deserializeEventMessage(agentEventBo));
        }
        return new DurationalAgentEvent(agentEventBo);
    }

    private Object deserializeEventMessage(AgentEventBo agentEventBo) {
        try {
            if (agentEventBo.getVersion() == 0) {
                return this.agentEventMessageDeserializer.deserialize(agentEventBo.getEventType(), agentEventBo.getEventBody());
            } else if (agentEventBo.getVersion() == AgentEventBo.CURRENT_VERSION) {
                return this.agentEventMessageDeserializerV1.deserialize(agentEventBo.getEventType(), agentEventBo.getEventBody());
            } else {
                throw new UnsupportedEncodingException("invalid version " + agentEventBo.getVersion());
            }
        } catch (UnsupportedEncodingException e) {
            logger.warn("error deserializing event message", e);
            return null;
        }
    }

}
