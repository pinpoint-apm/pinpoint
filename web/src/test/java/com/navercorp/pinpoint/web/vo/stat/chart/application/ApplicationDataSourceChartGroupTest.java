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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinIntFieldBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author minwoo.jung
 */
public class ApplicationDataSourceChartGroupTest {

    @Test
    public void createApplicationDataSourceChartGroup() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        List<AggreJoinDataSourceBo> aggreJoinDataSourceBoList = List.of(
                new AggreJoinDataSourceBo((short) 1000, "jdbc:mysql", 30, 25, "agent_id_1", 60, "agent_id_6", time),
                new AggreJoinDataSourceBo((short) 1000, "jdbc:mysql", 20, 5, "agent_id_2", 30, "agent_id_7", time - 60000),
                new AggreJoinDataSourceBo((short) 1000, "jdbc:mysql", 10, 25, "agent_id_3", 50, "agent_id_8", time - 120000),
                new AggreJoinDataSourceBo((short) 1000, "jdbc:mysql", 40, 4, "agent_id_4", 70, "agent_id_9", time - 180000),
                new AggreJoinDataSourceBo((short) 1000, "jdbc:mysql", 50, 25, "agent_id_5", 80, "agent_id_10", time - 240000)
        );


        StatChartGroup<ApplicationStatPoint<Integer>> applicationDataSourceChartGroup = new ApplicationDataSourceChart.ApplicationDataSourceChartGroup(timeWindow, "jdbc:mysql", "dbcp2", aggreJoinDataSourceBoList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Integer>>> charts = applicationDataSourceChartGroup.getCharts();
        assertThat(charts).hasSize(1);

        Chart<ApplicationStatPoint<Integer>> dataSourceChart = charts.get(ApplicationDataSourceChart.DataSourceChartType.ACTIVE_CONNECTION_SIZE);
        List<ApplicationStatPoint<Integer>> dataSourcePoints = dataSourceChart.getPoints();
        assertThat(dataSourcePoints).hasSize(5);
        int index = dataSourcePoints.size();

        for (ApplicationStatPoint<Integer> point : dataSourcePoints) {
            testDataSource(point, aggreJoinDataSourceBoList.get(--index));
        }
    }

    private void testDataSource(ApplicationStatPoint<Integer> dataSourcePoint, AggreJoinDataSourceBo aggreJoinDataSourceBo) {
        assertEquals(dataSourcePoint.getXVal(), aggreJoinDataSourceBo.getTimestamp());
        final JoinIntFieldBo activeConnectionSizeJoinValue = aggreJoinDataSourceBo.getActiveConnectionSizeJoinValue();
        assertEquals(dataSourcePoint.getYValForAvg(), activeConnectionSizeJoinValue.getAvg(), 0);
        assertEquals(dataSourcePoint.getYValForMin(), activeConnectionSizeJoinValue.getMin(), 0);
        assertEquals(dataSourcePoint.getYValForMax(), activeConnectionSizeJoinValue.getMax(), 0);
        assertEquals(dataSourcePoint.getAgentIdForMin(), activeConnectionSizeJoinValue.getMinAgentId());
        assertEquals(dataSourcePoint.getAgentIdForMax(), activeConnectionSizeJoinValue.getMaxAgentId());
    }


}