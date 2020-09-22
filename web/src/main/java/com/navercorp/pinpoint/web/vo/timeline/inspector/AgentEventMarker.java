/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.timeline.inspector;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.view.AgentEventMarkerSerializer;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@JsonSerialize(using = AgentEventMarkerSerializer.class)
public class AgentEventMarker {

    private final Map<AgentEventType, MutableInt> typeCounts = new EnumMap<>(AgentEventType.class);

    private int totalCount = 0;

    public void addAgentEvent(AgentEvent agentEvent) {
        AgentEventType agentEventType = AgentEventType.getTypeByCode(agentEvent.getEventTypeCode());
        if (agentEventType != null) {
            MutableInt typeCount = typeCounts.computeIfAbsent(agentEventType, k -> new MutableInt());
            typeCount.increment();
            totalCount++;
        }
    }

    public Map<AgentEventType, Integer> getTypeCounts() {
        Map<AgentEventType, Integer> typeCounts = new EnumMap<>(AgentEventType.class);
        for (Map.Entry<AgentEventType, MutableInt> e : this.typeCounts.entrySet()) {
            typeCounts.put(e.getKey(), e.getValue().intValue());
        }
        return typeCounts;
    }

    public int getTotalCount() {
        return totalCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentEventMarker that = (AgentEventMarker) o;
        if (totalCount != that.totalCount) return false;
        return typeCounts.equals(that.typeCounts);

    }

    @Override
    public int hashCode() {
        int result = typeCounts.hashCode();
        result = 31 * result + totalCount;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentEventMarker{");
        sb.append("typeCounts=").append(typeCounts);
        sb.append(", totalCount=").append(totalCount);
        sb.append('}');
        return sb.toString();
    }

}
