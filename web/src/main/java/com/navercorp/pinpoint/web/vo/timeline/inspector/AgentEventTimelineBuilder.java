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

import com.navercorp.pinpoint.web.filter.agent.AgentEventFilter;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentEventTimelineBuilder {

    private final long timelineStartTimestamp;
    private final long timelineEndTimestamp;

    private List<AgentEvent> agentEvents = Collections.emptyList();
    private List<AgentEventFilter> filters = new ArrayList<>();

    public AgentEventTimelineBuilder(Range range) {
        this.timelineStartTimestamp = range.getFrom();
        this.timelineEndTimestamp = range.getTo();
    }

    public AgentEventTimelineBuilder from(List<AgentEvent> agentEvents) {
        if (agentEvents != null) {
            this.agentEvents = agentEvents;
        }
        return this;
    }

    public AgentEventTimelineBuilder addFilter(AgentEventFilter filter) {
        if (filter != null) {
            filters.add(filter);
        }
        return this;
    }

    public AgentEventTimeline build() {
        List<AgentEvent> filteredAgentEvents = new ArrayList<>();
        for (AgentEvent agentEvent : agentEvents) {
            if (filterAgentEvent(agentEvent) == AgentEventFilter.ACCEPT) {
                filteredAgentEvents.add(agentEvent);
            }
        }
        return new AgentEventTimeline(createTimelineSegments(filteredAgentEvents));
    }

    private boolean filterAgentEvent(AgentEvent agentEvent) {
        for (AgentEventFilter filter : filters) {
            if (!filter.accept(agentEvent)) {
                return AgentEventFilter.REJECT;
            }
        }
        return AgentEventFilter.ACCEPT;
    }

    private List<AgentEventTimeline.Segment> createTimelineSegments(List<AgentEvent> agentEvents) {
        AgentEventTimeline.Segment segment = new AgentEventTimeline.Segment();
        segment.setStartTimestamp(timelineStartTimestamp);
        segment.setEndTimestamp(timelineEndTimestamp);
        segment.setValue(agentEvents);
        return Collections.singletonList(segment);
    }
}
