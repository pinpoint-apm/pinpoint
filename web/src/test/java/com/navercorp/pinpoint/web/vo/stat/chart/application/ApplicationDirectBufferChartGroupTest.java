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

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinLongFieldBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.chart.Chart;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDirectBufferBo;
import com.navercorp.pinpoint.web.vo.stat.chart.ChartGroupBuilder;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChartGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Roy Kim
 */
public class ApplicationDirectBufferChartGroupTest {

    @Test
    public void createApplicationDirectBufferChartGroupTest() {
        long time = 1495418083250L;
        Range range = Range.between(time - 240000, time);
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

        ChartGroupBuilder<AggreJoinDirectBufferBo, LongApplicationStatPoint> builder = ApplicationDirectBufferChart.newChartBuilder();
        StatChartGroup<LongApplicationStatPoint> group = builder.build(timeWindow, aggreDirectBufferList);
        Map<StatChartGroup.ChartType, Chart<LongApplicationStatPoint>> charts = group.getCharts();
        assertEquals(4, charts.size());

        Chart<LongApplicationStatPoint> directCountChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.DIRECT_COUNT);
        List<LongApplicationStatPoint> directCountPoints = directCountChart.getPoints();
        assertEquals(5, directCountPoints.size());
        int index = directCountPoints.size();
        for (LongApplicationStatPoint point : directCountPoints) {
            testDirectCount(point, aggreDirectBufferList.get(--index));
        }

        Chart<LongApplicationStatPoint> directMemoryUsedChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.DIRECT_MEMORY_USED);
        List<LongApplicationStatPoint> directMemoryUsedPoints = directMemoryUsedChart.getPoints();
        assertEquals(5, directMemoryUsedPoints.size());
        index = directMemoryUsedPoints.size();
        for (LongApplicationStatPoint point : directMemoryUsedPoints) {
            testDirectMemoryUsed(point, aggreDirectBufferList.get(--index));
        }

        Chart<LongApplicationStatPoint> mappedCountChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.MAPPED_COUNT);
        List<LongApplicationStatPoint> mappeedCountPoints = mappedCountChart.getPoints();
        assertEquals(5, mappeedCountPoints.size());
        index = mappeedCountPoints.size();
        for (LongApplicationStatPoint point : mappeedCountPoints) {
            testMappedCount(point, aggreDirectBufferList.get(--index));
        }

        Chart<LongApplicationStatPoint> mappedMemoryUsedChart = charts.get(ApplicationDirectBufferChart.DirectBufferChartType.MAPPED_MEMORY_USED);
        List<LongApplicationStatPoint> mappedMemoryUsedPoints = mappedMemoryUsedChart.getPoints();
        assertEquals(5, mappedMemoryUsedPoints.size());
        index = mappedMemoryUsedPoints.size();
        for (LongApplicationStatPoint point : mappedMemoryUsedPoints) {
            testMappedMemoryUsed(point, aggreDirectBufferList.get(--index));
        }
    }

    private void testDirectCount(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo directCountJoinValue = aggreJoinDirectBufferBo.getDirectCountJoinValue();
        assertEquals(directBufferPoint.getTimestamp(), aggreJoinDirectBufferBo.getTimestamp());
        JoinLongFieldBo longFieldBo = directBufferPoint.getLongFieldBo();
        assertEquals(longFieldBo, directCountJoinValue);
    }

    private void testDirectMemoryUsed(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo directMemoryUsedJoinValue = aggreJoinDirectBufferBo.getDirectMemoryUsedJoinValue();
        assertEquals(directBufferPoint.getTimestamp(), aggreJoinDirectBufferBo.getTimestamp());
        JoinLongFieldBo longFieldBo = directBufferPoint.getLongFieldBo();
        assertEquals(longFieldBo, directMemoryUsedJoinValue);
    }

    private void testMappedCount(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo mappedCountJoinValue = aggreJoinDirectBufferBo.getMappedCountJoinValue();
        assertEquals(directBufferPoint.getTimestamp(), aggreJoinDirectBufferBo.getTimestamp());
        JoinLongFieldBo longFieldBo = directBufferPoint.getLongFieldBo();
        assertEquals(longFieldBo, mappedCountJoinValue);
    }

    private void testMappedMemoryUsed(LongApplicationStatPoint directBufferPoint, AggreJoinDirectBufferBo aggreJoinDirectBufferBo) {
        final JoinLongFieldBo mappedMemoryUsedJoinValue = aggreJoinDirectBufferBo.getMappedMemoryUsedJoinValue();
        assertEquals(directBufferPoint.getTimestamp(), aggreJoinDirectBufferBo.getTimestamp());
        JoinLongFieldBo longFieldBo = directBufferPoint.getLongFieldBo();
        assertEquals(longFieldBo, mappedMemoryUsedJoinValue);
    }
}