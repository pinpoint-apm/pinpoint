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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author minwoo.jung
 */
public class ApplicationDataSourceChartGroupTest {

    @Test
    public void createApplicationDataSourceChartGroup() {
        long time = 1495418083250L;
        Range range = new Range(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);

        List<AggreJoinDataSourceBo> aggreJoinDataSourceBoList = new ArrayList<>();
        AggreJoinDataSourceBo aggreJoinDataSourceBo1 = new AggreJoinDataSourceBo((short)1000, "jdbc:mysql", 30, 25, "agent_id_1", 60, "agent_id_6", time);
        AggreJoinDataSourceBo aggreJoinDataSourceBo2 = new AggreJoinDataSourceBo((short)1000, "jdbc:mysql", 20, 5, "agent_id_2", 30, "agent_id_7", time - 60000);
        AggreJoinDataSourceBo aggreJoinDataSourceBo3 = new AggreJoinDataSourceBo((short)1000, "jdbc:mysql", 10, 25, "agent_id_3", 50, "agent_id_8", time - 120000);
        AggreJoinDataSourceBo aggreJoinDataSourceBo4 = new AggreJoinDataSourceBo((short)1000, "jdbc:mysql", 40, 4, "agent_id_4", 70, "agent_id_9", time - 180000);
        AggreJoinDataSourceBo aggreJoinDataSourceBo5 = new AggreJoinDataSourceBo((short)1000, "jdbc:mysql", 50, 25, "agent_id_5", 80, "agent_id_10", time - 240000);
        aggreJoinDataSourceBoList.add(aggreJoinDataSourceBo1);
        aggreJoinDataSourceBoList.add(aggreJoinDataSourceBo2);
        aggreJoinDataSourceBoList.add(aggreJoinDataSourceBo3);
        aggreJoinDataSourceBoList.add(aggreJoinDataSourceBo4);
        aggreJoinDataSourceBoList.add(aggreJoinDataSourceBo5);

        StatChartGroup applicationDataSourceChartGroup = new ApplicationDataSourceChart.ApplicationDataSourceChartGroup(timeWindow, "jdbc:mysql", "dbcp2", aggreJoinDataSourceBoList);
        Map<StatChartGroup.ChartType, Chart<? extends Point>> charts = applicationDataSourceChartGroup.getCharts();
        assertEquals(1, charts.size());

        Chart dataSourceChart = charts.get(ApplicationDataSourceChart.ApplicationDataSourceChartGroup.DataSourceChartType.ACTIVE_CONNECTION_SIZE);
        List<Point> dataSourcePoints = dataSourceChart.getPoints();
        assertEquals(5, dataSourcePoints.size());
        int index = dataSourcePoints.size();

        for (Point point : dataSourcePoints) {
            testDataSource((DataSourcePoint)point, aggreJoinDataSourceBoList.get(--index));
        }
    }

    private void testDataSource(DataSourcePoint dataSourcePoint, AggreJoinDataSourceBo aggreJoinDataSourceBo) {
        assertEquals(dataSourcePoint.getXVal(), aggreJoinDataSourceBo.getTimestamp());
        assertEquals(dataSourcePoint.getYValForAvg(), aggreJoinDataSourceBo.getAvgActiveConnectionSize(), 0);
        assertEquals(dataSourcePoint.getYValForMin(), aggreJoinDataSourceBo.getMinActiveConnectionSize(), 0);
        assertEquals(dataSourcePoint.getYValForMax(), aggreJoinDataSourceBo.getMaxActiveConnectionSize(), 0);
        assertEquals(dataSourcePoint.getAgentIdForMin(), aggreJoinDataSourceBo.getMinActiveConnectionAgentId());
        assertEquals(dataSourcePoint.getAgentIdForMax(), aggreJoinDataSourceBo.getMaxActiveConnectionAgentId());
    }


}