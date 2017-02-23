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
public class AgentStatusTimeline implements Timeline<AgentStatusTimelineSegment> {

    private final List<AgentStatusTimelineSegment> timelineSegments;
    private final boolean includeWarning;

    public AgentStatusTimeline(List<AgentStatusTimelineSegment> timelineSegments, boolean includeWarning) {
        this.timelineSegments = timelineSegments;
        this.includeWarning = includeWarning;
    }

    @Override
    public List<AgentStatusTimelineSegment> getTimelineSegments() {
        return timelineSegments;
    }

    public boolean isIncludeWarning() {
        return includeWarning;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentStatusTimeline that = (AgentStatusTimeline) o;

        if (includeWarning != that.includeWarning) return false;
        return timelineSegments != null ? timelineSegments.equals(that.timelineSegments) : that.timelineSegments == null;

    }

    @Override
    public int hashCode() {
        int result = timelineSegments != null ? timelineSegments.hashCode() : 0;
        result = 31 * result + (includeWarning ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentStatusTimeline{");
        sb.append("timelineSegments=").append(timelineSegments);
        sb.append(", includeWarning=").append(includeWarning);
        sb.append('}');
        return sb.toString();
    }

}
