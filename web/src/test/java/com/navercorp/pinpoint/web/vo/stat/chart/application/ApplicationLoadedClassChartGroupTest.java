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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationLoadedClassChartGroupTest {
    @Test
    public void createApplicationLoadedClassChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
        TimeWindow timeWindow = new TimeWindow(range);
        List<AggreJoinLoadedClassBo> aggreJoinLoadedClassBoList = new ArrayList<>(5);
        AggreJoinLoadedClassBo aggreJoinLoadedClassBo1 = new AggreJoinLoadedClassBo("testApp", 11, 20, "agent1_1", 60, "agent1_2", 11, 20, "agent1_1", 60, "agent1_2",time);
        AggreJoinLoadedClassBo aggreJoinLoadedClassBo2 = new AggreJoinLoadedClassBo("testApp", 22, 10, "agent2_1", 52, "agent2_2", 22, 10, "agent2_1", 52, "agent2_2",time - 60000);
        AggreJoinLoadedClassBo aggreJoinLoadedClassBo3 = new AggreJoinLoadedClassBo("testApp", 33, 9, "agent3_1", 39, "agent3_2", 33, 9, "agent3_1", 39, "agent3_2",time - 120000);
        AggreJoinLoadedClassBo aggreJoinLoadedClassBo4 = new AggreJoinLoadedClassBo("testApp", 44, 25, "agent4_1", 42, "agent4_2", 44, 25, "agent4_1", 42, "agent4_2",time - 180000);
        AggreJoinLoadedClassBo aggreJoinLoadedClassBo5 = new AggreJoinLoadedClassBo("testApp", 55, 54, "agent5_1", 55, "agent5_2", 55, 54, "agent5_1", 55, "agent5_2",time - 240000);
        aggreJoinLoadedClassBoList.add(aggreJoinLoadedClassBo1);
        aggreJoinLoadedClassBoList.add(aggreJoinLoadedClassBo2);
        aggreJoinLoadedClassBoList.add(aggreJoinLoadedClassBo3);
        aggreJoinLoadedClassBoList.add(aggreJoinLoadedClassBo4);
        aggreJoinLoadedClassBoList.add(aggreJoinLoadedClassBo5);

        ChartGroupBuilder<AggreJoinLoadedClassBo, LongApplicationStatPoint> builder = ApplicationLoadedClassChart.newChartBuilder();
        StatChartGroup<LongApplicationStatPoint> statChartGroup = builder.build(timeWindow, aggreJoinLoadedClassBoList);
        Map<StatChartGroup.ChartType, Chart<LongApplicationStatPoint>> charts = statChartGroup.getCharts();
        assertEquals(2, charts.size());

        Chart<LongApplicationStatPoint> loadedClassChart = charts.get(ApplicationLoadedClassChart.LoadedClassChartType.LOADED_CLASS_COUNT);
        List<LongApplicationStatPoint> loadedClassChartPoints = loadedClassChart.getPoints();
        assertEquals(5, loadedClassChartPoints.size());
        int index = loadedClassChartPoints.size();

        for (LongApplicationStatPoint point : loadedClassChartPoints) {
            testLoadedCLass(point, aggreJoinLoadedClassBoList.get(--index));
        }

        Chart<LongApplicationStatPoint> unloadedClassChart = charts.get(ApplicationLoadedClassChart.LoadedClassChartType.UNLOADED_CLASS_COUNT);
        List<LongApplicationStatPoint> unloadedClassChartPoints = unloadedClassChart.getPoints();
        assertEquals(5, unloadedClassChartPoints.size());
        index = unloadedClassChartPoints.size();

        for (LongApplicationStatPoint point : unloadedClassChartPoints) {
            testUnloadedCLass(point, aggreJoinLoadedClassBoList.get(--index));
        }
    }

    private void testLoadedCLass(LongApplicationStatPoint point, AggreJoinLoadedClassBo loadedClassBo) {
        final JoinLongFieldBo loadedClass = loadedClassBo.getLoadedClassJoinValue();
        assertEquals(point.getTimestamp(), loadedClassBo.getTimestamp());
        JoinLongFieldBo longFieldBo = point.getLongFieldBo();
        assertEquals(longFieldBo, loadedClass);
    }

    private void testUnloadedCLass(LongApplicationStatPoint point, AggreJoinLoadedClassBo loadedClassBo) {
        final JoinLongFieldBo unloadedClass = loadedClassBo.getUnloadedClassJoinValue();
        assertEquals(point.getTimestamp(), loadedClassBo.getTimestamp());
        JoinLongFieldBo longFieldBo = point.getLongFieldBo();
        assertEquals(longFieldBo, unloadedClass);
    }
}
