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

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.view.AgentEventSerializer;

import java.util.Comparator;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = AgentEventSerializer.class)
public class AgentEvent {

    public static final Comparator<AgentEvent> EVENT_TIMESTAMP_ASC_COMPARATOR = Comparator
                    .comparingLong(AgentEvent::getEventTimestamp)
                    .thenComparing(Comparator.comparingInt(AgentEvent::getEventTypeCode).reversed());

    private final String agentId;

    private final long startTimestamp;

    private final long eventTimestamp;

    private final AgentEventType eventType;

    private final Object eventMessage;

    public static AgentEvent from(AgentEventBo event) {
        Objects.requireNonNull(event, "event");

        final AgentEventType eventType = event.getEventType();

        return new AgentEvent(event.getAgentId(), event.getStartTimestamp(), event.getEventTimestamp(), eventType, null);
    }

    public static AgentEvent withEventMessage(AgentEventBo event, Object message) {
        Objects.requireNonNull(event, "event");

        final AgentEventType eventType = event.getEventType();

        return new AgentEvent(event.getAgentId(), event.getStartTimestamp(), event.getEventTimestamp(), eventType, message);
    }

    public AgentEvent(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType) {
        this(agentId, startTimestamp, eventTimestamp, eventType, null);
    }

    public AgentEvent(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType, Object eventMessage) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.eventTimestamp = eventTimestamp;
        this.startTimestamp = startTimestamp;
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.eventMessage = eventMessage;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public int getEventTypeCode() {
        return eventType.getCode();
    }

    public String getEventTypeDesc() {
        return eventType.getDesc();
    }

    public boolean hasEventMessage() {
        return eventType.getMessageType() != Void.class;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public Object getEventMessage() {
        return eventMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentEvent that = (AgentEvent) o;

        if (eventTimestamp != that.eventTimestamp) return false;
        if (startTimestamp != that.startTimestamp) return false;
        if (!Objects.equals(agentId, that.agentId)) return false;
        return eventType == that.eventType;
    }

    @Override
    public int hashCode() {
        int result = agentId != null ? agentId.hashCode() : 0;
        result = 31 * result + (int) (eventTimestamp ^ (eventTimestamp >>> 32));
        result = 31 * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AgentEvent{" + "agentId='" + agentId + '\'' + ", eventTimestamp=" + eventTimestamp + ", startTimestamp=" + startTimestamp + ", eventType=" + eventType + ", eventMessage=" + eventMessage + '}';
    }
}
