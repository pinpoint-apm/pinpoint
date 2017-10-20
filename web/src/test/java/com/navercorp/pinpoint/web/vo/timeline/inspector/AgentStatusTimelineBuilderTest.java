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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Taejin Koo
 */
public class AgentStatusTimelineBuilderTest {

    private static final long FROM = 10000;
    private static final long TO = 20000;
    private static final long DIFF = TO - FROM;

    private static final AgentStatus DEFAULT_STATUS;
    static {
        DEFAULT_STATUS = createAgentStatus(0, AgentLifeCycleState.RUNNING);
    }

    @Test
    public void defaultTest1() {
        // Given
        Range timelineRange = new Range(FROM, TO);

        // When
        long startTime = getRandomLong(FROM + 1, FROM + (DIFF / 2));
        long endTime = getRandomLong(startTime, TO - 1);
        AgentStatusTimelineSegment timelineSegment = createTimelineSegment(startTime, endTime);

        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, DEFAULT_STATUS, null, Arrays.asList(timelineSegment)).build();

        // Then
        List<AgentStatusTimelineSegment> actualTimelineSegmentList = timeline.getTimelineSegments();
        Assert.assertTrue(actualTimelineSegmentList.size() == 3);

        AgentStatusTimelineSegment first = actualTimelineSegmentList.get(0);
        assertTimelineSegment(first, FROM, startTime, AgentState.RUNNING);

        AgentStatusTimelineSegment unstableTimelineSegment = actualTimelineSegmentList.get(1);
        assertTimelineSegment(unstableTimelineSegment, startTime, endTime, AgentState.UNSTABLE_RUNNING);

