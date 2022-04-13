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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinResponseTimeBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author minwoo.jung
 */
public class ApplicationResponseTimeChartGroupTest {

    @Test
    public void createApplicationResponseTimeChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        final String id = "test_app";
        List<AggreJoinResponseTimeBo> aggreJoinResponseTimeBoList = new ArrayList<AggreJoinResponseTimeBo>();
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo1 = new AggreJoinResponseTimeBo(id, time, 3000, 2, "app_1_1", 6000, "app_1_1");
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo2 = new AggreJoinResponseTimeBo(id, time - 60000, 4000, 200, "app_2_1", 9000, "app_2_1");
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo3 = new AggreJoinResponseTimeBo(id, time - 120000, 2000, 20, "app_3_1", 7000, "app_3_1");
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo4 = new AggreJoinResponseTimeBo(id, time - 180000, 5000, 20, "app_4_1", 8000, "app_4_1");
        AggreJoinResponseTimeBo aggreJoinResponseTimeBo5 = new AggreJoinResponseTimeBo(id, time - 240000, 1000, 10, "app_5_1", 6600, "app_5_1");
        aggreJoinResponseTimeBoList.add(aggreJoinResponseTimeBo1);
        aggreJoinResponseTimeBoList.add(aggreJoinResponseTimeBo2);
        aggreJoinResponseTimeBoList.add(aggreJoinResponseTimeBo3);
        aggreJoinResponseTimeBoList.add(aggreJoinResponseTimeBo4);
        aggreJoinResponseTimeBoList.add(aggreJoinResponseTimeBo5);

        ChartGroupBuilder<AggreJoinResponseTimeBo, ApplicationStatPoint<Double>> builder = ApplicationResponseTimeChart.newChartBuilder();
        StatChartGroup<ApplicationStatPoint<Double>> statChartGroup = builder.build(timeWindow, aggreJoinResponseTimeBoList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Double>>> charts = statChartGroup.getCharts();

        Chart<ApplicationStatPoint<Double>> responseTimeChart = charts.get(ApplicationResponseTimeChart.ResponseTimeChartType.RESPONSE_TIME);
        List<ApplicationStatPoint<Double>> responseTimePointList = responseTimeChart.getPoints();
        assertEquals(5, responseTimePointList.size());
        int index = responseTimePointList.size();

        for (ApplicationStatPoint<Double> point : responseTimePointList) {
            testResponseTimeCount(point, aggreJoinResponseTimeBoList.get(--index));
        }
    }

    private void testResponseTimeCount(ApplicationStatPoint<Double> responseTimePoint, AggreJoinResponseTimeBo aggreJoinResponseTimeBo) {
        final JoinLongFieldBo responseTimeJoinValue = aggreJoinResponseTimeBo.getResponseTimeJoinValue();
        assertEquals(responseTimePoint.getYValForAvg(), responseTimeJoinValue.getAvg(), 0);
        assertEquals(responseTimePoint.getYValForMin(), responseTimeJoinValue.getMin(), 0);
        assertEquals(responseTimePoint.getYValForMax(), responseTimeJoinValue.getMax(), 0);
        assertEquals(responseTimePoint.getAgentIdForMax(), responseTimeJoinValue.getMaxAgentId());
        assertEquals(responseTimePoint.getAgentIdForMin(), responseTimeJoinValue.getMinAgentId());
    }


}