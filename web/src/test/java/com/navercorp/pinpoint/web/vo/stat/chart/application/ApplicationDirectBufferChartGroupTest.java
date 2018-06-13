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
        Range range = new Range(time - 240000, time);
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
            testDirectCount((DirectBufferPoint)point, aggreDirectBufferList.get(--index));
        }

        Chart directMemoryUsedChart = charts.get(ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup.DirectBufferChartType.DIRECT_MEMORY_USED);
        List<Point> directMemoryUsedPoints = directMemoryUsedChart.getPoints();
        assertEquals(5, directMemoryUsedPoints.size());
        index = directMemoryUsedPoints.size();
        for (Point point : directMemoryUsedPoints) {
            testDirectMemoryUsed((DirectBufferPoint)point, aggreDirectBufferList.get(--index));
        }

        Chart mappedCountChart = charts.get(ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup.DirectBufferChartType.MAPPED_COUNT);
        List<Point> mappeedCountPoints = mappedCountChart.getPoints();
        assertEquals(5, mappeedCountPoints.size());
        index = mappeedCountPoints.size();
        for (Point point : mappeedCountPoints) {
            testMappedCount((DirectBufferPoint)point, aggreDirectBufferList.get(--index));
        }

        Chart mappedMemoryUsedChart = charts.get(ApplicationDirectBufferChart.ApplicationDirectBufferChartGroup.DirectBufferChartType.MAPPED_MEMORY_USED);
        List<Point> mappedMemoryUsedPoints = mappedMemoryUsedChart.getPoints();
        assertEquals(5, mappedMemoryUsedPoints.size());
        index = mappedMemoryUsedPoints.size();
        for (Point point : mappedMemoryUsedPoints) {
            testMappedMemoryUsed((DirectBufferPoint)point, aggreDirectBufferList.get(--index));
        }
    }

    private void testDirectCount(DirectBufferPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), aggreJoinDirectBufferBo.getAvgDirectCount(), 0);
        assertEquals(directBufferPoint.getYValForMin(), aggreJoinDirectBufferBo.getMinDirectCount(), 0);
        assertEquals(directBufferPoint.getYValForMax(), aggreJoinDirectBufferBo.getMaxDirectCount(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), aggreJoinDirectBufferBo.getMinDirectCountAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), aggreJoinDirectBufferBo.getMaxDirectCountAgentId());
    }

    private void testDirectMemoryUsed(DirectBufferPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), aggreJoinDirectBufferBo.getAvgDirectMemoryUsed(), 0);
        assertEquals(directBufferPoint.getYValForMin(), aggreJoinDirectBufferBo.getMinDirectMemoryUsed(), 0);
        assertEquals(directBufferPoint.getYValForMax(), aggreJoinDirectBufferBo.getMaxDirectMemoryUsed(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), aggreJoinDirectBufferBo.getMinDirectMemoryUsedAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), aggreJoinDirectBufferBo.getMaxDirectMemoryUsedAgentId());
    }

    private void testMappedCount(DirectBufferPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), aggreJoinDirectBufferBo.getAvgMappedCount(), 0);
        assertEquals(directBufferPoint.getYValForMin(), aggreJoinDirectBufferBo.getMinMappedCount(), 0);
        assertEquals(directBufferPoint.getYValForMax(), aggreJoinDirectBufferBo.getMaxMappedCount(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), aggreJoinDirectBufferBo.getMinMappedCountAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), aggreJoinDirectBufferBo.getMaxMappedCountAgentId());
    }

    private void testMappedMemoryUsed(DirectBufferPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        assertEquals(directBufferPoint.getXVal(), aggreJoinDirectBufferBo.getTimestamp());
        assertEquals(directBufferPoint.getYValForAvg(), aggreJoinDirectBufferBo.getAvgMappedMemoryUsed(), 0);
        assertEquals(directBufferPoint.getYValForMin(), aggreJoinDirectBufferBo.getMinMappedMemoryUsed(), 0);
        assertEquals(directBufferPoint.getYValForMax(), aggreJoinDirectBufferBo.getMaxMappedMemoryUsed(), 0);
        assertEquals(directBufferPoint.getAgentIdForMin(), aggreJoinDirectBufferBo.getMinMappedMemoryUsedAgentId());
        assertEquals(directBufferPoint.getAgentIdForMax(), aggreJoinDirectBufferBo.getMaxMappedMemoryUsedAgentId());
    }
}