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

import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory;
import com.navercorp.pinpoint.web.filter.agent.AgentEventFilter;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author HyunGil Jeong
 */
public class AgentStatusTimelineBuilder {

    private static final AgentEventFilter LIFECYCLE_EVENT_FILTER = new AgentEventFilter() {
        @Override
        public boolean accept(AgentEvent agentEvent) {
            AgentEventType agentEventType = AgentEventType.getTypeByCode(agentEvent.getEventTypeCode());
            if (agentEventType == null) {
                return REJECT;
            }
            if (agentEventType.isCategorizedAs(AgentEventTypeCategory.AGENT_LIFECYCLE)) {
                return ACCEPT;
            }
            return REJECT;
        }
    };

    private final long timelineStartTimestamp;
    private final long timelineEndTimestamp;
    private final AgentState initialState;

    private final List<AgentEvent> agentEvents;
    private final List<AgentStatusTimelineSegment> warningStatusTimelineSegmentList;

    private boolean hasOverlap = false;

    public AgentStatusTimelineBuilder(Range range, AgentStatus initialStatus) {
        this(range, initialStatus, null);
    }

    public AgentStatusTimelineBuilder(Range range, AgentStatus initialStatus, List<AgentEvent> agentEvents) {
        this(range, initialStatus, agentEvents, null);
    }

    public AgentStatusTimelineBuilder(Range range, AgentStatus initialStatus, List<AgentEvent> agentEvents, List<AgentStatusTimelineSegment> warningStatusTimelineSegmentList) {
        Objects.requireNonNull(range, "range");
        Assert.isTrue(range.getRange() > 0, "timeline must have range greater than 0");
        timelineStartTimestamp = range.getFrom();
        timelineEndTimestamp = range.getTo();
        if (initialStatus == null) {
            initialState = AgentState.UNKNOWN;
        } else {
            initialState = AgentState.fromAgentLifeCycleState(initialStatus.getState());
        }

        if (agentEvents == null) {
            this.agentEvents = Collections.emptyList();
        } else {
            this.agentEvents = Collections.unmodifiableList(agentEvents);
        }

        if (warningStatusTimelineSegmentList == null) {
            this.warningStatusTimelineSegmentList = Collections.emptyList();
        } else {
            this.warningStatusTimelineSegmentList = Collections.unmodifiableList(warningStatusTimelineSegmentList);
        }
    }

    public AgentStatusTimeline build() {
        List<AgentStatusTimelineSegment> timelineSegments = createTimelineSegments(agentEvents);

        if (com.navercorp.pinpoint.common.util.CollectionUtils.hasLength(warningStatusTimelineSegmentList)) {
            List<AgentStatusTimelineSegment> timelineSegmentList = mergeTimelineSegment(timelineSegments, warningStatusTimelineSegmentList);
            return new AgentStatusTimeline(timelineSegmentList, hasOverlap);
        } else {
            return new AgentStatusTimeline(timelineSegments, hasOverlap);
        }
    }

    private List<AgentStatusTimelineSegment> mergeTimelineSegment(List<AgentStatusTimelineSegment> timelineSegments, List<AgentStatusTimelineSegment> warningStatusTimelineSegmentList) {
        List<AgentStatusTimelineSegment> result = new ArrayList<>();

        for (AgentStatusTimelineSegment segment : timelineSegments) {
            long startTimestamp = segment.getStartTimestamp();
            long endTimestamp = segment.getEndTimestamp();

            if (segment.getValue() == AgentState.RUNNING) {
                List<AgentStatusTimelineSegment> includedSegment = getIncludedSegment(startTimestamp, endTimestamp, warningStatusTimelineSegmentList);
                if (com.navercorp.pinpoint.common.util.CollectionUtils.hasLength(includedSegment)) {
                    List<AgentStatusTimelineSegment> newTimelineSegment = createNewTimelineSegment(segment, includedSegment);
                    result.addAll(newTimelineSegment);
                } else {
                    result.add(segment);
                }
            } else {
                result.add(segment);
            }
        }

        return result;
    }

    private List<AgentStatusTimelineSegment> getIncludedSegment(long startTimestamp, long endTimestamp, List<AgentStatusTimelineSegment> warningStatusTimelineSegmentList) {
        List<AgentStatusTimelineSegment> result = new ArrayList<>();

        for (AgentStatusTimelineSegment agentStatusTimelineSegment : warningStatusTimelineSegmentList) {
            if (agentStatusTimelineSegment.getStartTimestamp() < startTimestamp) {
                continue;
            }
            if (agentStatusTimelineSegment.getEndTimestamp() > endTimestamp) {
                continue;
            }

            result.add(agentStatusTimelineSegment);
        }

        return result;
    }

