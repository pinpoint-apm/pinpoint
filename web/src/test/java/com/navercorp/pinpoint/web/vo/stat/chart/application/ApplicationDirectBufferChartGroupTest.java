/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Roy Kim
 */
public class ApplicationDirectBufferChartGroupTest {

    @Test
    public void createApplicationDirectBufferChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.newRange(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        List<AggreJoinDirectBufferBo> aggreDirectBufferList = new ArrayList<>(5);
        AggreJoinDirectBufferBo aggreJoinDirectBufferBo1 = new AggreJoinDirectBufferBo("testApp", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", time);
        AggreJoinDirectBufferBo aggreJoinDirectBufferBo2 = new AggreJoinDirectBufferBo("testApp", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2", time - 60000);
        AggreJoinDirectBufferBo aggreJoinDirectBufferBo3 = new AggreJoinDirectBufferBo("testApp", 33, 39, "agent3_1", 9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2", time - 120000);
        AggreJoinDirectBufferBo aggreJoinDirectBufferBo4 = new AggreJoinDirectBufferBo("testApp", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", time - 180000);
        AggreJoinDirectBufferBo aggreJoinDirectBufferBo5 = new AggreJoinDirectBufferBo("testApp", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", time - 240000);
        aggreDirectBufferList.add(aggreJoinDirectBufferBo1);
        aggreDirectBufferList.add(aggreJoinDirectBufferBo2);
        aggreDirectBufferList.add(aggreJoinDirectBufferBo3);
        aggreDirectBufferList.add(aggreJoinDirectBufferBo4);
        aggreDirectBufferList.add(aggreJoinDirectBufferBo5);

        StatChartGroup applicationDirectBufferChartGroup = new ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup(timeWindow, aggreDirectBufferList);
        Map<StatChartGroup.ChartType, Chart<? extends Point>> charts = applicationDirectBufferChartGroup.getCharts();
        assertEquals(4, charts.size());

        Chart directCountChart = charts.get(ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup.DirectBufferChartType.DIRECT_COUNT);
        List<Point> directCountPoints = directCountChart.getPoints();
        assertEquals(5, directCountPoints.size());
        int index = directCountPoints.size();
        for (Point point : directCountPoints) {
            testDirectCount((LongApplicationStatPoint) point, aggreDirectBufferList.get(--index));
        }

        Chart directMemoryUsedChart = charts.get(ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup.DirectBufferChartType.DIRECT_MEMORY_USED);
        List<Point> directMemoryUsedPoints = directMemoryUsedChart.getPoints();
        assertEquals(5, directMemoryUsedPoints.size());
        index = directMemoryUsedPoints.size();
        for (Point point : directMemoryUsedPoints) {
            testDirectMemoryUsed((LongApplicationStatPoint)point, aggreDirectBufferList.get(--index));
        }

        Chart mappedCountChart = charts.get(ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup.DirectBufferChartType.MAPPED_COUNT);
        List<Point> mappeedCountPoints = mappedCountChart.getPoints();
        assertEquals(5, mappeedCountPoints.size());
        index = mappeedCountPoints.size();
        for (Point point : mappeedCountPoints) {
            testMappedCount((LongApplicationStatPoint)point, aggreDirectBufferList.get(--index));
        }

        Chart mappedMemoryUsedChart = charts.get(ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup.DirectBufferChartType.MAPPED_MEMORY_USED);
        List<Point> mappedMemoryUsedPoints = mappedMemoryUsedChart.getPoints();
        assertEquals(5, mappedMemoryUsedPoints.size());
        index = mappedMemoryUsedPoints.size();
        for (Point point : mappedMemoryUsedPoints) {
            testMappedMemoryUsed((LongApplicationStatPoint)point, aggreDirectBufferList.get(--index));
        }
    }

    private void testDirectCount(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo directCountJoinValue = aggreJoinDirectBufferBo.getDirectCountJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), directCountJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), directCountJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), directCountJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), directCountJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), directCountJoinValue.getMaxAgentId());
    }

    private void testDirectMemoryUsed(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo directMemoryUsedJoinValue = aggreJoinDirectBufferBo.getDirectMemoryUsedJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), directMemoryUsedJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), directMemoryUsedJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), directMemoryUsedJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), directMemoryUsedJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), directMemoryUsedJoinValue.getMaxAgentId());
    }

    private void testMappedCount(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo mappedCountJoinValue = aggreJoinDirectBufferBo.getMappedCountJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), mappedCountJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), mappedCountJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), mappedCountJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), mappedCountJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), mappedCountJoinValue.getMaxAgentId());
    }

    private void testMappedMemoryUsed(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo mappedMemoryUsedJoinValue = aggreJoinDirectBufferBo.getMappedMemoryUsedJoinValue();
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), mappedMemoryUsedJoinValue.getAvg(), 0);
        assertEquals(directBufferPoint.getYValForMin(), mappedMemoryUsedJoinValue.getMin(), 0);
        assertEquals(directBufferPoint.getYValForMax(), mappedMemoryUsedJoinValue.getMax(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), mappedMemoryUsedJoinValue.getMinAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), mappedMemoryUsedJoinValue.getMaxAgentId());
    }
}