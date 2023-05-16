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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinTotalThreadCountBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationTotalThreadCountChartGroupTest {
    @Test
    public void createApplicationTotalThreadCountChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);
        List<AggreJoinTotalThreadCountBo> aggreJoinTotalThreadCountBoList = List.of(
                new AggreJoinTotalThreadCountBo("testApp", time, 11, 20, "agent1_1", 60, "agent1_2"),
                new AggreJoinTotalThreadCountBo("testApp", time - 60000, 22, 10, "agent2_1", 52, "agent2_2"),
                new AggreJoinTotalThreadCountBo("testApp", time - 120000, 33, 9, "agent3_1", 39, "agent3_2"),
                new AggreJoinTotalThreadCountBo("testApp", time - 180000, 44, 25, "agent4_1", 42, "agent4_2"),
                new AggreJoinTotalThreadCountBo("testApp", time - 240000, 55, 54, "agent5_1", 55, "agent5_2")
        );

        ChartGroupBuilder<AggreJoinTotalThreadCountBo, ApplicationStatPoint<Long>> builder = ApplicationTotalThreadCountChart.newChartBuilder();
        StatChartGroup<ApplicationStatPoint<Long>> group = builder.build(timeWindow, aggreJoinTotalThreadCountBoList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Long>>> charts = group.getCharts();
        assertThat(charts).hasSize(1);

        Chart<ApplicationStatPoint<Long>> totalThreadCountChart = charts.get(ApplicationTotalThreadCountChart.TotalThreadCountChartType.TOTAL_THREAD_COUNT);
        List<ApplicationStatPoint<Long>> totalThreadCountChartPoints = totalThreadCountChart.getPoints();
        assertThat(totalThreadCountChartPoints).hasSize(5);
        int index = totalThreadCountChartPoints.size();

        for (ApplicationStatPoint<Long> point : totalThreadCountChartPoints) {
            testTotalThreadCount(point, aggreJoinTotalThreadCountBoList.get(--index));
        }
    }

    private void testTotalThreadCount(ApplicationStatPoint<Long> point, AggreJoinTotalThreadCountBo totalThreadCountBo) {
        assertEquals(point.getXVal(), totalThreadCountBo.getTimestamp());
        final JoinLongFieldBo totalThreadCountJoinValue = totalThreadCountBo.getTotalThreadCountJoinValue();
        assertEquals(point.getYValForAvg(), totalThreadCountJoinValue.getAvg(), 0);
        assertEquals(point.getYValForMin(), totalThreadCountJoinValue.getMin(), 0);
        assertEquals(point.getYValForMax(), totalThreadCountJoinValue.getMax(), 0);
        assertEquals(point.getAgentIdForMin(), totalThreadCountJoinValue.getMinAgentId());
        assertEquals(point.getAgentIdForMax(), totalThreadCountJoinValue.getMaxAgentId());
    }
}
