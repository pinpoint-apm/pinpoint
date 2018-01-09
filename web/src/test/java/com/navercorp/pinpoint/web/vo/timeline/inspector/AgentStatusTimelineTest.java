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
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class AgentStatusTimelineTest {

    @Test
    public void nullAgentStatus() {
        // Given
        Range timelineRange = new Range(0, 100);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(0, 100, AgentState.UNKNOWN));
        // When
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, null).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void nullAgentStatus_nullAgentEvents() {
        // Given
        Range timelineRange = new Range(0, 100);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(0, 100, AgentState.UNKNOWN));
        // When
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, null).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void agentStatus() {
        // Given
        Range timelineRange = new Range(100, 200);
        AgentLifeCycleState expectedState = AgentLifeCycleState.RUNNING;
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, AgentState.fromAgentLifeCycleState(expectedState)));
        // When
        AgentStatus initialStatus = createAgentStatus(50, expectedState);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void agentStatus_nullAgentEvents() {
        // Given
        Range timelineRange = new Range(100, 200);
        AgentLifeCycleState expectedState = AgentLifeCycleState.RUNNING;
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, AgentState.fromAgentLifeCycleState(expectedState)));
        // When
        AgentStatus initialStatus = createAgentStatus(50, expectedState);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus, null).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void singleLifeCycle_startedBeforeTimelineStartTimestamp() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, AgentState.RUNNING));
        // When
        long agentA = 0;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 140, AgentEventType.AGENT_PING),
                        createAgentEvent(agentA, 190, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void singleLifeCycle_startedAfterTimelineStartTimestamp_initialStateRunning() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 150, AgentState.RUNNING),
                createSegment(150, 200, AgentState.RUNNING));
        // When
        long agentA = 150;
        AgentStatus initialStatus = createAgentStatus(50, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentA, 180, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertTrue(timeline.isIncludeWarning());
    }

    @Test
    public void singleLifeCycle_startedAfterTimelineStartTimestamp_initialStateShutdown() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 150, AgentState.SHUTDOWN),
                createSegment(150, 200, AgentState.RUNNING));
        // When
        long agentA = 150;
        AgentStatus initialStatus = createAgentStatus(50, AgentLifeCycleState.SHUTDOWN);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentA, 180, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void singleLifeCycle_endedBeforeTimelineEndTimestamp() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 180, AgentState.RUNNING),
                createSegment(180, 200, AgentState.SHUTDOWN));
        // When
        long agentA = 0;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 120, AgentEventType.AGENT_PING),
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_PING),
                        createAgentEvent(agentA, 180, AgentEventType.AGENT_SHUTDOWN)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void singleLifeCycle_disconnected() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, AgentState.RUNNING));
        // When
        long agentA = 0;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_CLOSED_BY_SERVER),
                        createAgentEvent(agentA, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentA, 180, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_disconnected() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 150, AgentState.RUNNING),
                createSegment(150, 160, AgentState.UNKNOWN),
                createSegment(160, 200, AgentState.RUNNING));
        // When
        long agentA = 0;
        long agentB = 160;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_CLOSED_BY_SERVER),
                        createAgentEvent(agentB, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 180, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_noOverlap() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 140, AgentState.RUNNING),
                createSegment(140, 160, AgentState.SHUTDOWN),
                createSegment(160, 200, AgentState.RUNNING));
        // When
        long agentA = 0;
        long agentB = 160;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 140, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN),
                        createAgentEvent(agentB, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 180, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_noOverlap2() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 159, AgentState.RUNNING),
                createSegment(159, 160, AgentState.SHUTDOWN),
                createSegment(160, 200, AgentState.RUNNING));
        // When
        long agentA = 0;
        long agentB = 160;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 159, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN),
                        createAgentEvent(agentB, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 180, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_noOverlap3() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 120, AgentState.SHUTDOWN),
                createSegment(120, 140, AgentState.RUNNING),
                createSegment(140, 160, AgentState.SHUTDOWN),
                createSegment(160, 180, AgentState.RUNNING),
                createSegment(180, 200, AgentState.SHUTDOWN));
        // When
        long agentA = 120;
        long agentB = 160;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.SHUTDOWN);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 120, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentA, 140, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN),
                        createAgentEvent(agentB, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 180, AgentEventType.AGENT_SHUTDOWN)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertFalse(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_overlap() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 180, AgentState.RUNNING),
                createSegment(180, 200, AgentState.SHUTDOWN));
        // When
        long agentA = 0;
        long agentB = 120;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentB, 120, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentA, 140, AgentEventType.AGENT_PING),
                        createAgentEvent(agentB, 160, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN),
                        createAgentEvent(agentA, 180, AgentEventType.AGENT_SHUTDOWN)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertTrue(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_overlap2() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, AgentState.RUNNING));
        // When
        long agentA = 0;
        long agentB = 160;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 160, AgentEventType.AGENT_UNEXPECTED_SHUTDOWN),
                        createAgentEvent(agentB, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 180, AgentEventType.AGENT_PING)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertTrue(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_overlap3() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, AgentState.RUNNING));
        // When
        long agentA = 80;
        long agentB = 90;
        long agentC = 110;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 120, AgentEventType.AGENT_PING),
                        createAgentEvent(agentB, 130, AgentEventType.AGENT_PING),
                        createAgentEvent(agentC, 140, AgentEventType.AGENT_PING),
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_PING),
                        createAgentEvent(agentB, 170, AgentEventType.AGENT_SHUTDOWN),
                        createAgentEvent(agentA, 180, AgentEventType.AGENT_PING),
                        createAgentEvent(agentC, 190, AgentEventType.AGENT_SHUTDOWN)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertTrue(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_overlap4() {
        // Given
        Range timelineRange = new Range(100, 200);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Collections.singletonList(
                createSegment(100, 200, AgentState.RUNNING));
        // When
        long agentA = 90;
        long agentB = 130;
        long agentC = 160;
        long agentD = 180;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 120, AgentEventType.AGENT_PING),
                        createAgentEvent(agentB, 130, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_SHUTDOWN),
                        createAgentEvent(agentC, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 170, AgentEventType.AGENT_SHUTDOWN),
                        createAgentEvent(agentD, 180, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentC, 190, AgentEventType.AGENT_SHUTDOWN)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertTrue(timeline.isIncludeWarning());
    }

    @Test
    public void multipleLifeCycles_mixed() {
        // Given
        Range timelineRange = new Range(100, 300);
        List<AgentStatusTimelineSegment> expectedTimelineSegments = Arrays.asList(
                createSegment(100, 150, AgentState.RUNNING),
                createSegment(150, 160, AgentState.UNKNOWN),
                createSegment(160, 250, AgentState.RUNNING),
                createSegment(250, 260, AgentState.SHUTDOWN),
                createSegment(260, 290, AgentState.RUNNING),
                createSegment(290, 300, AgentState.UNKNOWN));
        // When
        long agentA = 90;
        long agentB = 160;
        long agentC = 220;
        long agentD = 260;
        AgentStatus initialStatus = createAgentStatus(90, AgentLifeCycleState.RUNNING);
        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, initialStatus,
                Arrays.asList(
                        createAgentEvent(agentA, 120, AgentEventType.AGENT_PING),
                        createAgentEvent(agentA, 150, AgentEventType.AGENT_UNEXPECTED_CLOSE_BY_SERVER),
                        createAgentEvent(agentB, 160, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 200, AgentEventType.AGENT_PING),
                        createAgentEvent(agentC, 220, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentB, 220, AgentEventType.AGENT_CLOSED_BY_SERVER),
                        createAgentEvent(agentC, 250, AgentEventType.AGENT_SHUTDOWN),
                        createAgentEvent(agentD, 260, AgentEventType.AGENT_CONNECTED),
                        createAgentEvent(agentD, 290, AgentEventType.AGENT_CLOSED_BY_SERVER)
                )).build();
        // Then
        Assert.assertEquals(expectedTimelineSegments, timeline.getTimelineSegments());
        Assert.assertTrue(timeline.isIncludeWarning());
    }

    private AgentStatus createAgentStatus(long timestamp, AgentLifeCycleState state) {
        AgentStatus agentStatus = new AgentStatus("testAgent");
        agentStatus.setEventTimestamp(timestamp);
        agentStatus.setState(state);
        return agentStatus;
    }

    private AgentEvent createAgentEvent(long agentStartTimestamp, long timestamp, AgentEventType agentEventType) {
        return new AgentEvent(new AgentEventBo("testAgentId", agentStartTimestamp, timestamp, agentEventType));
    }

    private AgentStatusTimelineSegment createSegment(long startTimestamp, long endTimestamp, AgentState state) {
        AgentStatusTimelineSegment segment = new AgentStatusTimelineSegment();
        segment.setStartTimestamp(startTimestamp);
        segment.setEndTimestamp(endTimestamp);
        segment.setValue(state);
        return segment;
    }
}