    private List<AgentStatusTimelineSegment> createNewTimelineSegment(AgentStatusTimelineSegment baseSegment, List<AgentStatusTimelineSegment> warningStatusTimelineSegmentList) {
        List<AgentStatusTimelineSegment> result = new ArrayList<>();

        long startTimestamp = baseSegment.getStartTimestamp();

        for (AgentStatusTimelineSegment segment : warningStatusTimelineSegmentList) {
            if (segment.getStartTimestamp() != startTimestamp) {
                AgentStatusTimelineSegment newSegment = new AgentStatusTimelineSegment();
                newSegment.setStartTimestamp(startTimestamp);
                newSegment.setEndTimestamp(segment.getStartTimestamp());
                newSegment.setValue(AgentState.RUNNING);

                result.add(newSegment);
            }

            result.add(segment);
            startTimestamp = segment.getEndTimestamp();
        }

        if (startTimestamp < baseSegment.getEndTimestamp()) {
            AgentStatusTimelineSegment newSegment = new AgentStatusTimelineSegment();
            newSegment.setStartTimestamp(startTimestamp);
            newSegment.setEndTimestamp(baseSegment.getEndTimestamp());
            newSegment.setValue(AgentState.RUNNING);
            result.add(newSegment);
        }

        return result;
    }

    private List<AgentEvent> filterAgentEvents(AgentEventFilter agentEventFilter, List<AgentEvent> agentEvents) {
        List<AgentEvent> filteredEvents = new ArrayList<>();
        for (AgentEvent agentEvent : agentEvents) {
            if (agentEventFilter.accept(agentEvent)) {
                filteredEvents.add(agentEvent);
            }
        }
        return filteredEvents;
    }

    private List<AgentStatusTimelineSegment> createTimelineSegments(List<AgentEvent> agentEvents) {
        if (CollectionUtils.isEmpty(agentEvents)) {
            AgentStatusTimelineSegment segment = createSegment(timelineStartTimestamp, timelineEndTimestamp, initialState);
            return Collections.singletonList(segment);
        } else {
            List<AgentEvent> lifeCycleEvents = filterAgentEvents(LIFECYCLE_EVENT_FILTER, agentEvents);
            List<AgentLifeCycle> agentLifeCycles = createAgentLifeCycles(lifeCycleEvents);
            return convertToTimelineSegments(agentLifeCycles);
        }
    }

    private List<AgentLifeCycle> createAgentLifeCycles(List<AgentEvent> agentEvents) {
        Map<Long, List<AgentEvent>> partitions = partitionByStartTimestamp(agentEvents);
        List<AgentLifeCycle> agentLifeCycles = new ArrayList<>(partitions.size());
        for (Map.Entry<Long, List<AgentEvent>> e : partitions.entrySet()) {
            Long agentStartTimestamp = e.getKey();
            List<AgentEvent> agentLifeCycleEvents = e.getValue();
            agentLifeCycles.add(createAgentLifeCycle(agentStartTimestamp, agentLifeCycleEvents));
        }
        return mergeOverlappingLifeCycles(agentLifeCycles);
    }

    private Map<Long, List<AgentEvent>> partitionByStartTimestamp(List<AgentEvent> agentEvents) {
        Map<Long, List<AgentEvent>> partitions = new HashMap<>();
        for (AgentEvent agentEvent : agentEvents) {
            long startTimestamp = agentEvent.getStartTimestamp();
            List<AgentEvent> partition = partitions.computeIfAbsent(startTimestamp, k -> new ArrayList<>());
            partition.add(agentEvent);
        }
        return partitions;
    }

    private AgentLifeCycle createAgentLifeCycle(long agentStartTimestamp, List<AgentEvent> agentEvents) {
        agentEvents.sort(AgentEvent.EVENT_TIMESTAMP_ASC_COMPARATOR);

        AgentEvent first = agentEvents.get(0);
        AgentEvent last = agentEvents.get(agentEvents.size() - 1);
        AgentState endState = AgentState.fromAgentEvent(last);
        long startTimestamp = first.getStartTimestamp();
        if (agentStartTimestamp <= timelineStartTimestamp) {
            startTimestamp = timelineStartTimestamp;
        }
        long endTimestamp = last.getEventTimestamp();
        if (endState == AgentState.RUNNING) {
            endTimestamp = timelineEndTimestamp;
        }
        return new AgentLifeCycle(startTimestamp, endTimestamp, endState);
    }

    private List<AgentLifeCycle> mergeOverlappingLifeCycles(List<AgentLifeCycle> agentLifeCycles) {
        agentLifeCycles.sort(AgentLifeCycle.START_TIMESTAMP_ASC_COMPARATOR);

        Queue<AgentLifeCycle> mergedAgentLifeCycles = new PriorityQueue<>(agentLifeCycles.size(), AgentLifeCycle.START_TIMESTAMP_ASC_COMPARATOR);
        for (AgentLifeCycle agentLifeCycle : agentLifeCycles) {
            Iterator<AgentLifeCycle> mergedAgentLifeCyclesIterator = mergedAgentLifeCycles.iterator();
            while (mergedAgentLifeCyclesIterator.hasNext()) {
                AgentLifeCycle mergedAgentLifeCycle = mergedAgentLifeCyclesIterator.next();
                if (mergedAgentLifeCycle.isOverlapping(agentLifeCycle)) {
                    mergedAgentLifeCyclesIterator.remove();
                    agentLifeCycle = AgentLifeCycle.merge(agentLifeCycle, mergedAgentLifeCycle);
                    hasOverlap = true;
                }
            }
            mergedAgentLifeCycles.add(agentLifeCycle);
        }
        return new ArrayList<>(mergedAgentLifeCycles);
    }

