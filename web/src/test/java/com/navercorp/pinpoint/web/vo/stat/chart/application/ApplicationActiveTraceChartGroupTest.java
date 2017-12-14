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

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.chart.Point;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinActiveTraceBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class ApplicationActiveTraceChartGroupTest {

    @Test
    public void createApplicationActiveTraceChartGroupTest() {
        long time = 1495418083250L;
        String id = "testApp";
        Range range = new Range(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);
        List<AggreJoinActiveTraceBo> aggreJoinActiveTraceBoList = new ArrayList<>(5);
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo1 = new AggreJoinActiveTraceBo(id, 1, (short)2, 150, 10, "app_1_1", 230, "app_1_2", time);
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo2 = new AggreJoinActiveTraceBo(id, 1, (short)2, 110, 22, "app_2_1", 330, "app_2_2", time - 60000);
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo3 = new AggreJoinActiveTraceBo(id, 1, (short)2, 120, 24, "app_3_1", 540, "app_3_2", time - 120000);
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo4 = new AggreJoinActiveTraceBo(id, 1, (short)2, 130, 25, "app_4_1", 560, "app_4_2", time - 180000);
        AggreJoinActiveTraceBo aggreJoinActiveTraceBo5 = new AggreJoinActiveTraceBo(id, 1, (short)2, 140, 12, "app_5_1", 260, "app_5_2", time - 240000);
        aggreJoinActiveTraceBoList.add(aggreJoinActiveTraceBo1);
        aggreJoinActiveTraceBoList.add(aggreJoinActiveTraceBo2);
        aggreJoinActiveTraceBoList.add(aggreJoinActiveTraceBo3);
        aggreJoinActiveTraceBoList.add(aggreJoinActiveTraceBo4);
        aggreJoinActiveTraceBoList.add(aggreJoinActiveTraceBo5);

        StatChartGroup applicationActiveTraceChartGroup = new ApplicationActiveTraceChart.ApplicationActiveTraceChartGroup(timeWindow, aggreJoinActiveTraceBoList);
        Map<StatChartGroup.ChartType, Chart<? extends Point>> charts = applicationActiveTraceChartGroup.getCharts();

        Chart<? extends Point> activeTraceChart = charts.get(ApplicationActiveTraceChart.ApplicationActiveTraceChartGroup.ActiveTraceChartType.ACTIVE_TRACE_COUNT);
        List<? extends Point> activeTracePointList = activeTraceChart.getPoints();
        assertEquals(5, activeTracePointList.size());
        int index = activeTracePointList.size();
        for (Point point : activeTracePointList) {
            testActiveTraceCount((ActiveTracePoint) point, aggreJoinActiveTraceBoList.get(--index));
        }
    }

    private void testActiveTraceCount(ActiveTracePoint activeTracePoint, AggreJoinActiveTraceBo aggreJoinActiveTraceBo) {
        assertEquals(activeTracePoint.getYValForAvg(), aggreJoinActiveTraceBo.getTotalCount());
        assertEquals(activeTracePoint.getYValForMin(), aggreJoinActiveTraceBo.getMinTotalCount());
        assertEquals(activeTracePoint.getYValForMax(), aggreJoinActiveTraceBo.getMaxTotalCount());
        assertEquals(activeTracePoint.getAgentIdForMin(), aggreJoinActiveTraceBo.getMinTotalCountAgentId());
        assertEquals(activeTracePoint.getAgentIdForMax(), aggreJoinActiveTraceBo.getMaxTotalCountAgentId());
    }

}