        AgentStatusTimelineSegment last = actualTimelineSegmentList.get(2);
        assertTimelineSegment(last, endTime, TO, AgentState.RUNNING);
    }

    @Test
    public void defaultTest2() {
        // Given
        Range timelineRange = new Range(FROM, TO);

        // When
        long firstStartTime = ThreadLocalRandom.current().nextLong(FROM + 1, FROM + (DIFF / 4));
        long firstEndTime = ThreadLocalRandom.current().nextLong(firstStartTime + 1, FROM + (DIFF / 2));
        AgentStatusTimelineSegment timelineSegment1 = createTimelineSegment(firstStartTime, firstEndTime);

        long secondStartTime = ThreadLocalRandom.current().nextLong(firstEndTime + 1, TO - (DIFF / 4));
        long secondEndTime = ThreadLocalRandom.current().nextLong(secondStartTime + 1, TO - 1);
        AgentStatusTimelineSegment timelineSegment2 = createTimelineSegment(secondStartTime, secondEndTime);

        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, DEFAULT_STATUS, null, Arrays.asList(timelineSegment1, timelineSegment2)).build();

        // Then
        List<AgentStatusTimelineSegment> actualTimelineSegmentList = timeline.getTimelineSegments();
        Assert.assertTrue(actualTimelineSegmentList.size() == 5);

        AgentStatusTimelineSegment first = actualTimelineSegmentList.get(0);
        assertTimelineSegment(first, FROM, firstStartTime, AgentState.RUNNING);

        AgentStatusTimelineSegment unstableTimelineSegment1 = actualTimelineSegmentList.get(1);
        assertTimelineSegment(unstableTimelineSegment1, firstStartTime, firstEndTime, AgentState.UNSTABLE_RUNNING);

        AgentStatusTimelineSegment middle = actualTimelineSegmentList.get(2);
        assertTimelineSegment(middle, firstEndTime, secondStartTime, AgentState.RUNNING);

        AgentStatusTimelineSegment unstableTimelineSegment2 = actualTimelineSegmentList.get(3);
        assertTimelineSegment(unstableTimelineSegment2, secondStartTime, secondEndTime, AgentState.UNSTABLE_RUNNING);

        AgentStatusTimelineSegment last = actualTimelineSegmentList.get(4);
        assertTimelineSegment(last, secondEndTime, TO, AgentState.RUNNING);
    }

    @Test
    public void boundaryValueTest1() {
        // Given
        Range timelineRange = new Range(FROM, TO);

        // When
        long endTime = getRandomLong(FROM + 1, TO - 1);
        AgentStatusTimelineSegment timelineSegment = createTimelineSegment(FROM, endTime);

        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, DEFAULT_STATUS, null, Arrays.asList(timelineSegment)).build();

        // Then
        List<AgentStatusTimelineSegment> actualTimelineSegmentList = timeline.getTimelineSegments();
        Assert.assertTrue(actualTimelineSegmentList.size() == 2);

        AgentStatusTimelineSegment unstableTimelineSegment = actualTimelineSegmentList.get(0);
        assertTimelineSegment(unstableTimelineSegment, FROM, endTime, AgentState.UNSTABLE_RUNNING);

        AgentStatusTimelineSegment last = actualTimelineSegmentList.get(1);
        assertTimelineSegment(last, endTime, TO, AgentState.RUNNING);
    }

    @Test
    public void boundaryValueTest2() {
        // Given
        Range timelineRange = new Range(FROM, TO);

        // When
        long startTime = ThreadLocalRandom.current().nextLong(FROM + 1, FROM + (DIFF / 2));
        AgentStatusTimelineSegment timelineSegment = createTimelineSegment(startTime, TO);

        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, DEFAULT_STATUS, null, Arrays.asList(timelineSegment)).build();

        // Then
        List<AgentStatusTimelineSegment> actualTimelineSegmentList = timeline.getTimelineSegments();
        Assert.assertTrue(actualTimelineSegmentList.size() == 2);

        AgentStatusTimelineSegment first = actualTimelineSegmentList.get(0);
        assertTimelineSegment(first, FROM, startTime, AgentState.RUNNING);

        AgentStatusTimelineSegment unstableTimelineSegment = actualTimelineSegmentList.get(1);
        assertTimelineSegment(unstableTimelineSegment, startTime, TO, AgentState.UNSTABLE_RUNNING);
    }

    @Test
    public void overBoundaryValueTest1() {
        // Given
        Range timelineRange = new Range(FROM, TO);

        // When
        long warningEndTime = ThreadLocalRandom.current().nextLong(FROM, TO);

        AgentStatusTimelineSegment timelineSegment = createTimelineSegment(FROM - 1, warningEndTime);

        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, DEFAULT_STATUS, null, Arrays.asList(timelineSegment)).build();

        // Then
        List<AgentStatusTimelineSegment> actualTimelineSegmentList = timeline.getTimelineSegments();
        Assert.assertTrue(actualTimelineSegmentList.size() == 1);

        AgentStatusTimelineSegment timelineSegment1 = actualTimelineSegmentList.get(0);
        assertTimelineSegment(timelineSegment1, FROM, TO, AgentState.RUNNING);
    }

    @Test
    public void overBoundaryValueTest2() {
        // Given
        Range timelineRange = new Range(FROM, TO);

        // When
        long warningStartTime = ThreadLocalRandom.current().nextLong(FROM, TO);
        AgentStatusTimelineSegment timelineSegment = createTimelineSegment(warningStartTime, TO + 1);

        AgentStatusTimeline timeline = new AgentStatusTimelineBuilder(timelineRange, DEFAULT_STATUS, null, Arrays.asList(timelineSegment)).build();

        // Then
        List<AgentStatusTimelineSegment> actualTimelineSegmentList = timeline.getTimelineSegments();
        Assert.assertTrue(actualTimelineSegmentList.size() == 1);

        AgentStatusTimelineSegment timelineSegment1 = actualTimelineSegmentList.get(0);
        assertTimelineSegment(timelineSegment1, FROM, TO, AgentState.RUNNING);
    }

    private void assertTimelineSegment(AgentStatusTimelineSegment timelineSegment, long startTime, long endTime, AgentState state) {
        Assert.assertEquals(startTime, timelineSegment.getStartTimestamp());
        Assert.assertEquals(endTime, timelineSegment.getEndTimestamp());
        Assert.assertEquals(state, timelineSegment.getValue());
    }

    private AgentStatusTimelineSegment createTimelineSegment(long startTimestamp, long endTimestamp) {
        AgentStatusTimelineSegment segment = new AgentStatusTimelineSegment();
        segment.setStartTimestamp(startTimestamp);
        segment.setEndTimestamp(endTimestamp);
        segment.setValue(AgentState.UNSTABLE_RUNNING);

        return segment;
    }

    private static AgentStatus createAgentStatus(long timestamp, AgentLifeCycleState state) {
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

    private long getRandomLong(long original, long bound) {
        return ThreadLocalRandom.current().nextLong(original, bound);
    }

}