    private List<AgentStatusTimelineSegment> convertToTimelineSegments(List<AgentLifeCycle> agentLifeCycles) {
        List<AgentStatusTimelineSegment> segments = new ArrayList<>();
        AgentStatusTimelineSegment fillerSegment = null;
        for (AgentLifeCycle agentLifeCycle : agentLifeCycles) {
            if (fillerSegment != null) {
                fillerSegment.setEndTimestamp(agentLifeCycle.getStartTimestamp());
                segments.add(fillerSegment);
            } else if (agentLifeCycle.getStartTimestamp() > timelineStartTimestamp) {
                if (initialState == AgentState.RUNNING) {
                    hasOverlap = true;
                }
                fillerSegment = initializeFillerSegment(timelineStartTimestamp, initialState);
                fillerSegment.setEndTimestamp(agentLifeCycle.getStartTimestamp());
                segments.add(fillerSegment);
            }
            AgentStatusTimelineSegment lifeCycleSegment = agentLifeCycle.toTimelineSegment();
            segments.add(lifeCycleSegment);
            fillerSegment = initializeFillerSegment(agentLifeCycle.getEndTimestamp(), agentLifeCycle.getEndState());

        }
        if (fillerSegment != null && fillerSegment.getStartTimestamp() < timelineEndTimestamp) {
            fillerSegment.setEndTimestamp(timelineEndTimestamp);
            segments.add(fillerSegment);
        }
        return segments;
    }

    private AgentStatusTimelineSegment initializeFillerSegment(long startTimestamp, AgentState state) {
        AgentStatusTimelineSegment fillerSegment = new AgentStatusTimelineSegment();
        fillerSegment.setStartTimestamp(startTimestamp);
        fillerSegment.setValue(state);
        return fillerSegment;
    }

    private AgentStatusTimelineSegment createSegment(long startTimestamp, long endTimestamp, AgentState state) {
        AgentStatusTimelineSegment segment = new AgentStatusTimelineSegment();
        segment.setStartTimestamp(startTimestamp);
        segment.setEndTimestamp(endTimestamp);
        segment.setValue(state);
        return segment;
    }

    private static class AgentLifeCycle {

        private static final Comparator<AgentLifeCycle> START_TIMESTAMP_ASC_COMPARATOR = new Comparator<AgentLifeCycle>() {
            @Override
            public int compare(AgentLifeCycle o1, AgentLifeCycle o2) {
                return Long.compare(o1.getStartTimestamp(), o2.getStartTimestamp());
            }
        };

        private final long startTimestamp;
        private final long endTimestamp;
        private final AgentState endState;

        private AgentLifeCycle(long startTimestamp, long endTimestamp, AgentState endState) {
            if (startTimestamp >= endTimestamp) {
                throw new IllegalArgumentException("startTimestamp must be less than endTimestamp");
            }
            this.startTimestamp = startTimestamp;
            this.endTimestamp = endTimestamp;
            this.endState = endState;
        }

        private static AgentLifeCycle merge(AgentLifeCycle o1, AgentLifeCycle o2) {
            long newStartTimestamp = Math.min(o1.getStartTimestamp(), o2.getStartTimestamp());
            if (o1.getEndTimestamp() > o2.getEndTimestamp()) {
                return new AgentLifeCycle(newStartTimestamp, o1.getEndTimestamp(), o1.getEndState());
            } else {
                return new AgentLifeCycle(newStartTimestamp, o2.getEndTimestamp(), o2.getEndState());
            }
        }

        private long getStartTimestamp() {
            return startTimestamp;
        }

        private long getEndTimestamp() {
            return endTimestamp;
        }

        private AgentState getEndState() {
            return endState;
        }

        private AgentStatusTimelineSegment toTimelineSegment() {
            AgentStatusTimelineSegment timelineSegment = new AgentStatusTimelineSegment();
            timelineSegment.setStartTimestamp(startTimestamp);
            timelineSegment.setEndTimestamp(endTimestamp);
            timelineSegment.setValue(AgentState.RUNNING);
            return timelineSegment;
        }

        private boolean isOverlapping(AgentLifeCycle other) {
            if (this.startTimestamp < other.getStartTimestamp()) {
                return other.getStartTimestamp() <= this.endTimestamp;
            } else if (this.startTimestamp > other.getStartTimestamp()) {
                return this.startTimestamp <= other.getEndTimestamp();
            } else {
                return true;
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AgentLifeCycle{");
            sb.append("startTimestamp=").append(startTimestamp);
            sb.append(", endTimestamp=").append(endTimestamp);
            sb.append(", endState=").append(endState);
            sb.append('}');
            return sb.toString();
        }
    }

}
