/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.web.service.stat.AgentWarningStatService;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentState;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author minwoo-jung
 */
@ExtendWith(MockitoExtension.class)
class AgentWarningStatServiceImplTest {

    private static final long CURRENT_TIME = System.currentTimeMillis();
    private static final long START_TIME = CURRENT_TIME - TimeUnit.DAYS.toMillis(1);
    private static final long TIME_5_MIN = 60000 * 5;

    @Mock
    private AgentStatService agentStatService;

    @Mock
    TenantProvider tenantProvider;

    private AgentWarningStatService agentWarningStatService;

    @BeforeEach
    public void setUp() throws Exception {
        this.agentWarningStatService = new AgentWarningStatServiceImpl(agentStatService, tenantProvider);
    }

    @Test
    public void selectTest1() {
        long from = START_TIME;
        long to = CURRENT_TIME;
        Range range = Range.between(from, to);

        List<SystemMetricPoint<Double>> mockDataList = new ArrayList<>(9);

        long firstStartTimestamp = from;
        long firstEndTimestamp = firstStartTimestamp + 10000;
        mockDataList.add(new SystemMetricPoint<Double>(firstStartTimestamp , 22.0));
        mockDataList.add(new SystemMetricPoint<Double>(firstStartTimestamp + 5000, 13.0));
        mockDataList.add(new SystemMetricPoint<Double>(firstEndTimestamp, 14.0));

        long secondStartTimestamp = from + TIME_5_MIN;
        long secondEndTimestamp = secondStartTimestamp;
        mockDataList.add(new SystemMetricPoint<Double>(secondStartTimestamp, 15.0));
        mockDataList.add(new SystemMetricPoint<Double>(secondStartTimestamp + 5000, 112.0));
        mockDataList.add(new SystemMetricPoint<Double>(secondEndTimestamp, 11.0));

        long thirdStartTimestamp = from + TIME_5_MIN + TIME_5_MIN;
        long thirdEndTimestamp = thirdStartTimestamp + 10000;
        mockDataList.add(new SystemMetricPoint<Double>(thirdStartTimestamp, 15.0));
        mockDataList.add(new SystemMetricPoint<Double>(thirdStartTimestamp + 5000, 112.0));
        mockDataList.add(new SystemMetricPoint<Double>(thirdEndTimestamp, 11.0));

        when(agentStatService.selectAgentStatUnconvertedTime(any(InspectorDataSearchKey.class), any(TimeWindow.class))).thenReturn(mockDataList);
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");

        List<AgentStatusTimelineSegment> timelineSegmentList = agentWarningStatService.select("applicationName", "pinpoint", range);
        assertThat(timelineSegmentList).hasSize(3);

        validateAgentStatusTimelineSegment(timelineSegmentList.get(0), AgentState.UNSTABLE_RUNNING, firstStartTimestamp, firstEndTimestamp);
        validateAgentStatusTimelineSegment(timelineSegmentList.get(1), AgentState.UNSTABLE_RUNNING, secondStartTimestamp, secondEndTimestamp);
        validateAgentStatusTimelineSegment(timelineSegmentList.get(2), AgentState.UNSTABLE_RUNNING, thirdStartTimestamp, thirdEndTimestamp);
    }

    private void validateAgentStatusTimelineSegment(AgentStatusTimelineSegment segment, AgentState expectedState, long expectedStartTimestamp, long expectedEndTimestamp) {
        assertEquals(segment.getValue(), expectedState);
        assertEquals(segment.getStartTimestamp(), expectedStartTimestamp);
        assertEquals(segment.getEndTimestamp(), expectedEndTimestamp);
    }

    @Test
    public void selectTest2() {
        long from = START_TIME;
        long to = CURRENT_TIME;
        Range range = Range.between(from, to);

        List<SystemMetricPoint<Double>> mockDataList = new ArrayList<>(10);
        mockDataList.add(new SystemMetricPoint<Double>(from , 22.0));
        mockDataList.add(new SystemMetricPoint<Double>(from + 5000, 13.0));
        mockDataList.add(new SystemMetricPoint<Double>(from + 10000, 14.0));
        mockDataList.add(new SystemMetricPoint<Double>(from + 15000, 14.0));
        mockDataList.add(new SystemMetricPoint<Double>(from + 20000, 14.0));
        mockDataList.add(new SystemMetricPoint<Double>(from + 25000, 14.0));

        when(agentStatService.selectAgentStatUnconvertedTime(any(InspectorDataSearchKey.class), any(TimeWindow.class))).thenReturn(mockDataList);
        when(tenantProvider.getTenantId()).thenReturn("pinpoint");

        List<AgentStatusTimelineSegment> timelineSegmentList = agentWarningStatService.select("applicationName", "pinpoint", range);

        assertThat(timelineSegmentList).hasSize(1);
        validateAgentStatusTimelineSegment(timelineSegmentList.get(0), AgentState.UNSTABLE_RUNNING, from, from + 25000);
    }

}