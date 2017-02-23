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

import com.navercorp.pinpoint.common.server.bo.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.web.filter.agent.AgentEventFilter;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
public class AgentEventTimelineTest {

    @Test
    public void noFilter() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentEvent> agentEvents = Arrays.asList(
                createAgentEvent(0, 140, AgentEventType.AGENT_PING),
                createAgentEvent(0, 190, AgentEventType.AGENT_PING));
        List<AgentEventTimeline.Segment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, agentEvents));
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(timelineRange)
                .from(agentEvents)
                .build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
    }

    @Test
    public void nullFilter() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentEvent> agentEvents = Arrays.asList(
                createAgentEvent(0, 140, AgentEventType.AGENT_PING),
                createAgentEvent(0, 190, AgentEventType.AGENT_PING));
        List<AgentEventTimeline.Segment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, agentEvents));
        // When
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(timelineRange)
                .from(agentEvents)
                .addFilter(null)
                .build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
    }

    @Test
    public void multipleFilters() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentEvent> agentEvents = Arrays.asList(
                createAgentEvent(0, 110, AgentEventType.AGENT_PING),
                createAgentEvent(0, 120, AgentEventType.AGENT_CONNECTED),
                createAgentEvent(0, 130, AgentEventType.AGENT_SHUTDOWN),
                createAgentEvent(0, 140, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN),
                createAgentEvent(0, 150, AgentEventType.AGENT_CLOSED_BY_SERVER),
                createAgentEvent(0, 160, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER),
                createAgentEvent(0, 170, AgentEventType.USER_THREAD_DUMP),
                createAgentEvent(0, 180, AgentEventType.OTHER));
        Set<AgentEventType> includedAgentEventTypes = new HashSet<AgentEventType>() {{
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
        AgentEventTimeline timeline = new AgentEventTimelineBuilder(timelineRange)
                .from(agentEvents)
                .addFilter(excludeUnexpectedEventsFilter)
                .addFilter(excludeUserThreadDumpFilter)
                .addFilter(excludeOtherFilter)
                .build();
        // Then
        List<AgentEvent> timelineEvents = new ArrayList<>();
        List<AgentEventTimeline.Segment> segments = timeline.getTimelineSegments();
        for (AgentEventTimeline.Segment segment : segments) {
            timelineEvents.addAll(segment.getValue());
        }
        for (AgentEvent timelineEvent : timelineEvents) {
            AgentEventType timelineEventType = AgentEventType.getTypeByCode(timelineEvent.getEventTypeCode());
            Assert.assertTrue(includedAgentEventTypes.contains(timelineEventType));
            Assert.assertTrue(timelineEventType != AgentEventType.AGENT_UNEXPECTED_SHUTDOWN);
            Assert.assertTrue(timelineEventType != AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER);
            Assert.assertTrue(timelineEventType != AgentEventType.USER_THREAD_DUMP);
            Assert.assertTrue(timelineEventType != AgentEventType.OTHER);
        }
    }

    private AgentEventTimeline.Segment createSegment(long startTimestamp, long endTimestamp, List<AgentEvent> agentEvents) {
        AgentEventTimeline.Segment segment = new AgentEventTimeline.Segment();
        segment.setStartTimestamp(startTimestamp);
        segment.setEndTimestamp(endTimestamp);
        segment.setValue(agentEvents);
        return segment;
    }

    private AgentEvent createAgentEvent(long agentStartTimestamp, long timestamp, AgentEventType agentEventType) {
        return new AgentEvent(new AgentEventBo("testAgentId", agentStartTimestamp, timestamp, agentEventType));
    }
}
