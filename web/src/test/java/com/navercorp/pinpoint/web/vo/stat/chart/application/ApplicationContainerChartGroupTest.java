/*
 * Copyright 2020 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDoubleFieldBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinContainerBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;


/**
 * @author Hyunjoon Cho
 */
public class ApplicationContainerChartGroupTest {
    @Test
    public void createApplicationContainerChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.newRange(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        List<AggreJoinContainerBo> aggreJoinContainerList = new ArrayList<>(5);
        AggreJoinContainerBo aggreJoinContainerBo1 = new AggreJoinContainerBo("testApp", 0.11, 0.60, "agent1_1", 0.20, "agent1_2", 0.11, 0.60, "agent1_1", 0.20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", 11, 60, "agent1_1", 20, "agent1_2", time);
        AggreJoinContainerBo aggreJoinContainerBo2 = new AggreJoinContainerBo("testApp", 0.22, 0.52, "agent2_1", 0.10, "agent2_2", 0.22, 0.52, "agent2_1", 0.10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2", 22, 52, "agent2_1", 10, "agent2_2",time - 60000);
        AggreJoinContainerBo aggreJoinContainerBo3 = new AggreJoinContainerBo("testApp", 0.33, 0.39, "agent3_1", 0.9, "agent3_2", 0.33, 0.39, "agent3_1", 0.9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2", 33, 39, "agent3_1", 9, "agent3_2",time - 120000);
        AggreJoinContainerBo aggreJoinContainerBo4 = new AggreJoinContainerBo("testApp", 0.44, 0.42, "agent4_1", 0.25, "agent4_2", 0.44, 0.42, "agent4_1", 0.25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2", 44, 42, "agent4_1", 25, "agent4_2",time - 180000);
        AggreJoinContainerBo aggreJoinContainerBo5 = new AggreJoinContainerBo("testApp", 0.55, 0.55, "agent5_1", 0.54, "agent5_2", 0.55, 0.55, "agent5_1", 0.54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2", 55, 55, "agent5_1", 54, "agent5_2",time - 240000);
        aggreJoinContainerList.add(aggreJoinContainerBo1);
        aggreJoinContainerList.add(aggreJoinContainerBo2);
        aggreJoinContainerList.add(aggreJoinContainerBo3);
        aggreJoinContainerList.add(aggreJoinContainerBo4);
        aggreJoinContainerList.add(aggreJoinContainerBo5);

        StatChartGroup applicationContainerChartGroup = new ApplicationContainerChart.ApplicationContainerChartGroup(timeWindow, aggreJoinContainerList);
        Map<StatChartGroup.ChartType, Chart<? extends Point>> charts = applicationContainerChartGroup.getCharts();
        assertEquals(4, charts.size());

        Chart userCpuUsageChart = charts.get(ApplicationContainerChart.ApplicationContainerChartGroup.ContainerChartType.USER_CPU_USAGE);
        List<Point> userCpuUsagePoints = userCpuUsageChart.getPoints();
        assertEquals(5, userCpuUsagePoints.size());
        int index = userCpuUsagePoints.size();
        for (Point point : userCpuUsagePoints) {
            testUserCpuUsage((DoubleApplicationStatPoint) point, aggreJoinContainerList.get(--index));
        }

        Chart systemCpuUsageChart = charts.get(ApplicationContainerChart.ApplicationContainerChartGroup.ContainerChartType.SYSTEM_CPU_USAGE);
        List<Point> systemCpuUsagePoints = systemCpuUsageChart.getPoints();
        assertEquals(5, systemCpuUsagePoints.size());
        index = systemCpuUsagePoints.size();
        for (Point point : systemCpuUsagePoints) {
            testSystemCpuUsage((DoubleApplicationStatPoint)point, aggreJoinContainerList.get(--index));
        }

        Chart memoryMaxChart = charts.get(ApplicationContainerChart.ApplicationContainerChartGroup.ContainerChartType.MEMORY_MAX);
        List<Point> memoryMaxPoints = memoryMaxChart.getPoints();
        assertEquals(5, memoryMaxPoints.size());
        index = memoryMaxPoints.size();
        for (Point point : memoryMaxPoints) {
            testMemoryMax((LongApplicationStatPoint)point, aggreJoinContainerList.get(--index));
        }

        Chart memoryUsageChart = charts.get(ApplicationContainerChart.ApplicationContainerChartGroup.ContainerChartType.MEMORY_USAGE);
        List<Point> memoryUsagePoints = memoryUsageChart.getPoints();
        assertEquals(5, memoryUsagePoints.size());
        index = memoryUsagePoints.size();
        for (Point point : memoryUsagePoints) {
            testMemoryUsage((LongApplicationStatPoint)point, aggreJoinContainerList.get(--index));
        }
    }

    private void testUserCpuUsage(DoubleApplicationStatPoint containerPoint, AggreJoinContainerBo aggreJoinContainerBo) {
        final JoinDoubleFieldBo userCpuUsageJoinValue = aggreJoinContainerBo.getUserCpuUsageJoinValue();
        assertEquals(containerPoint.getXVal(), aggreJoinContainerBo.getTimestamp());
        assertEquals(containerPoint.getYValForAvg(), userCpuUsageJoinValue.getAvg(), 0);
        assertEquals(containerPoint.getYValForMin(), userCpuUsageJoinValue.getMin(), 0);
        assertEquals(containerPoint.getYValForMax(), userCpuUsageJoinValue.getMax(), 0);
        assertEquals(containerPoint.getAgentIdForMin(), userCpuUsageJoinValue.getMinAgentId());
        assertEquals(containerPoint.getAgentIdForMax(), userCpuUsageJoinValue.getMaxAgentId());
    }

    private void testSystemCpuUsage(DoubleApplicationStatPoint containerPoint, AggreJoinContainerBo aggreJoinContainerBo) {
        final JoinDoubleFieldBo systemCpuUsageJoinValue = aggreJoinContainerBo.getSystemCpuUsageJoinValue();
        assertEquals(containerPoint.getXVal(), aggreJoinContainerBo.getTimestamp());
        assertEquals(containerPoint.getYValForAvg(), systemCpuUsageJoinValue.getAvg(), 0);
        assertEquals(containerPoint.getYValForMin(), systemCpuUsageJoinValue.getMin(), 0);
        assertEquals(containerPoint.getYValForMax(), systemCpuUsageJoinValue.getMax(), 0);
        assertEquals(containerPoint.getAgentIdForMin(), systemCpuUsageJoinValue.getMinAgentId());
        assertEquals(containerPoint.getAgentIdForMax(), systemCpuUsageJoinValue.getMaxAgentId());
    }

    private void testMemoryMax(LongApplicationStatPoint containerPoint, AggreJoinContainerBo aggreJoinContainerBo) {
        final JoinLongFieldBo memoryMaxJoinValue = aggreJoinContainerBo.getMemoryMaxJoinValue();
        assertEquals(containerPoint.getXVal(), aggreJoinContainerBo.getTimestamp());
        assertEquals(containerPoint.getYValForAvg(), memoryMaxJoinValue.getAvg(), 0);
        assertEquals(containerPoint.getYValForMin(), memoryMaxJoinValue.getMin(), 0);
        assertEquals(containerPoint.getYValForMax(), memoryMaxJoinValue.getMax(), 0);
        assertEquals(containerPoint.getAgentIdForMin(), memoryMaxJoinValue.getMinAgentId());
        assertEquals(containerPoint.getAgentIdForMax(), memoryMaxJoinValue.getMaxAgentId());
    }

    private void testMemoryUsage(LongApplicationStatPoint containerPoint, AggreJoinContainerBo aggreJoinContainerBo) {
        final JoinLongFieldBo memoryUsageJoinValue = aggreJoinContainerBo.getMemoryUsageJoinValue();
        assertEquals(containerPoint.getXVal(), aggreJoinContainerBo.getTimestamp());
        assertEquals(containerPoint.getYValForAvg(), memoryUsageJoinValue.getAvg(), 0);
        assertEquals(containerPoint.getYValForMin(), memoryUsageJoinValue.getMin(), 0);
        assertEquals(containerPoint.getYValForMax(), memoryUsageJoinValue.getMax(), 0);
        assertEquals(containerPoint.getAgentIdForMin(), memoryUsageJoinValue.getMinAgentId());
        assertEquals(containerPoint.getAgentIdForMax(), memoryUsageJoinValue.getMaxAgentId());
    }
}
