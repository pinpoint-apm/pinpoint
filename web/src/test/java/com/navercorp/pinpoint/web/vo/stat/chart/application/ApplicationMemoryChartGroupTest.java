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

package com.navercorp.pinpoint.web.vo.stat.chart.application;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinMemoryBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class ApplicationMemoryChartGroupTest {

    @Test
    public void createApplicationMemoryChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);
        List<AggreJoinMemoryBo> aggreJoinMemoryList = new ArrayList<>(5);
        AggreJoinMemoryBo aggreJoinMemoryBo1 = new AggreJoinMemoryBo("testApp", time, 3000, 1000, 5000, "agent1_1", "agent1_2", 300, 100, 500, "agent1_3", "agent1_4");
        AggreJoinMemoryBo aggreJoinMemoryBo2 = new AggreJoinMemoryBo("testApp", time - 60000, 2000, 1200, 5000, "agent2_1", "agent2_2", 200, 100, 600, "agent2_3", "agent2_4");
        AggreJoinMemoryBo aggreJoinMemoryBo3 = new AggreJoinMemoryBo("testApp", time - 120000, 1000, 1300, 7000, "agent3_1", "agent3_2", 400, 200, 700, "agent3_3", "agent3_4");
        AggreJoinMemoryBo aggreJoinMemoryBo4 = new AggreJoinMemoryBo("testApp", time - 180000, 5000, 1400, 8000, "agent4_1", "agent4_2", 500, 300, 800, "agent4_3", "agent4_4");
        AggreJoinMemoryBo aggreJoinMemoryBo5 = new AggreJoinMemoryBo("testApp", time - 240000, 4000, 1500, 9000, "agent5_1", "agent5_2", 400, 400, 900, "agent5_3", "agent5_4");
        aggreJoinMemoryList.add(aggreJoinMemoryBo1);
        aggreJoinMemoryList.add(aggreJoinMemoryBo2);
        aggreJoinMemoryList.add(aggreJoinMemoryBo3);
        aggreJoinMemoryList.add(aggreJoinMemoryBo4);
        aggreJoinMemoryList.add(aggreJoinMemoryBo5);

        ChartGroupBuilder<AggreJoinMemoryBo, ApplicationStatPoint<Double>> builder = ApplicationMemoryChart.newChartBuilder();
        StatChartGroup<ApplicationStatPoint<Double>> statChartGroup = builder.build(timeWindow, aggreJoinMemoryList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Double>>> charts = statChartGroup.getCharts();

        Chart<ApplicationStatPoint<Double>> heapChart = charts.get(ApplicationMemoryChart.MemoryChartType.MEMORY_HEAP);
        List<ApplicationStatPoint<Double>> heapPoints = heapChart.getPoints();
        assertEquals(5, heapPoints.size());
        int index = heapPoints.size();
        for (ApplicationStatPoint<Double> point : heapPoints) {
            testHeap(point, aggreJoinMemoryList.get(--index));
        }

        Chart<ApplicationStatPoint<Double>> nonHeapChart = charts.get(ApplicationMemoryChart.MemoryChartType.MEMORY_NON_HEAP);
        List<ApplicationStatPoint<Double>> nonHeapPoints = heapChart.getPoints();
        assertEquals(5, nonHeapPoints.size());
        index = nonHeapPoints.size();
        for (ApplicationStatPoint<Double> point : nonHeapPoints) {
            testHeap(point, aggreJoinMemoryList.get(--index));
        }
    }

    private void testHeap(ApplicationStatPoint<Double> memoryPoint, AggreJoinMemoryBo aggreJoinMemoryBo) {
        final JoinLongFieldBo heapUsedJoinValue = aggreJoinMemoryBo.getHeapUsedJoinValue();
        assertEquals(memoryPoint.getYValForAvg(), heapUsedJoinValue.getAvg(), 0);
        assertEquals(memoryPoint.getYValForMin(), heapUsedJoinValue.getMin(), 0);
        assertEquals(memoryPoint.getYValForMax(), heapUsedJoinValue.getMax(), 0);
        assertEquals(memoryPoint.getAgentIdForMin(), heapUsedJoinValue.getMinAgentId());
        assertEquals(memoryPoint.getAgentIdForMax(), heapUsedJoinValue.getMaxAgentId());
    }

}