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

import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.filter.agent.AgentEventFilter;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author HyunGil Jeong
 */
public class AgentEventTimelineTest {

    @Test
    public void noFilter() {
        // Given
        Range timelineRange = Range.between(100, 200);
        List<AgentEvent> agentEvents = List.of(
                createAgentEvent(140, AgentEventType.AGENT_PING),
                createAgentEvent(190, AgentEventType.AGENT_PING));
        List<AgentEventTimelineSegment> expectedTimelineSegments = List.of(
                createSegment(100, 200, agentEvents));
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(timelineRange, 1)
                .from(agentEvents)
                .build();
        // Then
        Assertions.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
    }

    @Test
    public void nullFilter() {
        // Given
        Range timelineRange = Range.between(100, 200);
        List<AgentEvent> agentEvents = List.of(
                createAgentEvent(140, AgentEventType.AGENT_PING),
                createAgentEvent(190, AgentEventType.AGENT_PING));
        List<AgentEventTimelineSegment> expectedTimelineSegments = List.of(
                createSegment(100, 200, agentEvents));
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(timelineRange, 1)
                .from(agentEvents)
                .addFilter(null)
                .build();
        // Then
        Assertions.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
    }

    @Test
    public void multipleFilters() {
        // Given
        Range timelineRange = Range.between(100, 200);
        List<AgentEvent> agentEvents = List.of(
                createAgentEvent(110, AgentEventType.AGENT_PING),
                createAgentEvent(120, AgentEventType.AGENT_CONNECTED),
                createAgentEvent(130, AgentEventType.AGENT_SHUTDOWN),
                createAgentEvent(140, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN),
                createAgentEvent(150, AgentEventType.AGENT_CLOSED_BY_SERVER),
                createAgentEvent(160, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER),
                createAgentEvent(170, AgentEventType.USER_THREAD_DUMP),
                createAgentEvent(180, AgentEventType.OTHER));
        Set<AgentEventType> includedAgentEventTypes = new HashSet<>() {{
            add(AgentEventType.AGENT_PING);
            add(AgentEventType.AGENT_CONNECTED);
            add(AgentEventType.AGENT_SHUTDOWN);
            add(AgentEventType.AGENT_CLOSED_BY_SERVER);
        }};
        AgentEventFilter excludeUnexpectedEventsFilter = new AgentEventFilter.ExcludeFilter(
                AgentEventType.AGENT_UNEXPECTED_SHUTDOWN, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER);
        AgentEventFilter excludeUserThreadDumpFilter = new AgentEventFilter.ExcludeFilter(AgentEventType.USER_THREAD_DUMP);
        AgentEventFilter excludeOtherFilter = new AgentEventFilter.ExcludeFilter(AgentEventType.OTHER);
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(timelineRange, 1)
                .from(agentEvents)
                .addFilter(excludeUnexpectedEventsFilter)
                .addFilter(excludeUserThreadDumpFilter)
                .addFilter(excludeOtherFilter)
                .build();
        // Then
        int allEventsTotalCount = 0;
        for (AgentEventTimelineSegment segment : timeline.getTimelineSegments()) {
            AgentEventMarker marker = segment.getValue();
            allEventsTotalCount += marker.getTotalCount();
            Map<AgentEventType, Integer> eventTypeCountMap = marker.getTypeCounts();

            assertThat(includedAgentEventTypes)
                    .containsAll(eventTypeCountMap.keySet())
                    .doesNotContain(AgentEventType.AGENT_UNEXPECTED_SHUTDOWN)
                    .doesNotContain(AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER)
                    .doesNotContain(AgentEventType.USER_THREAD_DUMP)
                    .doesNotContain(AgentEventType.OTHER);
        }
        assertThat(includedAgentEventTypes).hasSize(allEventsTotalCount);
    }

    @Test
    public void leftBiasedSpread() {
        // Given
        Range range = Range.between(100, 200);
        AgentEvent event1 = createAgentEvent(0, AgentEventType.AGENT_CONNECTED);
        AgentEvent event2 = createAgentEvent(5, AgentEventType.AGENT_PING);
        AgentEvent event3 = createAgentEvent(50, AgentEventType.AGENT_PING);
        AgentEvent event4 = createAgentEvent(100, AgentEventType.AGENT_PING);
        AgentEvent event5 = createAgentEvent(150, AgentEventType.AGENT_PING);
        AgentEvent event6 = createAgentEvent(220, AgentEventType.AGENT_SHUTDOWN);
        List<AgentEventTimelineSegment> expectedTimelineSegments = List.of(
                createSegment(100, 101, event1, event2, event3, event4),
                createSegment(150, 151, event5),
                createSegment(199, 200, event6));
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(range, 100)
                .from(List.of(event1, event2, event3, event4, event5, event6))
                .build();
        // Then
        Assertions.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
    }

