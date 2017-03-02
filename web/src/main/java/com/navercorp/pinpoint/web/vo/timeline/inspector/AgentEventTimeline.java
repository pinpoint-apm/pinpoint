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

import com.navercorp.pinpoint.web.vo.timeline.Timeline;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentEventTimeline implements Timeline<AgentEventTimelineSegment> {

    private final List<AgentEventTimelineSegment> timelineSegments;

    public AgentEventTimeline(List<AgentEventTimelineSegment> timelineSegments) {
        this.timelineSegments = timelineSegments;
    }

    @Override
    public List<AgentEventTimelineSegment> getTimelineSegments() {
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

}
