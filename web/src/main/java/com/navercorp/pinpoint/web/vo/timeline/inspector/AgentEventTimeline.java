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

import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.timeline.Timeline;
import com.navercorp.pinpoint.web.vo.timeline.TimelineSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentEventTimeline implements Timeline<AgentEventTimeline.Segment> {

    private final List<AgentEventTimeline.Segment> timelineSegments;

    AgentEventTimeline(List<AgentEventTimeline.Segment> timelineSegments) {
        this.timelineSegments = timelineSegments;
    }

    @Override
    public List<AgentEventTimeline.Segment> getTimelineSegments() {
        return timelineSegments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentEventTimeline that = (AgentEventTimeline) o;

        return timelineSegments != null ? timelineSegments.equals(that.timelineSegments) : that.timelineSegments == null;
    }

    @Override
    public int hashCode() {
        return timelineSegments != null ? timelineSegments.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentEventTimeline{");
        sb.append("timelineSegments=").append(timelineSegments);
        sb.append('}');
        return sb.toString();
    }

    public static class Segment implements TimelineSegment<List<AgentEvent>> {
        private long startTimestamp;
        private long endTimestamp;
        private List<AgentEvent> agentEvents = new ArrayList<>();

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
        public List<AgentEvent> getValue() {
            return agentEvents;
        }

        public void setValue(List<AgentEvent> agentEvents) {
            this.agentEvents = agentEvents;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Segment segment = (Segment) o;

            if (startTimestamp != segment.startTimestamp) return false;
            if (endTimestamp != segment.endTimestamp) return false;
            return agentEvents != null ? agentEvents.equals(segment.agentEvents) : segment.agentEvents == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (startTimestamp ^ (startTimestamp >>> 32));
            result = 31 * result + (int) (endTimestamp ^ (endTimestamp >>> 32));
            result = 31 * result + (agentEvents != null ? agentEvents.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Segment{");
            sb.append("startTimestamp=").append(startTimestamp);
            sb.append(", endTimestamp=").append(endTimestamp);
            sb.append(", agentEvents=").append(agentEvents);
            sb.append('}');
            return sb.toString();
        }
    }
}