    @Test
    public void rightBiasedSpread() {
        // Given
        Range range = Range.between(0, 199);
        AgentEvent event1 = createAgentEvent(0, AgentEventType.AGENT_CONNECTED);
        AgentEvent event2 = createAgentEvent(5, AgentEventType.AGENT_PING);
        AgentEvent event3 = createAgentEvent(100, AgentEventType.AGENT_PING);
        AgentEvent event4 = createAgentEvent(110, AgentEventType.AGENT_PING);
        AgentEvent event5 = createAgentEvent(199, AgentEventType.AGENT_PING);
        AgentEvent event6 = createAgentEvent(200, AgentEventType.AGENT_SHUTDOWN);
        List<AgentEventTimelineSegment> expectedTimelineSegments = List.of(
                createSegment(0, 1, event1),
                createSegment(5, 6, event2),
                createSegment(99, 199, event3, event4, event5, event6));
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(range, 100)
                .from(List.of(event1, event2, event3, event4, event5, event6))
                .build();
        // Then
        Assertions.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
    }

    @Test
    public void rangeLessThanNumTimeslots() {
        // Given
        Range range = Range.between(10, 20);
        AgentEvent event1 = createAgentEvent(10, AgentEventType.AGENT_PING);
        AgentEvent event2 = createAgentEvent(11, AgentEventType.AGENT_PING);
        AgentEvent event3 = createAgentEvent(12, AgentEventType.AGENT_PING);
        AgentEvent event4 = createAgentEvent(13, AgentEventType.AGENT_PING);
        AgentEvent event5 = createAgentEvent(14, AgentEventType.AGENT_PING);
        AgentEvent event6 = createAgentEvent(15, AgentEventType.AGENT_PING);
        AgentEvent event7 = createAgentEvent(16, AgentEventType.AGENT_PING);
        AgentEvent event8 = createAgentEvent(17, AgentEventType.AGENT_PING);
        AgentEvent event9 = createAgentEvent(18, AgentEventType.AGENT_PING);
        AgentEvent event10 = createAgentEvent(19, AgentEventType.AGENT_PING);
        List<AgentEventTimelineSegment> expectedTimelineSegments = List.of(
                createSegment(10, 11, event1),
                createSegment(11, 12, event2),
                createSegment(12, 13, event3),
                createSegment(13, 14, event4),
                createSegment(14, 15, event5),
                createSegment(15, 16, event6),
                createSegment(16, 17, event7),
                createSegment(17, 18, event8),
                createSegment(18, 19, event9),
                createSegment(19, 20, event10));
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(range, 100)
                .from(List.of(event1, event2, event3, event4, event5, event6, event7, event8, event9, event10))
                .build();
        // Then
        Assertions.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
    }

    @Test
    public void fullTimeslots_multipleEvents() {
        // Given
        long timeRangeMs = TimeUnit.DAYS.toMillis(7);
        long from = System.currentTimeMillis();
        long to = from + timeRangeMs;
        Range range = Range.between(from, to);
        int numTimeslots = 100;
        int expectedEventCountPerSegment = 20;
        List<AgentEvent> agentEvents = new ArrayList<>();
        for (int i = 0; i < timeRangeMs; i += (timeRangeMs / (numTimeslots * expectedEventCountPerSegment))) {
            agentEvents.add(createAgentEvent(from + i, AgentEventType.AGENT_PING));
        }
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(range, numTimeslots)
                .from(agentEvents)
                .build();
        // Then
        List<AgentEventTimelineSegment> timelineSegments = timeline.getTimelineSegments();
        assertThat(timelineSegments).hasSize(numTimeslots);
        for (AgentEventTimelineSegment timelineSegment : timelineSegments) {
            AgentEventMarker eventMarker = timelineSegment.getValue();

            Assertions.assertEquals(expectedEventCountPerSegment, eventMarker.getTotalCount());

            int pingEventCount = eventMarker.getTypeCounts().get(AgentEventType.AGENT_PING);
            Assertions.assertEquals(expectedEventCountPerSegment, pingEventCount);
        }
    }

    private AgentEventTimelineSegment createSegment(long startTimestamp, long endTimestamp, AgentEvent... agentEvent) {
        return createSegment(startTimestamp, endTimestamp, List.of(agentEvent));
    }

    private AgentEventTimelineSegment createSegment(long startTimestamp, long endTimestamp, List<AgentEvent> agentEvents) {
        AgentEventTimelineSegment segment = new AgentEventTimelineSegment();
        segment.setStartTimestamp(startTimestamp);
        segment.setEndTimestamp(endTimestamp);
        AgentEventMarker agentEventMarker = new AgentEventMarker();
        for (AgentEvent agentEvent : agentEvents) {
            agentEventMarker.addAgentEvent(agentEvent);
        }
        segment.setValue(agentEventMarker);
        return segment;
    }

    private AgentEvent createAgentEvent(long eventTimestamp, AgentEventType agentEventType) {
        return new AgentEvent(new AgentEventBo("testAgentId", 0, eventTimestamp, agentEventType));
    }
}
