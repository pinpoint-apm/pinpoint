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
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author HyunGil Jeong
 */
public class AgentEventTimelineBuilder {

    private static final int DEFAULT_NUM_TIMESLOTS = 50;

    private final long timelineStartTimestamp;
    private final long timelineEndTimestamp;
    private final long timeslotSize;
    private final int numTimeslots;

    private List<AgentEvent> agentEvents = Collections.emptyList();
    private List<AgentEventFilter> filters = new ArrayList<>();

    public AgentEventTimelineBuilder(Range range) {
        this(range, DEFAULT_NUM_TIMESLOTS);
    }

    public AgentEventTimelineBuilder(Range range, int numTimeslots) {
        Objects.requireNonNull(range, "range");
        Assert.isTrue(range.getRange() > 0, "timeline must have range greater than 0");
        Assert.isTrue(numTimeslots > 0, "numTimeslots must be greater than 0");
        this.timelineStartTimestamp = range.getFrom();
        this.timelineEndTimestamp = range.getTo();
        int adjustedNumTimeslots = numTimeslots;
        if (range.getRange() < adjustedNumTimeslots) {
            adjustedNumTimeslots = (int) range.getRange();
        }
        this.timeslotSize = (timelineEndTimestamp - timelineStartTimestamp) / adjustedNumTimeslots;
        this.numTimeslots = adjustedNumTimeslots;
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

    private List<AgentEventTimelineSegment> createTimelineSegments(List<AgentEvent> agentEvents) {
        if (agentEvents.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<AgentEvent>> timeWindowEventsMap = createTimeslotIndexMap(agentEvents);

        List<AgentEventTimelineSegment> timelineSegments = new ArrayList<>();
        for (Map.Entry<Long, List<AgentEvent>> e : timeWindowEventsMap.entrySet()) {
            AgentEventTimelineSegment segment = createSegment(e.getKey(), e.getValue());
            if (segment != null) {
                timelineSegments.add(segment);
            }
        }
        return timelineSegments;
    }

    private Map<Long, List<AgentEvent>> createTimeslotIndexMap(List<AgentEvent> agentEvents) {
        Map<Long, List<AgentEvent>> timeslotIndexMap = new TreeMap<>();
        for (AgentEvent agentEvent : agentEvents) {
            long timeslotIndex = getTimeslotIndex(agentEvent.getEventTimestamp());
            List<AgentEvent> timeslotAgentEvents = timeslotIndexMap.get(timeslotIndex);
            if (timeslotAgentEvents == null) {
                timeslotAgentEvents = new ArrayList<>();
                timeslotIndexMap.put(timeslotIndex, timeslotAgentEvents);
            }
            timeslotAgentEvents.add(agentEvent);
        }
        return timeslotIndexMap;
    }

    private AgentEventTimelineSegment createSegment(long timeslotIndex, List<AgentEvent> agentEvents) {
        // timeslotIndex guaranteed to be greater than 0 and less than numTimeslots
        long segmentStartTimestamp = timelineStartTimestamp + (timeslotIndex * timeslotSize);
        long segmentEndTimestamp = segmentStartTimestamp + timeslotSize;
        if (timeslotIndex >= (numTimeslots - 1)) {
            segmentEndTimestamp = timelineEndTimestamp;
        }

        AgentEventMarker agentEventMarker = createAgentEventMarker(agentEvents);
        if (agentEventMarker.getTotalCount() == 0) {
            return null;
        }
        AgentEventTimelineSegment segment = new AgentEventTimelineSegment();
        segment.setStartTimestamp(segmentStartTimestamp);
        segment.setEndTimestamp(segmentEndTimestamp);
        segment.setValue(agentEventMarker);
        return segment;
    }

    private long getTimeslotIndex(long timestamp) {
        long diff = timestamp - timelineStartTimestamp;
        long index = diff / timeslotSize;
        if (index < 0) {
            index = 0;
        }
        if (index >= numTimeslots) {
            index = numTimeslots - 1;
        }
        return index;
    }

    private AgentEventMarker createAgentEventMarker(List<AgentEvent> agentEvents) {
        AgentEventMarker agentEventMarker = new AgentEventMarker();
        for (AgentEvent agentEvent : agentEvents) {
            agentEventMarker.addAgentEvent(agentEvent);
        }
        return agentEventMarker;
    }
}
