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
public class AgentStatusTimelineSegment implements TimelineSegment<AgentState> {

    private long startTimestamp;
    private long endTimestamp;
    private AgentState value;

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

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @Override
    public AgentState getValue() {
        return value;
    }

    public void setValue(AgentState state) {
        this.value = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentStatusTimelineSegment that = (AgentStatusTimelineSegment) o;

        if (startTimestamp != that.startTimestamp) return false;
        if (endTimestamp != that.endTimestamp) return false;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        int result = (int) (startTimestamp ^ (startTimestamp >>> 32));
        result = 31 * result + (int) (endTimestamp ^ (endTimestamp >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Segment{");
        sb.append("startTimestamp=").append(startTimestamp);
        sb.append(", endTimestamp=").append(endTimestamp);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
