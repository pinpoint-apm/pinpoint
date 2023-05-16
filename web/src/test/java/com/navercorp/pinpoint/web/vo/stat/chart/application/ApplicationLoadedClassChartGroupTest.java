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
import com.navercorp.pinpoint.web.vo.stat.AggreJoinLoadedClassBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationLoadedClassChartGroupTest {
    @Test
    public void createApplicationLoadedClassChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);
        List<AggreJoinLoadedClassBo> aggreJoinLoadedClassBoList = List.of(
                new AggreJoinLoadedClassBo("testApp", 11, 20, "agent1_1", 60, "agent1_2", 11, 20, "agent1_1", 60, "agent1_2", time),
                new AggreJoinLoadedClassBo("testApp", 22, 10, "agent2_1", 52, "agent2_2", 22, 10, "agent2_1", 52, "agent2_2", time - 60000),
                new AggreJoinLoadedClassBo("testApp", 33, 9, "agent3_1", 39, "agent3_2", 33, 9, "agent3_1", 39, "agent3_2", time - 120000),
                new AggreJoinLoadedClassBo("testApp", 44, 25, "agent4_1", 42, "agent4_2", 44, 25, "agent4_1", 42, "agent4_2", time - 180000),
                new AggreJoinLoadedClassBo("testApp", 55, 54, "agent5_1", 55, "agent5_2", 55, 54, "agent5_1", 55, "agent5_2", time - 240000)
        );

        ChartGroupBuilder<AggreJoinLoadedClassBo, ApplicationStatPoint<Long>> builder = ApplicationLoadedClassChart.newChartBuilder();
        StatChartGroup<ApplicationStatPoint<Long>> statChartGroup = builder.build(timeWindow, aggreJoinLoadedClassBoList);
        Map<StatChartGroup.ChartType, Chart<ApplicationStatPoint<Long>>> charts = statChartGroup.getCharts();
        assertThat(charts).hasSize(2);

        Chart<ApplicationStatPoint<Long>> loadedClassChart = charts.get(ApplicationLoadedClassChart.LoadedClassChartType.LOADED_CLASS_COUNT);
        List<ApplicationStatPoint<Long>> loadedClassChartPoints = loadedClassChart.getPoints();
        assertThat(loadedClassChartPoints).hasSize(5);
        int index = loadedClassChartPoints.size();

        for (ApplicationStatPoint<Long> point : loadedClassChartPoints) {
            testLoadedCLass(point, aggreJoinLoadedClassBoList.get(--index));
        }

        Chart<ApplicationStatPoint<Long>> unloadedClassChart = charts.get(ApplicationLoadedClassChart.LoadedClassChartType.UNLOADED_CLASS_COUNT);
        List<ApplicationStatPoint<Long>> unloadedClassChartPoints = unloadedClassChart.getPoints();
        assertThat(unloadedClassChartPoints).hasSize(5);

        index = unloadedClassChartPoints.size();
        for (ApplicationStatPoint<Long> point : unloadedClassChartPoints) {
            testUnloadedCLass(point, aggreJoinLoadedClassBoList.get(--index));
        }
    }

    private void testLoadedCLass(ApplicationStatPoint<Long> point, AggreJoinLoadedClassBo loadedClassBo) {
        final JoinLongFieldBo loadedClass = loadedClassBo.getLoadedClassJoinValue();
        assertEquals(point.getXVal(), loadedClassBo.getTimestamp());
        assertEquals(point.getYValForAvg(), loadedClass.getAvg(), 0);
        assertEquals(point.getYValForMin(), loadedClass.getMin(), 0);
        assertEquals(point.getYValForMax(), loadedClass.getMax(), 0);
        assertEquals(point.getAgentIdForMin(), loadedClass.getMinAgentId());
        assertEquals(point.getAgentIdForMax(), loadedClass.getMaxAgentId());
    }

    private void testUnloadedCLass(ApplicationStatPoint<Long> point, AggreJoinLoadedClassBo loadedClassBo) {
        final JoinLongFieldBo unloadedClass = loadedClassBo.getUnloadedClassJoinValue();
        assertEquals(point.getXVal(), loadedClassBo.getTimestamp());
        assertEquals(point.getYValForAvg(), unloadedClass.getAvg(), 0);
        assertEquals(point.getYValForMin(), unloadedClass.getMin(), 0);
        assertEquals(point.getYValForMax(), unloadedClass.getMax(), 0);
        assertEquals(point.getAgentIdForMin(), unloadedClass.getMinAgentId());
        assertEquals(point.getAgentIdForMax(), unloadedClass.getMaxAgentId());
    }
}
