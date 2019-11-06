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

import com.navercorp.pinpoint.web.vo.timeline.TimelineSegment;

/**
 * @author HyunGil Jeong
 */
public class AgentEventTimelineSegment implements TimelineSegment<AgentEventMarker> {
    private long startTimestamp;
    private long endTimestamp;
    private AgentEventMarker agentEventMarker;

    @Override
    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    @Override
    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setValue(AgentEventMarker agentEventMarker) {
        this.agentEventMarker = agentEventMarker;
    }

    @Override
    public AgentEventMarker getValue() {
        return agentEventMarker;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentEventTimelineSegment segment = (AgentEventTimelineSegment) o;
        if (startTimestamp != segment.startTimestamp) return false;
        if (endTimestamp != segment.endTimestamp) return false;
        return agentEventMarker != null ? agentEventMarker.equals(segment.agentEventMarker) : segment.agentEventMarker == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (endTimestamp ^ (endTimestamp >>> 32));
        result = 31 * result + (agentEventMarker != null ? agentEventMarker.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentEventTimelineSegment{");
        sb.append("startTimestamp=").append(startTimestamp);
        sb.append(", endTimestamp=").append(endTimestamp);
        sb.append(", agentEventMarker=").append(agentEventMarker);
        sb.append('}');
        return sb.toString();
    }
}
