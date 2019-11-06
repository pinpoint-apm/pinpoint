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

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = AgentEventSerializer.class)
public class AgentEvent {

    public static final Comparator<AgentEvent> EVENT_TIMESTAMP_ASC_COMPARATOR = new Comparator<AgentEvent>() {
        @Override
        public int compare(AgentEvent o1, AgentEvent o2) {
            int eventTimestampComparison = Long.compare(o1.eventTimestamp, o2.eventTimestamp);
            if (eventTimestampComparison == 0) {
                return o2.eventTypeCode - o1.eventTypeCode;
            }
            return eventTimestampComparison;
        }
    };

    public static final Comparator<AgentEvent> EVENT_TIMESTAMP_DESC_COMPARATOR = new Comparator<AgentEvent>() {
        @Override
        public int compare(AgentEvent o1, AgentEvent o2) {
            int eventTimestampComparison = Long.compare(o2.eventTimestamp, o1.eventTimestamp);
            if (eventTimestampComparison == 0) {
                return o1.eventTypeCode - o2.eventTypeCode;
            }
            return eventTimestampComparison;
        }
    };

    private final String agentId;

    private final long eventTimestamp;

    private final int eventTypeCode;

    private final String eventTypeDesc;

    private final boolean hasEventMessage;

    private final long startTimestamp;

    private Object eventMessage;

    public AgentEvent(AgentEventBo agentEventBo) {
        this.agentId = agentEventBo.getAgentId();
        this.eventTimestamp = agentEventBo.getEventTimestamp();
        this.startTimestamp = agentEventBo.getStartTimestamp();
        AgentEventType eventType = agentEventBo.getEventType();
        this.eventTypeCode = eventType.getCode();
        this.eventTypeDesc = eventType.getDesc();
        this.hasEventMessage = eventType.getMessageType() != Void.class;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public int getEventTypeCode() {
        return eventTypeCode;
    }

    public String getEventTypeDesc() {
        return eventTypeDesc;
    }

    public boolean hasEventMessage() {
        return this.hasEventMessage;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public Object getEventMessage() {
        return eventMessage;
    }

    public void setEventMessage(Object eventMessage) {
        this.eventMessage = eventMessage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        result = prime * result + (int)(eventTimestamp ^ (eventTimestamp >>> 32));
        result = prime * result + eventTypeCode;
        result = prime * result + ((eventTypeDesc == null) ? 0 : eventTypeDesc.hashCode());
        result = prime * result + (hasEventMessage ? 1231 : 1237);
        result = prime * result + (int)(startTimestamp ^ (startTimestamp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AgentEvent other = (AgentEvent)obj;
        if (agentId == null) {
            if (other.agentId != null)
                return false;
        } else if (!agentId.equals(other.agentId))
            return false;
        if (eventTimestamp != other.eventTimestamp)
            return false;
        if (eventTypeCode != other.eventTypeCode)
            return false;
        if (eventTypeDesc == null) {
            if (other.eventTypeDesc != null)
                return false;
        } else if (!eventTypeDesc.equals(other.eventTypeDesc))
            return false;
        if (hasEventMessage != other.hasEventMessage)
            return false;
        if (startTimestamp != other.startTimestamp)
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentEvent{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", eventTimestamp=").append(eventTimestamp);
        sb.append(", eventTypeCode=").append(eventTypeCode);
        sb.append(", eventTypeDesc='").append(eventTypeDesc).append('\'');
        sb.append(", hasEventMessage=").append(hasEventMessage);
        sb.append(", startTimestamp=").append(startTimestamp);
        sb.append(", eventMessage=").append(eventMessage);
        sb.append('}');
        return sb.toString();
    }
}
